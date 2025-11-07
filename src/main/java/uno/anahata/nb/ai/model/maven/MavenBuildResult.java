package uno.anahata.nb.ai.model.maven;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MavenBuildResult {

    public enum ProcessStatus {
        COMPLETED,
        TIMEOUT,
        INTERRUPTED
    }

    private ProcessStatus status;
    private Integer exitCode; // Nullable, only present if status is COMPLETED
    private String stdOutput;
    private String stdError;
    private String logFile;
}
