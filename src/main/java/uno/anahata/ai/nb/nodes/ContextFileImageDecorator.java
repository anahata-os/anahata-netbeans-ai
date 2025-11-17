package uno.anahata.ai.nb.nodes;

import java.awt.Image;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.ImageDecorator;
import org.openide.util.ImageUtilities;
import org.openide.util.lookup.ServiceProvider;

@ServiceProvider(service = ImageDecorator.class, position = 100)
public class ContextFileImageDecorator implements ImageDecorator, PropertyChangeListener {
    private static final Logger log = Logger.getLogger(ContextFileImageDecorator.class.getName());
    private static final Image ANNOTATION_ICON = ImageUtilities.loadImage("uno/anahata/nb/ai/nodes/anahata_16.png", true);

    public ContextFileImageDecorator() {
        log.info("ENTRY ContextFileImageDecorator()");
        ContextFiles.getInstance().addPropertyChangeListener(this);
        log.info("EXIT ContextFileImageDecorator()");
    }

    @Override
    public Image annotateIcon(Image icon, int type, Set<? extends FileObject> files) {
        final String fileNames = files.stream().map(FileObject::getNameExt).collect(Collectors.joining(", "));
        log.log(Level.INFO, "ENTRY annotateIcon(icon={0}, type={1}, files=[{2}])", new Object[]{icon, type, fileNames});
        
        if (files.size() != 1) {
            log.log(Level.INFO, "EXIT annotateIcon: Returning original icon because file count is not 1.");
            return icon;
        }
        FileObject fo = files.iterator().next();
        File file = FileUtil.toFile(fo);

        if (file != null && ContextFiles.getInstance().contains(file)) {
            log.log(Level.INFO, "Decorating icon for: {0}", fo.getNameExt());
            Image merged = ImageUtilities.mergeImages(icon, ANNOTATION_ICON, 8, 8);
            log.log(Level.INFO, "EXIT annotateIcon: Returning decorated icon for {0}", file.getName());
            return merged;
        }
        log.log(Level.INFO, "EXIT annotateIcon: Returning original icon for {0}", file != null ? file.getName() : "null file");
        return icon;
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        log.log(Level.INFO, "ENTRY propertyChange(evt={0})", evt);
        if (ContextFiles.CONTEXT_FILES_PROPERTY.equals(evt.getPropertyName())) {
            log.info("Received context files change event.");

            Set<String> oldFiles = (Set<String>) evt.getOldValue();
            Set<String> newFiles = (Set<String>) evt.getNewValue();

            Set<String> changedPaths = new HashSet<>();
            changedPaths.addAll(oldFiles);
            changedPaths.addAll(newFiles);

            for (String path : changedPaths) {
                FileObject fo = FileUtil.toFileObject(new File(path));
                if (fo != null) {
                    try {
                        fo.setAttribute("inAiContext", newFiles.contains(path));
                        log.log(Level.INFO, "Fired attribute change for: {0}", path);
                    } catch (IOException e) {
                        log.log(Level.SEVERE, "Could not fire attribute change for: {0}", e);
                    }
                }
            }
        }
        log.log(Level.INFO, "EXIT propertyChange(evt={0})", evt);
    }
}
