package uno.anahata.nb.ai.model.projects;

import com.fasterxml.jackson.annotation.JsonInclude;
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
@Schema(description = "Represents a folder node within a source directory tree, containing a list of its own files and subfolders.")
@Data
@AllArgsConstructor
public final class SourceFolder {
    
    @Schema(description = "The display name of the source folder (e.g., 'Source Packages'). This is only included if it differs from the actual folder name.")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private final String displayName;

    @Schema(description = "The absolute path to the folder.")
    private final String path;

    @Schema(description = "The total size of all files within this folder and all its subfolders, calculated recursively.")
    private final long recursiveSize;

    @Schema(description = "A list of files contained directly within this folder.")
    private final List<ProjectFile> files;

    @Schema(description = "A list of subfolders contained directly within this folder.")
    private final List<SourceFolder> subfolders;
}
