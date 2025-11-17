package uno.anahata.ai.nb.mime;

import java.io.File;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.modules.ModuleInfo;
import org.openide.util.Lookup;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import uno.anahata.ai.nb.util.NetBeansModuleUtils;

public final class DisabledModulesMimeUtils {

    private static final Logger logger = Logger.getLogger(DisabledModulesMimeUtils.class.getName());

    private DisabledModulesMimeUtils() {
        // Utility class
    }

    public static class MimeInfo {
        public final String mimeType;
        public final String moduleCodeName;
        public final boolean enabled;

        public MimeInfo(String mime, String codeName, boolean enabled) {
            this.mimeType = mime;
            this.moduleCodeName = codeName;
            this.enabled = enabled;
        }
        
        /**
         * Gets the primary (first) file extension associated with this MIME type.
         * @return The primary extension, or null if none is found.
         */
        public String getPrimaryExtension() {
            List<String> extensions = FileUtil.getMIMETypeExtensions(mimeType);
            return (extensions != null && !extensions.isEmpty()) ? extensions.get(0) : null;
        }
        
        @Override
        public String toString() {
            return String.format("%s [%s] %s", mimeType, moduleCodeName, enabled ? "ENABLED" : "DISABLED");
        }
    }

    
    /**
     * Uses reflection to scan all module layer.xml files for DISABLED modules
     * to find potential language support.
     *
     * @return A list of MimeInfo objects for disabled modules.
     */
    public static List<MimeInfo> getMimeTypesFromDisabledModules() {
        List<MimeInfo> results = new ArrayList<>();
        Collection<? extends ModuleInfo> modules = Lookup.getDefault().lookupAll(ModuleInfo.class);

        for (ModuleInfo mi : modules) {
            boolean enabled = mi.isEnabled();
            if (enabled) continue; // Only process disabled modules

            List<File> jarFiles = NetBeansModuleUtils.getAllModuleJarsUsingReflection(mi);
            if (jarFiles.isEmpty()) continue;

            for (File jarFile : jarFiles) {
                FileObject jarFo = FileUtil.toFileObject(jarFile);
                if (jarFo == null) continue;

                FileObject layerFo = jarFo.getFileObject("layer.xml");
                if (layerFo == null) continue;

                try (InputStream is = layerFo.getInputStream()) {
                    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
                    DocumentBuilder db = dbf.newDocumentBuilder();
                    Document doc = db.parse(is);
                    doc.getDocumentElement().normalize();

                    List<String> mimes = extractEditorLanguagePaths(doc);
                    for (String mime : mimes) {
                        results.add(new MimeInfo(mime, mi.getCodeNameBase(), enabled));
                    }
                } catch (Exception ex) {
                    logger.log(Level.FINE, "Failed to parse layer.xml for " + mi.getCodeNameBase(), ex);
                }
            }
        }

        return results;
    }

    public static List<String> extractEditorLanguagePaths(Document doc) {
        List<String> mimes = new ArrayList<>();
        NodeList files = doc.getElementsByTagName("file");
        for (int i = 0; i < files.getLength(); i++) {
            Element file = (Element) files.item(i);
            String name = file.getAttribute("name");
            
            if ("language.instance".equals(name)) {
                Element parent = (Element) file.getParentNode();
                String parentName = parent.getAttribute("name");
                
                StringBuilder pathBuilder = new StringBuilder(parentName);
                Element current = parent;
                while (current.getParentNode() instanceof Element) {
                    current = (Element) current.getParentNode();
                    String folderName = current.getAttribute("name");
                    if (folderName != null && !folderName.isEmpty()) {
                        pathBuilder.insert(0, folderName + "/");
                    }
                }
                
                String fullPath = pathBuilder.toString();
                if (fullPath.startsWith("Editors/")) {
                    // Convert path to MIME type (e.g., Editors/text/x-java -> text/x-java)
                    String mime = fullPath.substring("Editors/".length()).replace('/', '-');
                    mimes.add(mime);
                }
            }
        }
        return mimes;
    }
}
