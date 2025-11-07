package uno.anahata.nb.ai.tools;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
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
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.resolver.ArtifactNotFoundException;
import org.apache.maven.artifact.resolver.ArtifactResolutionException;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.modules.maven.api.NbMavenProject;
import org.netbeans.modules.maven.api.execute.RunConfig;
import org.netbeans.modules.maven.api.execute.RunUtils;
import org.netbeans.modules.maven.embedder.EmbedderFactory;
import org.netbeans.modules.maven.embedder.MavenEmbedder;
import org.netbeans.modules.maven.execute.MavenCommandLineExecutor;
import org.openide.execution.ExecutionEngine;
import org.openide.execution.ExecutorTask;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbPreferences;
import org.openide.util.Task;
import org.openide.util.TaskListener;
import uno.anahata.gemini.functions.AIToolMethod;
import uno.anahata.gemini.functions.AIToolParam;
import uno.anahata.nb.ai.model.maven.MavenBuildResult;
import uno.anahata.nb.ai.model.maven.MavenBuildResult.ProcessStatus;

/**
 * Provides AI tool methods for interacting with Maven projects.
 * @author pablo
 */
public class Maven {
    private static final Logger LOG = Logger.getLogger(Maven.class.getName());
    private static final int MAX_OUTPUT_LENGTH = 3000;

    @AIToolMethod("Gets the path to the Maven installation configured in NetBeans.")
    public static String getMavenPath() {
        try {
            Preferences prefs = NbPreferences.root().node("org/netbeans/modules/maven");
            return prefs.get("commandLineMavenPath", "PREFERENCE_NOT_FOUND");
        } catch (Throwable t) {
            return "EXECUTION_FAILED: " + t.toString();
        }
    }

    @AIToolMethod(value = "Executes a list of Maven goals on a project synchronously, capturing the output.", behavior = uno.anahata.gemini.functions.ContextBehavior.EPHEMERAL)
    public static MavenBuildResult runGoals(
            @AIToolParam("The ID of the project to run the goals on.") String projectId,
            @AIToolParam("A list of Maven goals to execute (e.g., ['clean', 'install']).") List<String> goals,
            @AIToolParam("A list of profiles to activate.") List<String> profiles,
            @AIToolParam("A map of properties to set.") Map<String, String> properties,
            @AIToolParam("A list of additional Maven options.") List<String> options,
            @AIToolParam("The maximum time to wait for the build to complete, in milliseconds.") long timeout) throws Exception {

        Project project = findProject(projectId);
        if (project == null) {
            throw new IllegalArgumentException("Project not found or not open: " + projectId);
        }

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
            exitCode = future.get(timeout, TimeUnit.MILLISECONDS);
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

        if (fullLogContent.length() > MAX_OUTPUT_LENGTH) {
            File tempLogFile = File.createTempFile("anahata-maven-build-", ".log");
            try (PrintWriter out = new PrintWriter(new FileWriter(tempLogFile))) {
                out.println(fullLogContent);
            }
            logFilePath = tempLogFile.getAbsolutePath();
        }

        String finalOutput = truncate(capturedOutput, "stdout", logFilePath);
        String finalErrorOutput = truncate(capturedError, "stderr", logFilePath);

        return new MavenBuildResult(status, exitCode, finalOutput, finalErrorOutput, logFilePath);
    }
    
    private static String truncate(String content, String streamName, String logFilePath) {
        if (content.length() > MAX_OUTPUT_LENGTH) {
            String message = logFilePath != null 
                ? "Truncated. Full " + streamName + " (" + content.length() + " chars) in combined log: " + logFilePath
                : "Truncated. Full " + streamName + " (" + content.length() + " chars) available in output tab.";
            
            return message + "\n...\n" + content.substring(content.length() - MAX_OUTPUT_LENGTH);
        }
        return content;
    }

