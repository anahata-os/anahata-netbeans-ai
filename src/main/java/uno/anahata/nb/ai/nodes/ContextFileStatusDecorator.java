package uno.anahata.nb.ai.nodes;

import java.net.URL;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.StatusDecorator;
import org.openide.util.lookup.ServiceProvider;
import static uno.anahata.nb.ai.context.ContextFiles.ATTR_IN_AI_CONTEXT;

@ServiceProvider(service = StatusDecorator.class, position = 900)
public class ContextFileStatusDecorator implements StatusDecorator {

    private static final Logger log = Logger.getLogger(ContextFileStatusDecorator.class.getName());
    // Standardized path, loaded from the classpath root.
    private static final URL ICON_URL = ContextFileStatusDecorator.class.getResource("/icons/anahata_16.png");

    public ContextFileStatusDecorator() {
        log.info("Initializing ContextFileStatusDecorator");
        if (ICON_URL == null) {
            log.warning("Icon resource '/icons/anahata_16.png' not found!");
        }
    }

    @Override
    public String annotateNameHtml(String name, Set<? extends FileObject> files) {
        log.log(Level.INFO, "annotateNameHtml called for name: ''{0}'' on file: {1}", new Object[]{name, files});
        if (files == null || files.size() != 1) {
            return name;
        }
        FileObject fo = files.iterator().next();
        log.log(Level.INFO, "annotateNameHtml called for name: ''{0}'' on file object: {1}", new Object[]{name, fo != null ? fo.getNameExt() : "null"});
        
        if (fo == null) {
            return name;
        }

        if (Boolean.TRUE.equals(fo.getAttribute(ATTR_IN_AI_CONTEXT))) {
            log.log(Level.INFO, "File is in context (via attribute): {0}", fo.getNameExt());
            if (ICON_URL != null) {
                String annotatedName = "<html><img src=\"" + ICON_URL + "\">&nbsp;" + name;
                log.log(Level.INFO, "Annotating with icon: {0}", annotatedName);
                return annotatedName;
            } else {
                log.warning("ICON_URL is null, annotating with text fallback.");
                return "<html>" + name + " <font color='blue'>[Context]</font></html>";
            }
        }
        return name;
    }

    @Override
    public String annotateName(String name, Set<? extends FileObject> files) {
        if (files == null || files.size() != 1) {
            return name;
        }
        FileObject fo = files.iterator().next();
        log.log(Level.INFO, "annotateName called for name: ''{0}'' on file: {1}", new Object[]{name, fo != null ? fo.getNameExt() : "null"});

        if (fo == null) {
            return name;
        }
        
        if (Boolean.TRUE.equals(fo.getAttribute(ATTR_IN_AI_CONTEXT))) {
            return "[C] " + name;
        }
        return name;
    }
}
