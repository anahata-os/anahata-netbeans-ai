package uno.anahata.nb.ai.model.projects;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Represents a high-level overview of a project, including its root structure
 * and a detailed view of its source folders.
 * This is a Java 8-compatible, immutable data class.
 *
 * @author Anahata
 */
@Schema(description = "Represents a high-level, structured overview of a project, including its root files, folder names, and a detailed tree of its source code folders.")
@Data
@AllArgsConstructor
public final class ProjectOverview {

    @Schema(description = "The project ID, which is typically the folder name.")
    private final String id;

    @Schema(description = "The human-readable display name of the project.")
    private final String displayName;

    @Schema(description = "The absolute path to the project's root directory.")
    private final String projectDirectory;
    
    @Schema(description = "The content of the anahata.md file for the project (if it exists). This file contains **critical, high-level information** for you, your instructions for this project. Could also contain the project's purpose, status, and goals. It is your persistent memory for the project. Keep it up to date.")
    private final String anahataMdContent;

    @Schema(description = "A list of files located directly in the project's root directory.")
    private final List<ProjectFile> rootFiles;

    @Schema(description = "A list of the names of all folders located in the project's root directory.")
    private final List<String> rootFolderNames;

    @Schema(description = "A detailed, recursive tree structure of the project's primary source code folders.")
    private final List<SourceFolder> sourceFolders;

    @Schema(description = "A list of supported high-level NetBeans Project Actions that can be invoked on the Project (e.g., 'build', 'run').")
    private final List<String> actions;
    
}
