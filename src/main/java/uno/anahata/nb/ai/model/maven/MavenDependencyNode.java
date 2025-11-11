package uno.anahata.nb.ai.model.maven;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents a single node in a Maven dependency tree. 
 * This class is recursive, allowing it to represent the entire transitive dependency hierarchy.
 * @author pablo
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Represents a single node in a Maven dependency tree. This class is recursive, allowing it to represent the entire transitive dependency hierarchy.")
public class MavenDependencyNode {

    @Schema(description = "The groupId of the dependency.", example = "org.apache.commons")
    private String groupId;

    @Schema(description = "The artifactId of the dependency.", example = "commons-lang3")
    private String artifactId;

    @Schema(description = "The version of the dependency.", example = "3.12.0")
    private String version;

    @Schema(description = "The scope of the dependency (e.g., compile, test, provided).", example = "compile")
    private String scope;

    @Schema(description = "The resolution status of the dependency in the tree. Note: A null status implies the dependency is included. This field is only populated for special cases (e.g., conflicts, cycles) to save tokens.")
    private MavenDependencyStatus status;
    
    @Schema(description = "A list of exclusions defined for this dependency.")
    private List<MavenExclusion> exclusions;

    @Schema(description = "A list of child nodes representing the transitive dependencies of this artifact.")
    private List<MavenDependencyNode> children;
}
