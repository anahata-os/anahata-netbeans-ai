package uno.anahata.nb.ai.project.overview;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import uno.anahata.gemini.context.ResourceStatus;

/**
 * Represents a single file within a project, including its metadata and status.
 * This is a Java 8-compatible, immutable data class.
 *
 * @author Anahata
 */
@Schema(description = "Represents a single file within a project, including its metadata and its status in the conversation context.")
@Data
@AllArgsConstructor
public final class ProjectFile {

    @Schema(description = "The name of the file.")
    private final String name;

    @Schema(description = "The absolute path to the file.")
    private final String path;

    @Schema(description = "The size of the file in bytes.")
    private final long size;

    @Schema(description = "The last modified timestamp of the file on disk.")
    private final long lastModified;

    @Schema(description = "The status of the file relative to the conversation context (e.g., VALID, STALE, NOT_IN_CONTEXT).")
    private final ResourceStatus resourceStatus;
}
