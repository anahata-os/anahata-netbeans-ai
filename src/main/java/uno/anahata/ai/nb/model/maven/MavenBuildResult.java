package uno.anahata.ai.nb.model.maven;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import uno.anahata.ai.tools.spi.pojos.TextChunk;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Represents the detailed result of a Maven build execution, including status, exit code, and captured output.")
public class MavenBuildResult {

    @Schema(description = "The final status of the Maven process (e.g., COMPLETED, TIMEOUT).")
    public enum ProcessStatus {
        COMPLETED,
        TIMEOUT,
        INTERRUPTED
    }

    @Schema(description = "The final status of the Maven process.")
    private ProcessStatus status;

    @Schema(description = "The exit code of the Maven process. Note: This can be unreliable in some execution environments; always check the stdout for 'BUILD SUCCESS'.")
    private Integer exitCode;

    @Schema(description = "A chunk of the standard output stream, typically the tail end of the log.")
    private TextChunk stdOutput;

    @Schema(description = "A chunk of the standard error stream.")
    private TextChunk stdError;

    @Schema(description = "The absolute path to the full, untruncated log file saved on disk.")
    private String logFile;
}
