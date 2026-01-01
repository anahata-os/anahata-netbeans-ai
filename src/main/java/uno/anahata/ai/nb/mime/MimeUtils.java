/* Licensed under the Apache License, Version 2.0 */
package uno.anahata.ai.nb.mime;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 * Utility class for working with MIME types and file extensions in NetBeans.
 */
public final class MimeUtils {

    private MimeUtils() {
        // Utility class
    }

    /**
     * Scans the NetBeans System Filesystem's "Editors" folder to find all registered MIME types.
     * This is the most reliable method to get a complete list of MIME types the IDE is aware of.
     *
     * @return A Set of all registered MIME type strings.
     */
    public static Set<String> getAllMimeTypes() {
        Set<String> mimeTypes = new HashSet<>();
        FileObject editorsFolder = FileUtil.getConfigFile("Editors");
        if (editorsFolder != null) {
            // Recursively iterate through all folders
            Enumeration<? extends FileObject> e = editorsFolder.getFolders(true);
            while (e.hasMoreElements()) {
                FileObject mimeFolder = e.nextElement();
                // The relative path from the 'Editors' folder is the mime type
                String mimeType = FileUtil.getRelativePath(editorsFolder, mimeFolder);
                // We only care about folders that directly contain settings (files), not intermediate folders.
                boolean hasSettings = false;
                for (FileObject child : mimeFolder.getChildren()) {
                    if (child.isData()) {
                        hasSettings = true;
                        break;
                    }
                }
                if (hasSettings) {
                    mimeTypes.add(mimeType);
                }
            }
        }
        return mimeTypes;
    }
    
    /**
     * Scans the NetBeans System Filesystem to find all registered MIME types and their file extensions,
     * returning a map where the key is the extension and the value is the MIME type.
     * If an extension is associated with multiple MIME types, the last one found will be stored.
     *
     * @return A Map of file extensions to their corresponding MIME types.
     */
    public static Map<String, String> getExtensionToMimeTypeMap() {
        Map<String, String> extensionToMimeMap = new HashMap<>();
        Set<String> allMimeTypes = getAllMimeTypes();

        for (String mimeType : allMimeTypes) {
            List<String> extensions = FileUtil.getMIMETypeExtensions(mimeType);
            if (extensions != null) {
                for (String extension : extensions) {
                    extensionToMimeMap.put(extension, mimeType);
                }
            }
        }
        return extensionToMimeMap;
    }
}