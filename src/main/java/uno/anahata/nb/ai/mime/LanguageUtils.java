package uno.anahata.nb.ai.mime;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.netbeans.api.lexer.Language;

public final class LanguageUtils {

    private LanguageUtils() {
        // Utility class
    }

    /**
     * Retrieves a list of all supported languages in the NetBeans IDE.
     * A language is considered supported if a Language instance is registered
     * for a known MIME type.
     *
     * @return A List of Language<?> objects.
     */
    public static List<Language<?>> getAllSupportedLanguages() {
        List<Language<?>> supportedLanguages = new ArrayList<>();
        Set<String> mimeTypes = MimeUtils.getAllMimeTypes();

        for (String mimeType : mimeTypes) {
            Language<?> language = Language.find(mimeType);
            if (language != null) {
                supportedLanguages.add(language);
            }
        }
        return supportedLanguages;
    }

    /**
     * Creates a map where the key is the language's unique identifier (its MIME type)
     * and the value is the corresponding MIME type.
     *
     * <p>Note: The Language class does not expose a simple 'name' property. The MIME type
     * itself serves as the unique identifier for the language in the Lexer API.</p>
     *
     * @return A Map<String, String> of language MIME types (identifier) to MIME types.
     */
    public static Map<String, String> getLanguageToMimeTypeMap() {
        Map<String, String> languageToMimeMap = new HashMap<>();
        Set<String> mimeTypes = MimeUtils.getAllMimeTypes();

        for (String mimeType : mimeTypes) {
            Language<?> language = Language.find(mimeType);
            if (language != null) {
                // FIX: Use mimeType() as the unique identifier, as getName() does not exist.
                String languageIdentifier = language.mimeType();
                languageToMimeMap.put(languageIdentifier, mimeType);
            }
        }
        return languageToMimeMap;
    }
}
