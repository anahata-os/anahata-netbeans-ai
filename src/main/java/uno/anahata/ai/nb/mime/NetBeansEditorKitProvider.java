/* Licensed under the Apache License, Version 2.0 */
package uno.anahata.ai.nb.mime;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.EditorKit;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import uno.anahata.ai.swing.render.editorkit.EditorKitProvider;
import uno.anahata.ai.nb.mime.DisabledModulesMimeUtils.MimeInfo;
import uno.anahata.ai.nb.mime.MimeUtils;

public class NetBeansEditorKitProvider implements EditorKitProvider {
    private static final Logger logger = Logger.getLogger(NetBeansEditorKitProvider.class.getName());

    private final Map<String, String> languageToMimeTypeMap;

    public NetBeansEditorKitProvider() {
        logger.log(Level.INFO, "Initializing NetBeansEditorKitProvider language cache...");
        this.languageToMimeTypeMap = new ConcurrentHashMap<>();

        // 1. Start with the hardcoded map as a baseline (the fallback/default).
        Map<String, String> hardcodedMap = Map.of(
            "java", "text/x-java",
            "xml", "text/xml",
            "html", "text/html",
            "css", "text/css",
            "javascript", "text/javascript",
            "json", "text/x-json",
            "sql", "text/x-sql",
            "properties", "text/x-properties",
            "bash", "text/plain" // Added explicit bash fallback
        );
        languageToMimeTypeMap.putAll(hardcodedMap);
        logger.log(Level.INFO, "Populated cache with {0} hardcoded languaget to mime type.", languageToMimeTypeMap.size());
        languageToMimeTypeMap.putAll(MimeUtils.getExtensionToMimeTypeMap());
        logger.log(Level.INFO, "After MimeUtils2.getExtensionToMimeTypeMap(): {0} ", languageToMimeTypeMap.size());
        /*
        logger.log(Level.INFO, "Populated cache with {0} hardcoded fallbacks.", languageToMimeTypeMap.size());

        // 2. Override with MIME types from ENABLED modules (the reliable scan).
        List<MimeInfo> activeMimes = DisabledModulesMimeUtils.getMimeTypesFromActiveModules();
        int enabledCount = 0;
        for (MimeInfo info : activeMimes) {
            String primaryExtension = info.getPrimaryExtension();
            if (primaryExtension != null) {
                logger.log(Level.INFO, "Overriding with active module: {0} -> {1}", new Object[]{primaryExtension, info.mimeType});
                languageToMimeTypeMap.put(primaryExtension, info.mimeType);
                enabledCount++;
            }
        }
        logger.log(Level.INFO, "Discovered and overwrote with {0} MIME types from enabled modules.", enabledCount);

        // 3. Log information about potential MIME types in DISABLED modules.
*/
        //logMimeTypesFromDisabledModules();


        logger.log(Level.INFO, "Cache initialization complete. Final cache size: {0}", languageToMimeTypeMap.size());
    }

    @Override
    public EditorKit getEditorKitForLanguage(String language) {
        String langLower = (language == null) ? "" : language.toLowerCase().trim();
        String mimeType = languageToMimeTypeMap.get(langLower);

        if (mimeType != null) {
            EditorKit kit = MimeLookup.getLookup(mimeType).lookup(EditorKit.class);
            if (kit != null) {
                return kit;
            }
        }
        return null;
    }
}