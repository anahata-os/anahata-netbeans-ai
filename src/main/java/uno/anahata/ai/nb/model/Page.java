/* Licensed under the Apache License, Version 2.0 */
package uno.anahata.ai.nb.model;

import java.util.List;
import java.util.stream.Collectors;
import lombok.Getter;

/**
 * A generic, self-contained container for a paginated result set.
 * It encapsulates the logic for slicing a larger list into a single page.
 * @param <T> The type of the items in the page.
 */
@Getter
public class Page<T> {
    /** The starting index of the returned page (0-based). */
    private final int startIndex;
    /** The total number of items available across all pages. */
    private final int totalCount;
    /** The number of items requested for the page. */
    private final int pageSize;
    /** The list of items for the current page. */
    private final List<T> page;

    /**
     * Creates a new Page object by slicing a complete list of items.
     * @param allItems The complete list of items to be paginated.
     * @param startIndex The starting index (0-based) for the page.
     * @param pageSize The maximum number of items for the page.
     */
    public Page(List<T> allItems, int startIndex, int pageSize) {
        this.totalCount = allItems.size();
        this.startIndex = startIndex;
        this.pageSize = pageSize;
        this.page = allItems.stream()
                .skip(startIndex)
                .limit(pageSize)
                .collect(Collectors.toList());
    }
}