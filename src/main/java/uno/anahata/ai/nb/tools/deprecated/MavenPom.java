package uno.anahata.ai.nb.tools.deprecated;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.model.Dependency;
import org.netbeans.api.project.Project;
import org.netbeans.modules.maven.api.ModelUtils;
import org.netbeans.modules.maven.api.NbMavenProject;
import org.openide.filesystems.FileObject;
import uno.anahata.ai.AnahataExecutors;
import uno.anahata.ai.tools.AIToolMethod;
import uno.anahata.ai.tools.AIToolParam;
import uno.anahata.ai.nb.model.maven.AddDependencyResult;
import uno.anahata.ai.nb.model.maven.DeclaredArtifact;
import uno.anahata.ai.nb.model.maven.DependencyGroup;
import uno.anahata.ai.nb.model.maven.DependencyScope;
import uno.anahata.ai.nb.model.maven.MavenBuildResult;
import uno.anahata.ai.nb.model.maven.ResolvedDependencyGroup;
import uno.anahata.ai.nb.model.maven.ResolvedDependencyScope;
import uno.anahata.ai.nb.tools.Maven;
import uno.anahata.ai.nb.tools.Projects;

/**
 * Provides AI tool methods for querying and modifying a project's pom.xml file.
 * @author pablo
 * @deprecated This class is deprecated as of 2025-11-16 and will be removed in a future version. 
 *             All functionality has been consolidated into {@link MavenTools}.
 */
@Deprecated
public class MavenPom {
    private static final Logger LOG = Logger.getLogger(MavenPom.class.getName());
    private static final ExecutorService MAVEN_EXECUTORS = AnahataExecutors.newCachedThreadPoolExecutor("nb-maven");

    /**
     * @deprecated Use {@link MavenTools#addDependency(String, String, String, String, String, String, String)} instead.
     */
    @AIToolMethod("The definitive 'super-tool' for adding a Maven dependency. It follows a safe, multi-phase process and returns a structured result object. The model is responsible for interpreting the result.")
    @Deprecated
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
            boolean preflightSuccess = Maven.downloadDependencyArtifact(projectId, groupId, artifactId, version, classifier, type);
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
            MavenBuildResult resolveResult = Maven.runGoals(projectId, Collections.singletonList("dependency:resolve"), null, null, null, null);
            resultBuilder.dependencyResolveResult(resolveResult);
            summary.append("Result: 'dependency:resolve' goal executed. See MavenBuildResult for details.\n\n");

            // Phase 4: Asynchronous Source/Javadoc Download
            summary.append("Phase 4: Triggering async download of sources and javadocs...\n");
            MAVEN_EXECUTORS.submit(() -> {
                try {
                    Maven.downloadProjectDependencies(projectId, Arrays.asList("sources", "javadoc"));
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

    /**
     * @deprecated Use {@link MavenTools#getDeclaredDependencies(String)} instead.
     */
    @AIToolMethod("Gets the list of dependencies directly declared in the pom.xml, grouped by scope and groupId for maximum token efficiency.")
    @Deprecated
    public static List<DependencyScope> getDeclaredDependencies(
            @AIToolParam("The ID of the project to analyze.") String projectId) throws Exception {
        
        Project project = Projects.findProject(projectId);
        NbMavenProject nbMavenProject = project.getLookup().lookup(NbMavenProject.class);
        List<Dependency> dependencies = nbMavenProject.getMavenProject().getDependencies();
        return groupDeclaredDependencies(dependencies);
    }

    /**
     * @deprecated Use {@link MavenTools#getResolvedDependencies(String)} instead.
     */
    @AIToolMethod("Gets the final, fully resolved list of transitive dependencies for the project, representing the actual runtime classpath. The output is in an ultra-compact format (List<ResolvedDependencyScope>) for maximum token efficiency.")
    @Deprecated
    public static List<ResolvedDependencyScope> getResolvedDependencies(
            @AIToolParam("The ID of the project to analyze.") String projectId) throws Exception {

        Project project = Projects.findProject(projectId);
        NbMavenProject nbMavenProject = project.getLookup().lookup(NbMavenProject.class);
        Collection<Artifact> artifacts = nbMavenProject.getMavenProject().getArtifacts();
        return groupResolvedArtifacts(artifacts);
    }

    /**
     * Private helper to transform the raw list of declared dependencies from the POM.
     */
    private static List<DependencyScope> groupDeclaredDependencies(List<Dependency> dependencies) {
        // Group dependencies by scope first
        Map<String, List<Dependency>> dependenciesByScope = dependencies.stream()
                .collect(Collectors.groupingBy(dep -> dep.getScope() == null ? "compile" : dep.getScope()));

        List<DependencyScope> result = new ArrayList<>();

        for (Map.Entry<String, List<Dependency>> scopeEntry : dependenciesByScope.entrySet()) {
            String scope = scopeEntry.getKey();
            List<Dependency> depsInScope = scopeEntry.getValue();

            // Then group by groupId
            Map<String, List<Dependency>> dependenciesByGroup = depsInScope.stream()
                    .collect(Collectors.groupingBy(Dependency::getGroupId));

            List<DependencyGroup> dependencyGroups = new ArrayList<>();
            for (Map.Entry<String, List<Dependency>> groupEntry : dependenciesByGroup.entrySet()) {
                String groupId = groupEntry.getKey();
                List<Dependency> depsInGroup = groupEntry.getValue();

                List<DeclaredArtifact> declaredArtifacts = new ArrayList<>();
                for (Dependency dep : depsInGroup) {
                    // Build the compact artifact string
                    StringBuilder artifactBuilder = new StringBuilder();
                    artifactBuilder.append(dep.getArtifactId()).append(':').append(dep.getVersion());
                    if (dep.getClassifier() != null && !dep.getClassifier().isEmpty()) {
                        artifactBuilder.append(':').append(dep.getClassifier());
                    }
                    if (dep.getType() != null && !dep.getType().equals("jar")) {
                        artifactBuilder.append(':').append(dep.getType());
                    }

                    // Handle exclusions, setting to null if empty
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
    
    /**
     * Private helper to transform the final list of resolved artifacts into the ultra-compact format.
     */
    private static List<ResolvedDependencyScope> groupResolvedArtifacts(Collection<Artifact> artifacts) {
        // Group artifacts by scope first
        Map<String, List<Artifact>> artifactsByScope = artifacts.stream()
                .collect(Collectors.groupingBy(art -> art.getScope() == null ? "compile" : art.getScope()));

        List<ResolvedDependencyScope> result = new ArrayList<>();

        for (Map.Entry<String, List<Artifact>> scopeEntry : artifactsByScope.entrySet()) {
            String scope = scopeEntry.getKey();
            List<Artifact> artifactsInScope = scopeEntry.getValue();

            // Then group by groupId
            Map<String, List<Artifact>> artifactsByGroup = artifactsInScope.stream()
                    .collect(Collectors.groupingBy(Artifact::getGroupId));

            List<ResolvedDependencyGroup> dependencyGroups = new ArrayList<>();
            for (Map.Entry<String, List<Artifact>> groupEntry : artifactsByGroup.entrySet()) {
                String groupId = groupEntry.getKey();
                List<Artifact> artifactsInGroup = groupEntry.getValue();

                List<String> compactArtifacts = new ArrayList<>();
                for (Artifact art : artifactsInGroup) {
                    // Build the compact artifact string (artifactId:version[:classifier][:type])
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
}