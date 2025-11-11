package uno.anahata.nb.ai.tools;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.maven.model.Dependency;
import org.netbeans.api.project.Project;
import org.netbeans.modules.maven.api.NbMavenProject;
import uno.anahata.gemini.functions.AIToolMethod;
import uno.anahata.gemini.functions.AIToolParam;
import uno.anahata.nb.ai.model.maven.DeclaredArtifact;
import uno.anahata.nb.ai.model.maven.DependencyGroup;
import uno.anahata.nb.ai.model.maven.DependencyScope;

/**
 * Provides AI tool methods for querying a project's pom.xml file for declared dependencies.
 * @author pablo
 */
public class MavenPom {

    @AIToolMethod("Gets the list of dependencies directly declared in the pom.xml, grouped by scope and groupId for maximum token efficiency.")
    public static List<DependencyScope> getDeclaredDependencies(
            @AIToolParam("The ID of the project to analyze.") String projectId) throws Exception {
        
        Project project = Projects.findProject(projectId);
        NbMavenProject nbMavenProject = project.getLookup().lookup(NbMavenProject.class);
        
        // Group dependencies by scope first
        Map<String, List<Dependency>> dependenciesByScope = nbMavenProject.getMavenProject().getDependencies().stream()
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
}
