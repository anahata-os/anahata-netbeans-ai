package uno.anahata.nb.ai.tools;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import java.util.stream.Collectors;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.resolver.ArtifactNotFoundException;
import org.apache.maven.artifact.resolver.ArtifactResolutionException;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.maven.api.NbMavenProject;
import org.netbeans.modules.maven.api.execute.RunConfig;
import org.netbeans.modules.maven.api.execute.RunUtils;
import org.netbeans.modules.maven.embedder.EmbedderFactory;
import org.netbeans.modules.maven.embedder.MavenEmbedder;
import org.netbeans.modules.maven.execute.MavenCommandLineExecutor;
import org.openide.execution.ExecutionEngine;
import org.openide.execution.ExecutorTask;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbPreferences;
import org.openide.util.Task;
import org.openide.util.TaskListener;
import uno.anahata.gemini.functions.AIToolMethod;
import uno.anahata.gemini.functions.AIToolParam;
import uno.anahata.gemini.functions.spi.pojos.TextChunk;
import uno.anahata.gemini.internal.TextUtils;
import uno.anahata.nb.ai.model.maven.MavenBuildResult;
import uno.anahata.nb.ai.model.maven.MavenBuildResult.ProcessStatus;

/**
 * Provides AI tool methods for interacting with Maven projects.
 * @author pablo
 * @deprecated This class is deprecated as of 2025-11-16 and will be removed in a future version. 
 *             All functionality has been consolidated into {@link MavenTools}.
 */
@Deprecated
public class Maven {
    private static final Logger LOG = Logger.getLogger(Maven.class.getName());
    private static final int MAX_OUTPUT_LINES = 100;
    private static final int MAX_LINE_LENGTH = 2000;
    private static final long DEFAULT_TIMEOUT_MS = 300_000; // 5 minutes

    /**
     * @deprecated Use {@link MavenTools#getMavenPath()} instead.
     */
    @AIToolMethod("Gets the path to the Maven installation configured in NetBeans.")
    @Deprecated
    public static String getMavenPath() {
        try {
            Preferences prefs = NbPreferences.root().node("org/netbeans/modules/maven");
            return prefs.get("commandLineMavenPath", "PREFERENCE_NOT_FOUND");
        } catch (Throwable t) {
            return "EXECUTION_FAILED: " + t.toString();
        }
    }

