/* Licensed under the Apache License, Version 2.0 */
package uno.anahata.ai.nb.model.maven;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * The top-level container that groups dependency groups by their scope.
 * @author pablo
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "A container that groups dependency information by scope.")
public class DependencyScope {
    
    @Schema(description = "The dependency scope (e.g., compile, test, provided).", example = "compile")
    private String scope;
    
    @Schema(description = "The list of dependency groups belonging to this scope.")
    private List<DependencyGroup> groups;
}
