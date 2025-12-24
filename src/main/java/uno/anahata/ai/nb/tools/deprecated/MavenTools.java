/* Licensed under the Apache License, Version 2.0 */
package uno.anahata.ai.nb.tools.deprecated;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import java.util.stream.Collectors;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.resolver.ArtifactNotFoundException;
import org.apache.maven.artifact.resolver.ArtifactResolutionException;
import org.apache.maven.model.Dependency;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.maven.api.ModelUtils;
import org.netbeans.modules.maven.api.NbMavenProject;
import org.netbeans.modules.maven.api.execute.RunConfig;
import org.netbeans.modules.maven.api.execute.RunUtils;
import org.netbeans.modules.maven.embedder.EmbedderFactory;
import org.netbeans.modules.maven.embedder.MavenEmbedder;
import org.netbeans.modules.maven.execute.MavenCommandLineExecutor;
import org.netbeans.modules.maven.indexer.api.NBVersionInfo;
import org.netbeans.modules.maven.indexer.api.QueryField;
import org.netbeans.modules.maven.indexer.api.RepositoryPreferences;
import org.netbeans.modules.maven.indexer.api.RepositoryQueries;
import org.openide.execution.ExecutionEngine;
import org.openide.execution.ExecutorTask;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbPreferences;
import org.openide.util.Task;
import org.openide.util.TaskListener;
import uno.anahata.ai.AnahataExecutors;
import uno.anahata.ai.tools.AIToolMethod;
import uno.anahata.ai.tools.AIToolParam;
import uno.anahata.ai.tools.spi.pojos.TextChunk;
import uno.anahata.ai.internal.TextUtils;
import uno.anahata.ai.nb.model.maven.AddDependencyResult;
import uno.anahata.ai.nb.model.maven.DeclaredArtifact;
import uno.anahata.ai.nb.model.maven.DependencyGroup;
import uno.anahata.ai.nb.model.maven.DependencyScope;
import uno.anahata.ai.nb.model.maven.MavenArtifactSearchResult;
import uno.anahata.ai.nb.model.maven.MavenBuildResult;
import uno.anahata.ai.nb.model.maven.MavenSearchResultPage;
import uno.anahata.ai.nb.model.maven.ResolvedDependencyGroup;
import uno.anahata.ai.nb.model.maven.ResolvedDependencyScope;
import uno.anahata.ai.nb.tools.Projects;
import uno.anahata.ai.nb.util.TeeInputOutput;

/**
 * Consolidated "super-tool" class for all Maven-related AI operations.
 * This class combines functionality from the deprecated Maven, MavenPom, and MavenSearch classes.
 * It serves as the single, definitive entry point for searching, modifying, and executing Maven tasks.
 * 
 * @author pablo
 */
public class MavenTools {
    private static final Logger LOG = Logger.getLogger(MavenTools.class.getName());
    private static final ExecutorService MAVEN_EXECUTORS = AnahataExecutors.newCachedThreadPoolExecutor("nb-maven");
    private static final int MAX_OUTPUT_LINES = 100;
    private static final int MAX_LINE_LENGTH = 2000;
    private static final long DEFAULT_TIMEOUT_MS = 300_000; // 5 minutes