    /**
     * @deprecated Use {@link MavenTools#runGoals(String, List, List, Map, List, Long)} instead.
     */
    @AIToolMethod(value = "Executes a list of Maven goals on a Project synchronously (waits for the build to finish), capturing the last " + MAX_OUTPUT_LINES + " lines of the output.", behavior = uno.anahata.gemini.functions.ContextBehavior.EPHEMERAL)
    @Deprecated
    public static MavenBuildResult runGoals(
            @AIToolParam("The ID of the project to run the goals on.") String projectId,
            @AIToolParam("A list of Maven goals to execute (e.g., ['clean', 'install']).") List<String> goals,
            @AIToolParam("A list of profiles to activate.") List<String> profiles,
            @AIToolParam("A map of properties to set.") Map<String, String> properties,
            @AIToolParam("A list of additional Maven options.") List<String> options,
            @AIToolParam("The maximum time to wait for the build to complete, in milliseconds.") Long timeout) throws Exception {

        Project project = Projects.findProject(projectId);
        
        long effectiveTimeout = timeout != null ? timeout : DEFAULT_TIMEOUT_MS;
        
        ProjectInformation info = ProjectUtils.getInformation(project);
        String displayName = info.getDisplayName();
        String goalsString = String.join(" ", goals);
        String tabTitle = "Anahata - " + displayName + " (" + goalsString + ")";

        List<String> commandLine = new ArrayList<>(goals);
        if (options != null) {
            commandLine.addAll(options);
        }

        RunConfig config = RunUtils.createRunConfig(
                FileUtil.toFile(project.getProjectDirectory()),
                project,
                tabTitle,
                commandLine
        );

        if (profiles != null && !profiles.isEmpty()) {
            config.setActivatedProfiles(profiles);
        }

        if (properties != null) {
            properties.forEach(config::setProperty);
        }

        TeeInputOutput teeIO = new TeeInputOutput(org.openide.windows.IOProvider.getDefault().getIO(config.getTaskDisplayName(), true));
        MavenCommandLineExecutor executor = new MavenCommandLineExecutor(config, teeIO, null);

        LOG.info("Executing Maven build via ExecutionEngine to avoid RunUtils deadlock...");
        ExecutorTask task = ExecutionEngine.getDefault().execute(
            config.getTaskDisplayName(),
            executor,
            teeIO
        );
        executor.setTask(task);
        LOG.info("Task launched. Attaching SAFE listener.");

        CompletableFuture<Integer> future = new CompletableFuture<>();
        task.addTaskListener(new TaskListener() {
            @Override
            public void taskFinished(Task finishedTask) {
                LOG.info("SAFE LISTENER: Task finished.");
                int taskResult = -1; // Default to error
                if (finishedTask instanceof ExecutorTask) {
                    taskResult = ((ExecutorTask) finishedTask).result();
                } else {
                    LOG.log(Level.WARNING, "Task finished, but it was not an ExecutorTask. Cannot get exit code. Task type: {0}", finishedTask.getClass().getName());
                }
                future.complete(taskResult);
                finishedTask.removeTaskListener(this);
            }
        });

        ProcessStatus status;
        Integer exitCode = null;

        try {
            exitCode = future.get(effectiveTimeout, TimeUnit.MILLISECONDS);
            status = ProcessStatus.COMPLETED;
        } catch (TimeoutException e) {
            task.stop();
            status = ProcessStatus.TIMEOUT;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            task.stop();
            status = ProcessStatus.INTERRUPTED;
        } catch (Exception e) {
            task.stop();
            status = ProcessStatus.COMPLETED; // The future completed, but with an exception.
            exitCode = -1; // Generic error code
            LOG.log(Level.WARNING, "Error while waiting for build future", e);
        }

        String capturedOutput = teeIO.getCapturedOutput();
        String capturedError = teeIO.getCapturedError();
        String fullLogContent = "--- STDOUT ---\n" + capturedOutput + "\n\n--- STDERR ---\n" + capturedError;
        String logFilePath = null;

        File tempLogFile = File.createTempFile("anahata-maven-build-", ".log");
        try (PrintWriter out = new PrintWriter(new FileWriter(tempLogFile))) {
            out.println(fullLogContent);
        }
        logFilePath = tempLogFile.getAbsolutePath();
        
        // Use TextUtils to get the tail of the output, respecting line boundaries and character encoding.
        int totalLines = (int) capturedOutput.lines().count();
        int startIndex = Math.max(0, totalLines - MAX_OUTPUT_LINES);
        
        TextChunk stdoutChunk = TextUtils.processText(capturedOutput, startIndex, MAX_OUTPUT_LINES, null, MAX_LINE_LENGTH);
        TextChunk stderrChunk = TextUtils.processText(capturedError, 0, null, null, MAX_LINE_LENGTH); // Show all of stderr, but truncate long lines

        return new MavenBuildResult(status, exitCode, stdoutChunk, stderrChunk, logFilePath);
    }
    
    /**
     * @deprecated Use {@link MavenTools#downloadProjectDependencies(String, List)} instead.
     */
    @AIToolMethod("Downloads all missing dependencies artifacts (e.g., 'sources', 'javadoc') for a given Maven project's dependencies.")
    @Deprecated
    public static String downloadProjectDependencies(
            @AIToolParam("The ID of the project to download dependencies for.") String projectId,
            @AIToolParam("A list of classifiers to download (e.g., ['sources', 'javadoc']).") List<String> classifiers) throws Exception {
        
        Project project = Projects.findProject(projectId);
        NbMavenProject nbMavenProject = project.getLookup().lookup(NbMavenProject.class);
        if (nbMavenProject == null) {
            throw new IllegalStateException("Project '" + projectId + "' is not a Maven project or could not be found.");
        }
        
        MavenEmbedder onlineEmbedder = EmbedderFactory.getOnlineEmbedder();
        java.util.Set<Artifact> artifacts = nbMavenProject.getMavenProject().getArtifacts();
        int totalSuccessCount = 0;
        int totalFailCount = 0;
        StringBuilder errors = new StringBuilder();
        
        for (String classifier : classifiers) {
            int successCount = 0;
            int failCount = 0;
            
            for (Artifact art : artifacts) {
                if (downloadArtifact(onlineEmbedder, nbMavenProject, art, classifier, errors)) {
                    successCount++;
                } else {
                    failCount++;
                }
            }
            totalSuccessCount += successCount;
            totalFailCount += failCount;
        }

        NbMavenProject.fireMavenProjectReload(project);
        
        String artifactTypeNames = classifiers.stream()
                .map(c -> c.substring(0, 1).toUpperCase() + c.substring(1))
                .collect(Collectors.joining(" and "));
        
        return buildResultString(artifactTypeNames, "Project", projectId, totalSuccessCount, totalFailCount, errors);
    }

