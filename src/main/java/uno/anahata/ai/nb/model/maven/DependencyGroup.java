/* Licensed under the Apache License, Version 2.0 */
package uno.anahata.ai.nb.model.maven;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * A container that groups declared artifacts by their common groupId.
 * @author pablo
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "A container that groups declared artifacts by their common groupId.")
public class DependencyGroup {
    
    @Schema(description = "The common groupId for all artifacts in this group.", example = "org.apache.commons")
    private String id;
    
    @Schema(description = "The list of artifacts belonging to this group.")
    private List<DeclaredArtifact> artifacts;
}
