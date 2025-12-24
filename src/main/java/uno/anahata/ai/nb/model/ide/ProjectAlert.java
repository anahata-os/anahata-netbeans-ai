/* Licensed under the Apache License, Version 2.0 */
package uno.anahata.ai.nb.model.ide;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ProjectAlert {
    private final String displayName;
    private final String description;
    private final String category;
    private final String severity;
    private final boolean resolvable;
}