    @AIToolMethod("Downloads all missing sources for a given Maven project's dependencies.")
    public static String downloadProjectSources(String projectId) throws Exception {
        Project project = findProject(projectId);
        NbMavenProject nbMavenProject = project.getLookup().lookup(NbMavenProject.class);
        if (nbMavenProject == null) {
            throw new IllegalStateException("Could not find NbMavenProject for project: " + projectId);
        }

        MavenEmbedder onlineEmbedder = EmbedderFactory.getOnlineEmbedder();
        java.util.Set<Artifact> artifacts = nbMavenProject.getMavenProject().getArtifacts();
        int successCount = 0;
        int failCount = 0;
        StringBuilder errors = new StringBuilder();

        for (Artifact art : artifacts) {
            if (downloadArtifactSource(onlineEmbedder, nbMavenProject, art, errors)) {
                successCount++;
            } else {
                failCount++;
            }
        }

        NbMavenProject.fireMavenProjectReload(project);
        return buildResultString("Project", projectId, successCount, failCount, errors);
    }

    @AIToolMethod("Downloads sources for a single, specific dependency of a given Maven project.")
    public static String downloadDependencySource(String projectId, String groupId, String artifactId) throws Exception {
        Project project = findProject(projectId);
        NbMavenProject nbMavenProject = project.getLookup().lookup(NbMavenProject.class);
        if (nbMavenProject == null) {
            throw new IllegalStateException("Could not find NbMavenProject for project: " + projectId);
        }

        MavenEmbedder onlineEmbedder = EmbedderFactory.getOnlineEmbedder();
        java.util.Set<Artifact> artifacts = nbMavenProject.getMavenProject().getArtifacts();
        int successCount = 0;
        int failCount = 0;
        StringBuilder errors = new StringBuilder();
        boolean found = false;

        for (Artifact art : artifacts) {
            if (art.getGroupId().equals(groupId) && art.getArtifactId().equals(artifactId)) {
                found = true;
                if (downloadArtifactSource(onlineEmbedder, nbMavenProject, art, errors)) {
                    successCount++;
                } else {
                    failCount++;
                }
                break;
            }
        }

        if (!found) {
            return "Error: Dependency " + groupId + ":" + artifactId + " not found in project " + projectId;
        }

        NbMavenProject.fireMavenProjectReload(project);
        return buildResultString("Dependency", groupId + ":" + artifactId, successCount, failCount, errors);
    }

    private static boolean downloadArtifactSource(MavenEmbedder embedder, NbMavenProject project, Artifact art, StringBuilder errors) {
        if (Artifact.SCOPE_SYSTEM.equals(art.getScope())) {
            return false; // Skip system-scoped artifacts
        }
        try {
            Artifact sourcesArtifact = embedder.createArtifactWithClassifier(
                    art.getGroupId(),
                    art.getArtifactId(),
                    art.getVersion(),
                    art.getType(),
                    "sources"
            );
            embedder.resolveArtifact(
                    sourcesArtifact,
                    project.getMavenProject().getRemoteArtifactRepositories(),
                    embedder.getLocalRepository()
            );
            return true;
        } catch (ArtifactNotFoundException e) {
            errors.append("Sources not found for ").append(art.getId()).append("\n");
        } catch (ArtifactResolutionException e) {
            errors.append("Could not resolve sources for ").append(art.getId()).append(": ").append(e.getMessage()).append("\n");
        } catch (Exception e) {
            errors.append("An unexpected error occurred for ").append(art.getId()).append(": ").append(e.getMessage()).append("\n");
        }
        return false;
    }

    private static Project findProject(String id) {
        for (Project project : OpenProjects.getDefault().getOpenProjects()) {
            FileObject root = project.getProjectDirectory();
            if (root.getNameExt().equals(id)) {
                return project;
            }
        }
        throw new IllegalArgumentException("Project not found or not open: " + id);
    }

    private static String buildResultString(String targetType, String targetId, int success, int failed, StringBuilder errors) {
        String result = String.format("Source download for %s '%s' complete. Success: %d, Failed: %d.", targetType, targetId, success, failed);
        if (failed > 0) {
            result += "\nErrors:\n" + errors.toString();
        }
        return result;
    }
}