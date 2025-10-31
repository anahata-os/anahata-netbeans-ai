package uno.anahata.nb.ai.project.overview;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Represents a folder within a source directory tree, containing files and subfolders.
 * This is a Java 8-compatible, immutable data class.
 *
 * @author Anahata
 */
@Schema(description = "Represents a folder within a source directory tree, containing a list of its own files and subfolders.")
@Data
@AllArgsConstructor
public final class SourceFolder {

    @Schema(description = "The name of the folder.")
    private final String name;

    @Schema(description = "The absolute path to the folder.")
    private final String path;

    @Schema(description = "The total size of all files within this folder and all its subfolders, calculated recursively.")
    private final long recursiveSize;

    @Schema(description = "A list of files contained directly within this folder.")
    private final List<ProjectFile> files;

    @Schema(description = "A list of subfolders contained directly within this folder.")
    private final List<SourceFolder> subfolders;
}