    /**
     * @deprecated Use {@link MavenTools#downloadDependencyArtifact(String, String, String, String, String, String)} instead.
     */
    @AIToolMethod("Downloads a specific classified artifact (e.g., 'sources', 'javadoc', or the main artifact if classifier is null) for a single dependency. This can be used to verify an artifact exists before adding it to a POM. Returns true on success, false on failure.")
    @Deprecated
    public static boolean downloadDependencyArtifact(
            @AIToolParam("The ID of the project to use for repository context.") String projectId,
            @AIToolParam("The groupId of the dependency.") String groupId,
            @AIToolParam("The artifactId of the dependency.") String artifactId,
            @AIToolParam("The version of the dependency (e.g., 'LATEST', '1.0.0').") String version,
            @AIToolParam("The classifier of the artifact to download (e.g., 'sources', 'javadoc'). Use null for the main artifact.") String classifier,
            @AIToolParam("The type of the dependency (e.g., 'test-jar'). If null, defaults to 'jar'.") String type) throws Exception {
        
        Project project = Projects.findProject(projectId);
        NbMavenProject nbMavenProject = project.getLookup().lookup(NbMavenProject.class);
        if (nbMavenProject == null) {
            throw new IllegalStateException("Project '" + projectId + "' is not a Maven project or could not be found.");
        }
        
        MavenEmbedder embedder = EmbedderFactory.getOnlineEmbedder();
        
        // This method works even if the artifact is not a dependency of the project.
        // It creates a temporary artifact object and tries to resolve it against the configured repositories.
        Artifact temporaryArtifact = embedder.createArtifactWithClassifier(
                groupId, 
                artifactId, 
                version, 
                type != null ? type : "jar", 
                classifier
        );
        
        return downloadArtifact(embedder, nbMavenProject, temporaryArtifact, classifier, new StringBuilder());
    }
    
    private static boolean downloadArtifact(MavenEmbedder embedder, NbMavenProject project, Artifact art, String classifier, StringBuilder errors) {
        if (Artifact.SCOPE_SYSTEM.equals(art.getScope())) {
            return false; // Skip system-scoped artifacts
        }
        try {
            // The artifact 'art' might already have a classifier. The createArtifactWithClassifier method handles this correctly.
            // If 'classifier' is null, it resolves the main artifact. If not, it resolves the classified one.
            Artifact artifactToResolve = embedder.createArtifactWithClassifier(
                    art.getGroupId(),
                    art.getArtifactId(),
                    art.getVersion(),
                    art.getType(),
                    classifier
            );
            
            embedder.resolveArtifact(
                    artifactToResolve,
                    project.getMavenProject().getRemoteArtifactRepositories(),
                    embedder.getLocalRepository()
            );
            return true;
        } catch (ArtifactNotFoundException e) {
            errors.append(classifier).append(" not found for ").append(art.getId()).append("\n");
        } catch (ArtifactResolutionException e) {
            errors.append("Could not resolve ").append(classifier).append(" for ").append(art.getId()).append(": ").append(e.getMessage()).append("\n");
        } catch (Exception e) {
            errors.append("An unexpected error occurred for ").append(art.getId()).append(" while downloading ").append(classifier).append(": ").append(e.getMessage()).append("\n");
        }
        return false;
    }
    
    private static String buildResultString(String artifactType, String targetType, String targetId, int success, int failed, StringBuilder errors) {
        String result = String.format("%s download for %s '%s' complete. Success: %d, Failed: %d.", artifactType, targetType, targetId, success, failed);
        if (failed > 0) {
            result += "\nErrors:\n" + errors.toString();
        }
        return result;
    }
}