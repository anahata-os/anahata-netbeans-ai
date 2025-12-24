/* Licensed under the Apache License, Version 2.0 */
package uno.anahata.ai.nb.model.java;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Represents a paginated result set for the members of a Java type.
 */
@Data
@AllArgsConstructor
@Schema(description = "Represents a paginated result set for the members of a Java type.")
public class MemberSearchResultPage {
    
    @Schema(description = "The starting index of the returned page (0-based).")
    private final int startIndex;
    
    @Schema(description = "The total number of members found for the type.")
    private final int totalCount;
    
    @Schema(description = "The list of MemberInfo objects for the current page.")
    private final List<MemberInfo> page;
}