/* Licensed under the Apache License, Version 2.0 */
package uno.anahata.ai.nb.model.maven;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

/**
 * Represents the detailed, multi-phase result of the {@code MavenPom.addDependency} "super-tool".
 * This structured object provides a clear, programmatic way to inspect the outcome of each step in the process,
 * leaving the final interpretation of success or failure to the consumer (the AI model).
 */
@Getter
@Builder
@Schema(description = "Represents the detailed, multi-phase result of the MavenPom.addDependency tool.")
public class AddDependencyResult {

    @Schema(description = "Indicates if the initial pre-flight check to download the main artifact was successful.")
    private final boolean preflightCheckSuccess;

    @Schema(description = "Indicates if the pom.xml file was successfully modified.")
    private final boolean pomModificationSuccess;

    @Schema(description = "Contains the complete result of the 'dependency:resolve' Maven goal, including exit code, output, and a path to the full log file. This will be null if prior steps failed.")
    private final MavenBuildResult dependencyResolveResult;

    @Schema(description = "Indicates if the asynchronous background task to download sources and javadocs was launched.")
    private final boolean asyncDownloadsLaunched;

    @Schema(description = "A final, human-readable summary of the entire operation, intended for display.")
    private final String summary;
}
