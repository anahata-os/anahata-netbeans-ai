/* Licensed under the Apache License, Version 2.0 */
package uno.anahata.ai.nb.model.projects;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Represents a key conceptual component of a project, like a major class or package.
 * This is a simple, immutable data class.
 * 
 * @author Anahata
 */
@Schema(description = "Represents a key conceptual component of a project, like a major class or package.")
@Data
@AllArgsConstructor
public final class ProjectComponent {

    @Schema(description = "The name of the component (e.g., a fully qualified class name or a package name).")
    private final String name;

    @Schema(description = "A brief description of the component's purpose, often derived from its Javadoc.")
    private final String description;
}