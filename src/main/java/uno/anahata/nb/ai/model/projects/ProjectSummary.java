package uno.anahata.nb.ai.model.projects;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Represents a high-level, abstract summary of a project, focusing on its
 * key conceptual components rather than its file structure.
 * This is a simple, immutable data class.
 * 
 * @author Anahata
 */
@Schema(description = "Represents a high-level, abstract summary of a project, focusing on its key conceptual components.")
@Data
@AllArgsConstructor
public final class ProjectSummary {

    @Schema(description = "The project ID, which is typically the folder name.")
    private final String id;

    @Schema(description = "The human-readable display name of the project.")
    private final String displayName;

    @Schema(description = "The content of the anahata.md file for the project (if it exists). This file contains critical, high-level information and instructions for the project.")
    private final String anahataMdContent;

    @Schema(description = "A list of the key conceptual components within the project.")
    private final List<ProjectComponent> components;
}
