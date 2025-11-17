package uno.anahata.ai.nb.tools;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.netbeans.modules.maven.indexer.api.NBVersionInfo;
import org.netbeans.modules.maven.indexer.api.QueryField;
import org.netbeans.modules.maven.indexer.api.RepositoryPreferences;
import org.netbeans.modules.maven.indexer.api.RepositoryQueries;
import org.netbeans.modules.maven.indexer.api.RepositoryQueries.Result;
import uno.anahata.gemini.functions.AIToolMethod;
import uno.anahata.gemini.functions.AIToolParam;
import uno.anahata.ai.nb.model.maven.MavenArtifactSearchResult;
import uno.anahata.ai.nb.model.maven.MavenSearchResultPage;

/**
 * Provides AI tool methods for searching the Maven index.
 * This tool uses the official NetBeans Maven Indexer API to perform comprehensive searches
 * across all configured repositories (local, remote, and project-specific).
 * 
 * @author pablo
 * @deprecated This class is deprecated as of 2025-11-16 and will be removed in a future version. 
 *             All functionality has been consolidated into {@link MavenTools}.
 */
@Deprecated
public class MavenSearch {

    /**
     * @deprecated Use {@link MavenTools#searchMavenIndex(String, Integer, Integer)} instead.
     */
    @AIToolMethod("Searches the Maven index for artifacts matching a given query. The search is performed across all configured repositories (local, remote, and project-specific).")
    @Deprecated
    public static MavenSearchResultPage searchMavenIndex(
            @AIToolParam("The search query, with terms separated by spaces (e.g., 'junit platform').") String query,
            @AIToolParam("The starting index (0-based) for pagination. Defaults to 0 if null.") Integer startIndex,
            @AIToolParam("The maximum number of results to return. Defaults to 100 if null.") Integer pageSize) throws Exception {
        
        if (query == null || query.isBlank()) {
            throw new IllegalArgumentException("Search query cannot be null or blank.");
        }
        
        String q = query.trim();
        
        // Apply defaults
        int start = startIndex != null ? startIndex : 0;
        int size = pageSize != null ? pageSize : 100;
        
        // The AddDependencyPanel logic splits the query by spaces to allow multi-keyword searches.
        String[] splits = q.split(" "); 

        List<QueryField> fields = new ArrayList<>();
        
        // Fields to search in the index
        List<String> fStrings = new ArrayList<>();
        fStrings.add(QueryField.FIELD_GROUPID);
        fStrings.add(QueryField.FIELD_ARTIFACTID);
        fStrings.add(QueryField.FIELD_VERSION);
        fStrings.add(QueryField.FIELD_NAME);
        fStrings.add(QueryField.FIELD_DESCRIPTION);
        fStrings.add(QueryField.FIELD_CLASSES);

        // For each word in the query, search all fields
        for (String curText : splits) {
            for (String fld : fStrings) {
                QueryField f = new QueryField();
                f.setField(fld);
                f.setValue(curText);
                f.setMatch(QueryField.MATCH_ANY);
                f.setOccur(QueryField.OCCUR_SHOULD);
                fields.add(f);
            }
        }

        // Perform the search against all configured repositories
        Result<NBVersionInfo> results = RepositoryQueries.findResult(fields, RepositoryPreferences.getInstance().getRepositoryInfos());

        // Wait for partial results to complete (if any)
        if (results.isPartial()) {
            results.waitForSkipped();
        }

        // Process and return the results with pagination
        if (results.getResults() == null) {
            return new MavenSearchResultPage(start, 0, Collections.emptyList());
        }
        
        List<NBVersionInfo> allResults = results.getResults();
        int totalCount = allResults.size();

        List<MavenArtifactSearchResult> page = allResults.stream()
                .skip(start)
                .limit(size)
                .map(info -> new MavenArtifactSearchResult(
                        info.getGroupId(),
                        info.getArtifactId(),
                        info.getVersion(),
                        info.getRepoId(),
                        info.getPackaging(),
                        info.getProjectDescription()
                ))
                .collect(Collectors.toList());
        
        return new MavenSearchResultPage(start, totalCount, page);
    }
}