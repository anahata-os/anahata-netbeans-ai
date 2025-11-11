package uno.anahata.nb.ai.model.maven;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents a Maven dependency exclusion.
 * @author pablo
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Represents a Maven dependency exclusion, corresponding to an <exclusion> tag in a pom.xml.")
public class MavenExclusion {
    
    @Schema(description = "The groupId of the artifact to be excluded.", example = "commons-logging")
    private String groupId;
    
    @Schema(description = "The artifactId of the artifact to be excluded.", example = "commons-logging")
    private String artifactId;
}
