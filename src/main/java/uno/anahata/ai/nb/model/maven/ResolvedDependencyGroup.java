/* Licensed under the Apache License, Version 2.0 */
package uno.anahata.ai.nb.model.maven;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * A container that groups fully resolved artifacts by their common groupId.
 * This is the ultra-compact version of DependencyGroup, designed to save tokens
 * by holding a List<String> of compact artifact IDs instead of a List<DeclaredArtifact>.
 * @author pablo
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "A container that groups fully resolved artifacts by their common groupId in an ultra-compact format.")
public class ResolvedDependencyGroup {
    
    @Schema(description = "The common groupId for all artifacts in this group.", example = "org.apache.commons")
    private String id;
    
    @Schema(description = "The list of compact artifact IDs (artifactId:version[:classifier][:type]) belonging to this group.", example = "[\"commons-lang3:3.12.0\"]")
    private List<String> artifacts;
}