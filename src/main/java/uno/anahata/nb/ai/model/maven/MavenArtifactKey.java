package uno.anahata.nb.ai.model.maven;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * A base class representing the fundamental key of a Maven artifact (groupId and artifactId).
 * This is used as a reusable component, for example, in defining exclusions.
 * @author pablo
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "The fundamental key of a Maven artifact, consisting of its groupId and artifactId.")
public class MavenArtifactKey {
    
    @Schema(description = "The groupId of the artifact.", example = "org.apache.commons")
    private String groupId;
    
    @Schema(description = "The artifactId of the artifact.", example = "commons-lang3")
    private String artifactId;
}
