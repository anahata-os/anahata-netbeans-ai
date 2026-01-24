/* Licensed under the Apache License, Version 2.0 */
package uno.anahata.ai.nb.model.maven;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import uno.anahata.ai.tools.spi.pojos.TextChunk;

/**
 * Represents the detailed result of a Maven build execution, including status, exit code, and captured output.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Represents the detailed result of a Maven build execution, including status, exit code, and captured output.")
public class MavenBuildResult {

    /**
     * The final status of the Maven process.
     */
    @Schema(description = "The final status of the Maven process (e.g., COMPLETED, TIMEOUT).")
    public enum ProcessStatus {
        /** The process completed normally. */
        COMPLETED,
        /** The process timed out. */
        TIMEOUT,
        /** The process was interrupted. */
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

    @Schema(description = "A list of build phases executed during the Maven run, with their individual outcomes.")
    private List<BuildPhase> phases;

    /**
     * Represents a single phase or goal within a Maven build.
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Represents a single phase or goal within a Maven build.")
    public static class BuildPhase {
        @Schema(description = "The name of the phase (e.g., 'compile', 'test').")
        private String name;
        @Schema(description = "The plugin and goal associated with this phase.")
        private String plugin;
        @Schema(description = "Whether this phase succeeded or failed.")
        private boolean success;
        @Schema(description = "The duration of this phase in milliseconds.")
        private long durationMs;
    }
}
