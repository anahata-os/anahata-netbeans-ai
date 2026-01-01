/* Licensed under the Apache License, Version 2.0 */
package uno.anahata.ai.nb.model.maven;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents a declared artifact in a compact format, designed for token efficiency.
 * @author anahata
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Represents a declared artifact in a compact format.")
public class DeclaredArtifact {

    @Schema(
        description = "A compact string representing the artifact's coordinates in the format 'artifactId:version[:classifier][:type]'. Classifier and type are omitted if not present.", 
        example = "guava:33.4.8-jre"
    )
    private String id;

    @Schema(
        description = "A list of exclusions for this dependency, with each exclusion represented as a compact 'groupId:artifactId' string. This field will be null if there are no exclusions.",
        example = "[\"org.springframework.boot:spring-boot-starter-tomcat\"]"
    )
    private List<String> exclusions;
}