package uno.anahata.nb.ai;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JEditorPane;
import javax.swing.text.Document;
import javax.swing.text.EditorKit;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.TokenHierarchy;
import uno.anahata.gemini.ui.render.editorkit.EditorKitProvider;

public class NetBeansEditorKitProvider implements EditorKitProvider {
    private static final Logger logger = Logger.getLogger(NetBeansEditorKitProvider.class.getName());

    // Use a static map as a clean, extensible, hardcoded lookup table.
    private static final Map<String, String> LANGUAGE_TO_MIME_TYPE_MAP;

    static {
        Map<String, String> map = new HashMap<>();
        map.put("java", "text/x-java");
        map.put("xml", "text/xml");
        map.put("html", "text/html");
        map.put("css", "text/css");
        map.put("javascript", "text/javascript");
        map.put("json", "text/json");
        map.put("sql", "text/x-sql");
        map.put("properties", "text/x-properties");
        // This map can be extended as needed.
        LANGUAGE_TO_MIME_TYPE_MAP = Collections.unmodifiableMap(map);
    }
    
    //@Override
    public String getMimeTypeForLanguage(String language) {
        return LANGUAGE_TO_MIME_TYPE_MAP.get(language);
    }

    @Override
    public EditorKit getEditorKitForLanguage(String language) {
        if (language == null || language.trim().isEmpty()) {
            return null;
        }
        String langLower = language.toLowerCase().trim();
        String mimeType = LANGUAGE_TO_MIME_TYPE_MAP.get(langLower);

        if (mimeType != null) {
            logger.log(Level.INFO, "Found MIME type ''{0}'' for language ''{1}''", new Object[]{mimeType, language});
            // Use MimeLookup to get the NetBeans-specific EditorKit for syntax highlighting
            EditorKit kit = MimeLookup.getLookup(mimeType).lookup(EditorKit.class);
            //EditorKit kit = JEditorPane.createEditorKitForContentType(mimeType);
            if (kit != null) {
                logger.log(Level.INFO, "Found EditorKit for mime type: {0}: {1}", new Object[]{mimeType, kit});
                return kit;
            } else {
                logger.log(Level.WARNING, "Could not find an EditorKit for mime type: {0}", mimeType);
            }
        } else {
            logger.log(Level.WARNING, "Could not find a registered MIME type for language: {0}", language);
        }
        return null;
    }


}