    //<editor-fold defaultstate="collapsed" desc="From MavenSearch.java">
    @AIToolMethod("Searches the Maven index for artifacts matching a given query. The search is performed across all configured repositories (local, remote, and project-specific).")
    public static MavenSearchResultPage searchMavenIndex(
            @AIToolParam("The search query, with terms separated by spaces (e.g., 'junit platform').") String query,
            @AIToolParam("The starting index (0-based) for pagination. Defaults to 0 if null.") Integer startIndex,
            @AIToolParam("The maximum number of results to return. Defaults to 100 if null.") Integer pageSize) throws Exception {
        
        if (query == null || query.isBlank()) {
            throw new IllegalArgumentException("Search query cannot be null or blank.");
        }
        
        String q = query.trim();
        
        // Apply defaults
        int start = startIndex != null ? startIndex : 0;
        int size = pageSize != null ? pageSize : 100;
        
        // The AddDependencyPanel logic splits the query by spaces to allow multi-keyword searches.
        String[] splits = q.split(" "); 

        List<QueryField> fields = new ArrayList<>();
        
        // Fields to search in the index
        List<String> fStrings = new ArrayList<>();
        fStrings.add(QueryField.FIELD_GROUPID);
        fStrings.add(QueryField.FIELD_ARTIFACTID);
        fStrings.add(QueryField.FIELD_VERSION);
        fStrings.add(QueryField.FIELD_NAME);
        fStrings.add(QueryField.FIELD_DESCRIPTION);
        fStrings.add(QueryField.FIELD_CLASSES);

        // For each word in the query, search all fields
        for (String curText : splits) {
            for (String fld : fStrings) {
                QueryField f = new QueryField();
                f.setField(fld);
                f.setValue(curText);
                f.setMatch(QueryField.MATCH_ANY);
                f.setOccur(QueryField.OCCUR_SHOULD);
                fields.add(f);
            }
        }

        // Perform the search against all configured repositories
        RepositoryQueries.Result<NBVersionInfo> results = RepositoryQueries.findResult(fields, RepositoryPreferences.getInstance().getRepositoryInfos());

        // Wait for partial results to complete (if any)
        if (results.isPartial()) {
            results.waitForSkipped();
        }

        // Process and return the results with pagination
        if (results.getResults() == null) {
            return new MavenSearchResultPage(start, 0, Collections.emptyList());
        }
        
        List<NBVersionInfo> allResults = results.getResults();
        int totalCount = allResults.size();

        List<MavenArtifactSearchResult> page = allResults.stream()
                .skip(start)
                .limit(size)
                .map(info -> new MavenArtifactSearchResult(
                        info.getGroupId(),
                        info.getArtifactId(),
                        info.getVersion(),
                        info.getRepoId(),
                        info.getPackaging(),
                        info.getProjectDescription()
                ))
                .collect(Collectors.toList());
        
        return new MavenSearchResultPage(start, totalCount, page);
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="From MavenPom.java">
    @AIToolMethod("The definitive 'super-tool' for adding a Maven dependency. It follows a safe, multi-phase process and returns a structured result object. The model is responsible for interpreting the result.")
    public static AddDependencyResult addDependency(
            @AIToolParam("The ID of the project to modify.") String projectId,
            @AIToolParam("The groupId of the dependency.") String groupId,
            @AIToolParam("The artifactId of the dependency.") String artifactId,
            @AIToolParam("The version of the dependency.") String version,
            @AIToolParam("The scope of the dependency (e.g., 'compile', 'test'). If null, defaults to 'compile'.") String scope,
            @AIToolParam("The classifier of the dependency (e.g., 'jdk17'). Can be null.") String classifier,
            @AIToolParam("The type of the dependency (e.g., 'test-jar'). If null, defaults to 'jar'.") String type) {
        
        AddDependencyResult.AddDependencyResultBuilder resultBuilder = AddDependencyResult.builder();
        StringBuilder summary = new StringBuilder();

        try {
            // Phase 1: Pre-flight Check
            summary.append("Phase 1: Pre-flight check...\n");
            boolean preflightSuccess = downloadDependencyArtifact(projectId, groupId, artifactId, version, classifier, type);
            resultBuilder.preflightCheckSuccess(preflightSuccess);

            if (!preflightSuccess) {
                summary.append("Result: FAILED. Main artifact could not be resolved. pom.xml was not modified.");
                return resultBuilder.summary(summary.toString()).build();
            }
            summary.append("Result: SUCCESS. Main artifact found.\n\n");

            // Phase 2: POM Modification
            summary.append("Phase 2: Modifying pom.xml...\n");
            Project project = Projects.findProject(projectId);
            FileObject pom = project.getProjectDirectory().getFileObject("pom.xml");
            if (pom == null) {
                summary.append("Result: FAILED. Could not find pom.xml.");
                return resultBuilder.pomModificationSuccess(false).summary(summary.toString()).build();
            }

            String effectiveScope = (scope == null || scope.isBlank()) ? "compile" : scope;
            String effectiveType = (type == null || type.isBlank()) ? "jar" : type;
            String effectiveClassifier = (classifier == null || classifier.isBlank()) ? null : classifier;
            
            ModelUtils.addDependency(pom, groupId, artifactId, version, effectiveClassifier, effectiveScope, effectiveType, false);
            resultBuilder.pomModificationSuccess(true);
            summary.append("Result: SUCCESS. Dependency added to pom.xml.\n\n");

            // Phase 3: Transitive Dependencies
            summary.append("Phase 3: Resolving transitive dependencies...\n");
            MavenBuildResult resolveResult = runGoals(projectId, Collections.singletonList("dependency:resolve"), null, null, null, null);
            resultBuilder.dependencyResolveResult(resolveResult);
            summary.append("Result: 'dependency:resolve' goal executed. See MavenBuildResult for details.\n\n");

            // Phase 4: Asynchronous Source/Javadoc Download
            summary.append("Phase 4: Triggering async download of sources and javadocs...\n");
            MAVEN_EXECUTORS.submit(() -> {
                try {
                    downloadProjectDependencies(projectId, Arrays.asList("sources", "javadoc"));
                } catch (Exception e) {
                    LOG.log(Level.WARNING, "Error during async source/javadoc download", e);
                }
            });
            resultBuilder.asyncDownloadsLaunched(true);
            summary.append("Result: Background download task launched.\n");

            // Final Step: Reload Project
            NbMavenProject.fireMavenProjectReload(project);
            summary.append("Project reload triggered.");

            return resultBuilder.summary(summary.toString()).build();

        } catch (Exception e) {
            summary.append("\nFATAL ERROR: An unexpected exception occurred: ").append(e.getMessage());
            LOG.log(Level.SEVERE, "Add dependency failed", e);
            return resultBuilder.summary(summary.toString()).build();
        }
    }

    @AIToolMethod("Gets the list of dependencies directly declared in the pom.xml, grouped by scope and groupId for maximum token efficiency.")
    public static List<DependencyScope> getDeclaredDependencies(
            @AIToolParam("The ID of the project to analyze.") String projectId) throws Exception {
        
        Project project = Projects.findProject(projectId);
        NbMavenProject nbMavenProject = project.getLookup().lookup(NbMavenProject.class);
        List<Dependency> dependencies = nbMavenProject.getMavenProject().getDependencies();
        return groupDeclaredDependencies(dependencies);
    }

    @AIToolMethod("Gets the final, fully resolved list of transitive dependencies for the project, representing the actual runtime classpath. The output is in an ultra-compact format (List<ResolvedDependencyScope>) for maximum token efficiency.")
    public static List<ResolvedDependencyScope> getResolvedDependencies(
            @AIToolParam("The ID of the project to analyze.") String projectId) throws Exception {

        Project project = Projects.findProject(projectId);
        NbMavenProject nbMavenProject = project.getLookup().lookup(NbMavenProject.class);
        Collection<Artifact> artifacts = nbMavenProject.getMavenProject().getArtifacts();
        return groupResolvedArtifacts(artifacts);
    }

    private static List<DependencyScope> groupDeclaredDependencies(List<Dependency> dependencies) {
        Map<String, List<Dependency>> dependenciesByScope = dependencies.stream()
                .collect(Collectors.groupingBy(dep -> dep.getScope() == null ? "compile" : dep.getScope()));

        List<DependencyScope> result = new ArrayList<>();

        for (Map.Entry<String, List<Dependency>> scopeEntry : dependenciesByScope.entrySet()) {
            String scope = scopeEntry.getKey();
            List<Dependency> depsInScope = scopeEntry.getValue();

            Map<String, List<Dependency>> dependenciesByGroup = depsInScope.stream()
                    .collect(Collectors.groupingBy(Dependency::getGroupId));

            List<DependencyGroup> dependencyGroups = new ArrayList<>();
            for (Map.Entry<String, List<Dependency>> groupEntry : dependenciesByGroup.entrySet()) {
                String groupId = groupEntry.getKey();
                List<Dependency> depsInGroup = groupEntry.getValue();

                List<DeclaredArtifact> declaredArtifacts = new ArrayList<>();
                for (Dependency dep : depsInGroup) {
                    StringBuilder artifactBuilder = new StringBuilder();
                    artifactBuilder.append(dep.getArtifactId()).append(':').append(dep.getVersion());
                    if (dep.getClassifier() != null && !dep.getClassifier().isEmpty()) {
                        artifactBuilder.append(':').append(dep.getClassifier());
                    }
                    if (dep.getType() != null && !dep.getType().equals("jar")) {
                        artifactBuilder.append(':').append(dep.getType());
                    }

                    List<String> exclusions = null;
                    if (dep.getExclusions() != null && !dep.getExclusions().isEmpty()) {
                        exclusions = dep.getExclusions().stream()
                                .map(ex -> ex.getGroupId() + ":" + ex.getArtifactId())
                                .collect(Collectors.toList());
                    }
                    
                    declaredArtifacts.add(new DeclaredArtifact(artifactBuilder.toString(), exclusions));
                }
                dependencyGroups.add(new DependencyGroup(groupId, declaredArtifacts));
            }
            result.add(new DependencyScope(scope, dependencyGroups));
        }
        
        return result;
    }
    
    private static List<ResolvedDependencyScope> groupResolvedArtifacts(Collection<Artifact> artifacts) {
        Map<String, List<Artifact>> artifactsByScope = artifacts.stream()
                .collect(Collectors.groupingBy(art -> art.getScope() == null ? "compile" : art.getScope()));

        List<ResolvedDependencyScope> result = new ArrayList<>();

        for (Map.Entry<String, List<Artifact>> scopeEntry : artifactsByScope.entrySet()) {
            String scope = scopeEntry.getKey();
            List<Artifact> artifactsInScope = scopeEntry.getValue();

            Map<String, List<Artifact>> artifactsByGroup = artifactsInScope.stream()
                    .collect(Collectors.groupingBy(Artifact::getGroupId));

            List<ResolvedDependencyGroup> dependencyGroups = new ArrayList<>();
            for (Map.Entry<String, List<Artifact>> groupEntry : artifactsByGroup.entrySet()) {
                String groupId = groupEntry.getKey();
                List<Artifact> artifactsInGroup = groupEntry.getValue();

                List<String> compactArtifacts = new ArrayList<>();
                for (Artifact art : artifactsInGroup) {
                    StringBuilder artifactBuilder = new StringBuilder();
                    artifactBuilder.append(art.getArtifactId()).append(':').append(art.getVersion());
                    if (art.getClassifier() != null && !art.getClassifier().isEmpty()) {
                        artifactBuilder.append(':').append(art.getClassifier());
                    }
                    if (art.getType() != null && !art.getType().equals("jar")) {
                        artifactBuilder.append(':').append(art.getType());
                    }
                    
                    compactArtifacts.add(artifactBuilder.toString());
                }
                dependencyGroups.add(new ResolvedDependencyGroup(groupId, compactArtifacts));
            }
            result.add(new ResolvedDependencyScope(scope, dependencyGroups));
        }
        
        return result;
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="From Maven.java">
    @AIToolMethod("Gets the path to the Maven installation configured in NetBeans.")
    public static String getMavenPath() {
        try {
            Preferences prefs = NbPreferences.root().node("org/netbeans/modules/maven");
            return prefs.get("commandLineMavenPath", "PREFERENCE_NOT_FOUND");
        } catch (Throwable t) {
            return "EXECUTION_FAILED: " + t.toString();
        }
    }

    @AIToolMethod(value = "Executes a list of Maven goals on a Project synchronously (waits for the build to finish), capturing the last " + MAX_OUTPUT_LINES + " lines of the output.", behavior = uno.anahata.ai.tools.ContextBehavior.EPHEMERAL)
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

        MavenBuildResult.ProcessStatus status;
        Integer exitCode = null;

        try {
            exitCode = future.get(effectiveTimeout, TimeUnit.MILLISECONDS);
            status = MavenBuildResult.ProcessStatus.COMPLETED;
        } catch (TimeoutException e) {
            task.stop();
            status = MavenBuildResult.ProcessStatus.TIMEOUT;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            task.stop();
            status = MavenBuildResult.ProcessStatus.INTERRUPTED;
        } catch (Exception e) {
            task.stop();
            status = MavenBuildResult.ProcessStatus.COMPLETED; // The future completed, but with an exception.
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
        
        int totalLines = (int) capturedOutput.lines().count();
        int startIndex = Math.max(0, totalLines - MAX_OUTPUT_LINES);
        
        TextChunk stdoutChunk = TextUtils.processText(capturedOutput, startIndex, MAX_OUTPUT_LINES, null, MAX_LINE_LENGTH);
        TextChunk stderrChunk = TextUtils.processText(capturedError, 0, null, null, MAX_LINE_LENGTH); // Show all of stderr, but truncate long lines

        return new MavenBuildResult(status, exitCode, stdoutChunk, stderrChunk, logFilePath);
    }
    
    @AIToolMethod("Downloads all missing dependencies artifacts (e.g., 'sources', 'javadoc') for a given Maven project's dependencies.")
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

    @AIToolMethod("Downloads a specific classified artifact (e.g., 'sources', 'javadoc', or the main artifact if classifier is null) for a single dependency. This can be used to verify an artifact exists before adding it to a POM. Returns true on success, false on failure.")
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
            return false;
        }
        LOG.log(Level.INFO, "Attempting to resolve artifact: {0}:{1}:{2}:{3}:{4}", new Object[]{art.getGroupId(), art.getArtifactId(), art.getVersion(), art.getType(), classifier});
        try {
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
            LOG.log(Level.INFO, "Successfully resolved artifact: {0}", artifactToResolve.getId());
            return true;
        } catch (ArtifactNotFoundException e) {
            LOG.log(Level.WARNING, "Artifact not found: {0}", e.getMessage());
            errors.append(classifier).append(" not found for ").append(art.getId()).append("\n");
        } catch (ArtifactResolutionException e) {
            LOG.log(Level.WARNING, "Artifact resolution error: {0}", e.getMessage());
            errors.append("Could not resolve ").append(classifier).append(" for ").append(art.getId()).append(": ").append(e.getMessage()).append("\n");
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Unexpected error during artifact resolution", e);
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
    //</editor-fold>
}