package uno.anahata.ai.nb.model.java;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Represents a paginated result set from a Java package search.
 */
@Data
@AllArgsConstructor
@Schema(description = "Represents a paginated result set from a Java package search.")
public class PackageSearchResultPage {
    
    @Schema(description = "The starting index of the returned page (0-based).")
    private final int startIndex;
    
    @Schema(description = "The total number of matching packages found.")
    private final int totalCount;
    
    @Schema(description = "The list of fully qualified package names for the current page.")
    private final List<String> page;
}
