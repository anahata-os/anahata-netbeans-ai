/* Licensed under the Apache License, Version 2.0 */
package uno.anahata.ai.nb.model.java;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Represents a paginated result set from a Java type search within a package.
 */
@Data
@AllArgsConstructor
@Schema(description = "Represents a paginated result set from a Java type search within a package.")
public class TypeSearchResultPage {
    
    @Schema(description = "The starting index of the returned page (0-based).")
    private final int startIndex;
    
    @Schema(description = "The total number of matching types found in the package.")
    private final int totalCount;
    
    @Schema(description = "The list of TypeInfo objects for the current page.")
    private final List<TypeInfo> page;
}
