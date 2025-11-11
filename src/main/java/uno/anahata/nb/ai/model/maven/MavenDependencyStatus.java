package uno.anahata.nb.ai.model.maven;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Represents the resolution status of a dependency in a Maven dependency tree.
 * @author pablo
 */
@Schema(description = "Represents the resolution status of a dependency in a Maven dependency tree.")
public enum MavenDependencyStatus {
    
    /**
     * The dependency was omitted due to a version conflict with another transitive dependency.
     */
    OMITTED_FOR_CONFLICT,
    
    /**
     * The dependency was omitted because it would have created a cyclical dependency.
     */
    OMITTED_FOR_CYCLE;
}
