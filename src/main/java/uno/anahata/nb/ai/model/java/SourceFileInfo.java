package uno.anahata.nb.ai.model.java;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import uno.anahata.gemini.functions.spi.pojos.FileInfo;
import uno.anahata.nb.ai.model.util.TextChunk;

@Getter
@Schema(
    description = "Extends FileInfo to provide rich, source-code-specific context, including its origin (project, JAR, or JDK) and safe, paginated content access. " +
                  "The output of a tool returning this object will be deemed ephemeral if the source file is not in one of the open projects or if its content has been truncated (paginated). " +
                  "This is enforced by the getResourceId() method, which returns null in those cases, preventing the ResourceTracker from tracking it."
)
public class SourceFileInfo extends FileInfo {

    @Schema(description = "The type of origin for the source file (e.g., from an open project, a dependency JAR, or the JDK).")
    private final SourceOrigin originType;

    @Schema(description = "The location of the origin, such as the project name, the path to the JAR file, or the JDK version.")
    private final String originLocation;

    @Schema(description = "A chunk of the file's content, including pagination and truncation metadata. This field is populated ONLY if the file content is partial. If this field is null, the 'content' field from the parent FileInfo class holds the full file content.")
    private final TextChunk chunk;

    public SourceFileInfo(
            String path,
            String content,
            long contentLines,
            long lastModified,
            long size,
            SourceOrigin originType,
            String originLocation,
            TextChunk chunk) {
        super(path, content, contentLines, lastModified, size);
        this.originType = originType;
        this.originLocation = originLocation;
        this.chunk = chunk;
    }

    /**
     * Overridden to enforce safety. A SourceFileInfo is only considered a stateful,
     * trackable resource if it represents the *full* content of a file that
     * belongs to an open project. In all other cases (e.g., from a JAR, from
     * the JDK, or a partial/paginated view), it returns null, making it
     * ephemeral to the ResourceTracker.
     *
     * @return The file path if the resource is stateful, otherwise null.
     */
    @Override
    public String getResourceId() {
        boolean isFromProject = originType == SourceOrigin.PROJECT;
        boolean isFullContent = chunk == null;

        if (isFromProject && isFullContent) {
            return getPath();
        } else {
            return null;
        }
    }
}
