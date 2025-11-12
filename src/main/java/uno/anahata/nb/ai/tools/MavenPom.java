package uno.anahata.nb.ai.tools;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.model.Dependency;
import org.netbeans.api.project.Project;
import org.netbeans.modules.maven.api.ModelUtils;
import org.netbeans.modules.maven.api.NbMavenProject;
import org.openide.filesystems.FileObject;
import uno.anahata.gemini.functions.AIToolMethod;
import uno.anahata.gemini.functions.AIToolParam;
import uno.anahata.nb.ai.model.maven.DeclaredArtifact;
import uno.anahata.nb.ai.model.maven.DependencyGroup;
import uno.anahata.nb.ai.model.maven.DependencyScope;
import uno.anahata.nb.ai.model.maven.ResolvedDependencyGroup;
import uno.anahata.nb.ai.model.maven.ResolvedDependencyScope;

/**
 * Provides AI tool methods for querying and modifying a project's pom.xml file.
 * @author pablo
 */
public class MavenPom {

    @AIToolMethod("Adds a new dependency to the project's pom.xml file. This is a high-level 'super-tool' that safely modifies the POM using the official NetBeans APIs.")
    public static String addDependency(
            @AIToolParam("The ID of the project to modify.") String projectId,
            @AIToolParam("The groupId of the dependency (e.g., 'org.apache.commons').") String groupId,
            @AIToolParam("The artifactId of the dependency (e.g., 'commons-lang3').") String artifactId,
            @AIToolParam("The version of the dependency (e.g., '3.12.0').") String version,
            @AIToolParam("The scope of the dependency (e.g., 'compile', 'test'). If null, defaults to 'compile'.") String scope) {
        
        try {
            Project project = Projects.findProject(projectId);
            FileObject pom = project.getProjectDirectory().getFileObject("pom.xml");
            if (pom == null) {
                return "Error: Could not find pom.xml for project '" + projectId + "'.";
            }

            // The scope parameter is optional, defaulting to "compile" if null.
            String effectiveScope = (scope == null || scope.isBlank()) ? "compile" : scope;

            // Call the robust NetBeans API to modify the POM.
            // Passing null for type and classifier, and false for canbeUpdate, which are common defaults.
            ModelUtils.addDependency(pom, groupId, artifactId, version, null, effectiveScope, null, false);
            
            // Trigger a project reload to make the IDE aware of the change.
            NbMavenProject.fireMavenProjectReload(project);

            return "Success: Dependency '" + groupId + ":" + artifactId + ":" + version + "' was added to the pom.xml.";
        } catch (Exception e) {
            return "Error: Failed to add dependency. " + e.getMessage();
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
