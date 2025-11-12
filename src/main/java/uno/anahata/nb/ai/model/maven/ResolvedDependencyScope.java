package uno.anahata.nb.ai.model.maven;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * The top-level container for resolved dependencies, grouping them by scope.
 * This is the ultra-compact version of DependencyScope, designed to save tokens
 * by using ResolvedDependencyGroup which holds a List<String> instead of a List<DeclaredArtifact>.
 * @author pablo
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "A container that groups fully resolved dependencies by scope in an ultra-compact format.")
public class ResolvedDependencyScope {
    
    @Schema(description = "The dependency scope (e.g., compile, test, provided).", example = "compile")
    private String scope;
    
    @Schema(description = "The list of dependency groups belonging to this scope.")
    private List<ResolvedDependencyGroup> groups;
}
