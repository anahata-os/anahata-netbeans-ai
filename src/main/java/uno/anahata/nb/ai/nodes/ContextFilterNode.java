package uno.anahata.nb.ai.nodes;

import java.awt.Image;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.filesystems.FileObject;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;
import org.openide.util.ImageUtilities;
import uno.anahata.nb.ai.context.ContextFiles;

public class ContextFilterNode extends FilterNode implements PropertyChangeListener {
    private static final Logger log = Logger.getLogger(ContextFilterNode.class.getName());
    private static final Image ANNOTATION_ICON = ImageUtilities.loadImage("uno/anahata/nb/ai/nodes/anahata_16.png", true);

    private final FileObject fileObject;

    public ContextFilterNode(Node original) {
        super(original, new FilterNode.Children(original));
        log.log(Level.INFO, "ENTRY ContextFilterNode(original={0})", original.getName());
        this.fileObject = original.getLookup().lookup(FileObject.class);
        if (this.fileObject != null) {
            ContextFiles.getInstance().addPropertyChangeListener(this);
            log.log(Level.INFO, "Attached listener to node: {0}", fileObject.getNameExt());
        } else {
            log.warning("ContextFilterNode created for a node without a FileObject in its lookup.");
        }
        log.log(Level.INFO, "EXIT ContextFilterNode(original={0})", original.getName());
    }

    private boolean isFileInContext() {
        log.log(Level.INFO, "ENTRY isFileInContext()");
        if (fileObject == null) {
            log.log(Level.INFO, "EXIT isFileInContext(): false (fileObject is null)");
            return false;
        }
        File file = new File(fileObject.getPath());
        boolean result = ContextFiles.getInstance().contains(file);
        log.log(Level.INFO, "EXIT isFileInContext(): {0} for file {1}", new Object[]{result, file.getName()});
        return result;
    }

    @Override
    public Image getIcon(int type) {
        log.log(Level.INFO, "ENTRY getIcon(type={0}) for {1}", new Object[]{type, fileObject != null ? fileObject.getNameExt() : "unknown"});
        Image originalIcon = super.getIcon(type);
        if (isFileInContext()) {
            log.log(Level.INFO, "Decorating icon for: {0}", fileObject.getNameExt());
            Image merged = ImageUtilities.mergeImages(originalIcon, ANNOTATION_ICON, 8, 8);
            log.log(Level.INFO, "EXIT getIcon: Returning decorated icon.");
            return merged;
        }
        log.log(Level.INFO, "EXIT getIcon: Returning original icon.");
        return originalIcon;
    }
    
    @Override
    public Image getOpenedIcon(int type) {
        log.log(Level.INFO, "ENTRY getOpenedIcon(type={0}) for {1}", new Object[]{type, fileObject != null ? fileObject.getNameExt() : "unknown"});
        Image originalIcon = super.getOpenedIcon(type);
        if (isFileInContext()) {
            log.log(Level.INFO, "Decorating opened icon for: {0}", fileObject.getNameExt());
            Image merged = ImageUtilities.mergeImages(originalIcon, ANNOTATION_ICON, 8, 8);
            log.log(Level.INFO, "EXIT getOpenedIcon: Returning decorated icon.");
            return merged;
        }
        log.log(Level.INFO, "EXIT getOpenedIcon: Returning original icon.");
        return originalIcon;
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        log.log(Level.INFO, "ENTRY propertyChange(evt={0}) for {1}", new Object[]{evt, fileObject != null ? fileObject.getNameExt() : "unknown"});
        if (ContextFiles.CONTEXT_FILES_PROPERTY.equals(evt.getPropertyName()) && fileObject != null) {
            log.log(Level.INFO, "Received context files change event for node: {0}", fileObject.getNameExt());
            fireIconChange();
            fireOpenedIconChange();
            fireDisplayNameChange(null, getDisplayName());
        }
        log.log(Level.INFO, "EXIT propertyChange(evt={0})", evt);
    }

    @Override
    public void destroy() throws IOException {
        log.log(Level.INFO, "ENTRY destroy() for {0}", fileObject != null ? fileObject.getNameExt() : "unknown");
        if (fileObject != null) {
            ContextFiles.getInstance().removePropertyChangeListener(this);
            log.log(Level.INFO, "Detached listener from node: {0}", fileObject.getNameExt());
        }
        super.destroy();
        log.log(Level.INFO, "EXIT destroy()");
    }
}
