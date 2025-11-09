package uno.anahata.nb.ai.model.util;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class TextProcessResult {
    private final int totalLineCount;
    private final int matchingLineCount;
    private final String text;
}
