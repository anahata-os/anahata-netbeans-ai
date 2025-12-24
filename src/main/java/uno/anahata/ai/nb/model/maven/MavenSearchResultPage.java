/* Licensed under the Apache License, Version 2.0 */
package uno.anahata.ai.nb.model.maven;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Represents a paginated result set from a Maven index search.
 * 
 * @author pablo
 */
@Data
@AllArgsConstructor
public class MavenSearchResultPage {
    
    private final int startIndex;
    private final int totalCount;
    private final List<MavenArtifactSearchResult> page;
}