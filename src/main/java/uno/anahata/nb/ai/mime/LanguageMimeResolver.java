/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package uno.anahata.nb.ai.mime;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.MIMEResolver;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.openide.util.Lookup;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;
import org.netbeans.api.editor.mimelookup.MimePath;
import java.lang.reflect.Method;  // For internal FileType access (stable but non-public)
import java.util.HashMap;
import java.util.Map;

/**
 * Utility to get MIME type for a language without hardcoding.
 */
public class LanguageMimeResolver {
// Minimal mapping of language names to primary file extensions (not MIME types).
    // This is maintainable and avoids reflection. Extend as needed for other languages.
    private static final Map<String, String> LANGUAGE_TO_EXTENSION = new HashMap<>();
    static {
        LANGUAGE_TO_EXTENSION.put("java", "java");
        LANGUAGE_TO_EXTENSION.put("xml", "xml");
        LANGUAGE_TO_EXTENSION.put("python", "py"); // Requires Python plugin
        // Add more mappings for other languages (e.g., "kotlin" → "kt") as needed.
    }

    /**
     * Gets the MIME type for a given language name (e.g., "java" → "text/x-java").
     * @param languageName The language name (case-insensitive, e.g., "Java", "xml", "Python").
     * @return The MIME type, or null if the language is not recognized or supported.
     */
    public static String getMimeTypeForLanguage(String languageName) {
        if (languageName == null || languageName.trim().isEmpty()) {
            return null;
        }

        // Normalize input to lowercase for case-insensitive matching.
        String normalizedLang = languageName.trim().toLowerCase();

        // Get the file extension for the language.
        String extension = LANGUAGE_TO_EXTENSION.get(normalizedLang);
        if (extension == null) {
            return null; // Language not recognized.
        }

        // Resolve MIME type using the extension.
        return resolveMimeTypeFromExtension("dummy." + extension);
    }

    /**
     * Resolves MIME type from a file extension using public FileUtil API.
     * @param fileNameWithExt A filename with the extension (e.g., "dummy.java").
     * @return The MIME type, or null if not resolved.
     */
    private static String resolveMimeTypeFromExtension(String fileNameWithExt) {
        try {
            // Create a temporary FileObject in memory (extension drives MIME resolution).
            FileObject tempFo = FileUtil.createMemoryFileSystem().getRoot().createData(fileNameWithExt);
            String mimeType = FileUtil.getMIMEType(tempFo); // Public, strongly typed API.
            tempFo.delete(); // Cleanup.
            return mimeType;
        } catch (IOException e) {
            // Handle error (e.g., log it in production).
            return null;
        }
    }
}