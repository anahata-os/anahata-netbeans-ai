package uno.anahata.ai.nb.model.maven;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * A POJO representing a single search result from the Maven index.
 * @author Anahata
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MavenArtifactSearchResult {
    private String groupId;
    private String artifactId;
    private String version;
    private String repositoryId;
    private String packaging;
    private String description;
}
