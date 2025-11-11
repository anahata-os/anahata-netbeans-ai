package uno.anahata.nb.ai.model.util;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class TextChunk {
    private final int totalLineCount;
    private final int matchingLineCount;
    private final int fromLine;
    private final int toLine;
    private final int truncatedLinesCount;
    private final String text;
}
