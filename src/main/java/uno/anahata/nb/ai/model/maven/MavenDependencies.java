package uno.anahata.nb.ai.model.maven;

import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * A POJO that holds structured information about a Maven project's dependencies.
 * The dependencies are grouped by scope, and then by groupId for token efficiency.
 * 
 * @author pablo
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MavenDependencies {

    /**
     * Dependencies as they are explicitly declared in the pom.xml.
     * Structure: Scope -> GroupID -> List of "artifactId:version"
     */
    private Map<String, Map<String, List<String>>> declaredDependencies;

    /**
     * The full, transitive dependency tree after resolution.
     * Structure: Scope -> GroupID -> List of "artifactId:version"
     */
    private Map<String, Map<String, List<String>>> resolvedDependencies;
}
