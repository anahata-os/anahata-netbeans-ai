package uno.anahata.nb.ai.util;

import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import uno.anahata.nb.ai.model.util.TextProcessResult;

public class TextUtils {

    /**
     * Processes a block of text with pagination, filtering, and line truncation.
     *
     * @param text The full text content to process.
     * @param startIndex The starting line number (0-based) for pagination. Can be null.
     * @param pageSize The number of lines to return. Can be null for no limit.
     * @param grepPattern A regex pattern to filter lines. Can be null or empty.
     * @param maxLineLength The maximum length of each line. Lines longer than this will be truncated. Can be null or 0 for no limit.
     * @return A TextProcessResult object containing metadata and the processed text.
     */
    public static TextProcessResult processText(String text, Integer startIndex, Integer pageSize, String grepPattern, Integer maxLineLength) {
        if (text == null || text.isEmpty()) {
            return new TextProcessResult(0, 0, "");
        }

        // Handle nulls and provide sensible defaults
        int start = (startIndex == null || startIndex < 0) ? 0 : startIndex;
        int size = (pageSize == null || pageSize <= 0) ? Integer.MAX_VALUE : pageSize;
        int maxLen = (maxLineLength == null || maxLineLength <= 0) ? 0 : maxLineLength;

        List<String> allLines = text.lines().collect(Collectors.toList());
        int totalLineCount = allLines.size();

        List<String> filteredLines;
        if (grepPattern != null && !grepPattern.trim().isEmpty()) {
            Pattern pattern = Pattern.compile(grepPattern);
            filteredLines = allLines.stream()
                    .filter(line -> pattern.matcher(line).matches())
                    .collect(Collectors.toList());
        } else {
            filteredLines = allLines;
        }
        int matchingLineCount = filteredLines.size();

        String processedText = filteredLines.stream()
                .skip(start)
                .limit(size)
                .map(line -> truncateLine(line, maxLen))
                .collect(Collectors.joining("\n"));

        return new TextProcessResult(totalLineCount, matchingLineCount, processedText);
    }

    private static String truncateLine(String line, int maxLineLength) {
        if (maxLineLength > 0 && line.length() > maxLineLength) {
            int originalLength = line.length();
            return line.substring(0, maxLineLength)
                    + " [ANAHATA][line truncated at " + maxLineLength
                    + " characters, " + (originalLength - maxLineLength) + " more]";
        }
        return line;
    }
}
