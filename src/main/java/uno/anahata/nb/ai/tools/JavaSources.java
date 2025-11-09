package uno.anahata.nb.ai.tools;

import uno.anahata.gemini.functions.AIToolMethod;
import uno.anahata.gemini.functions.AIToolParam;
import uno.anahata.nb.ai.model.util.TextProcessResult;
import uno.anahata.nb.ai.util.NetBeansJavaQueryUtils;
import uno.anahata.nb.ai.util.TextUtils;

/**
 * Provides tools for retrieving Java source code.
 * @author Anahata
 */
public class JavaSources {
     
    @AIToolMethod(value = "Gets the source code of a Java file with pagination, filtering, and line truncation.", requiresApproval = false)
    public static TextProcessResult getSourceFileContent(
            @AIToolParam("The fully qualified name of the class.") String fqn,
            @AIToolParam("The starting line number (0-based) for pagination.") Integer startIndex,
            @AIToolParam("The number of lines to return.") Integer pageSize,
            @AIToolParam("A regex pattern to filter lines. Can be null or empty to return all lines.") String grepPattern,
            @AIToolParam("The maximum length of each line. Lines longer than this will be truncated. Set to 0 for no limit.") Integer maxLineLength) throws Exception {

        String content = NetBeansJavaQueryUtils.getSourceContent(fqn);
        if (content == null) {
            throw new IllegalStateException("Error: Source file not found for " + fqn);
        }

        return TextUtils.processText(content, startIndex, pageSize, grepPattern, maxLineLength);
    }
}
