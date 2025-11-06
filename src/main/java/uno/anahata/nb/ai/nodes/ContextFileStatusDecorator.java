package uno.anahata.nb.ai.nodes;

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
import org.openide.filesystems.StatusDecorator;
import org.openide.util.lookup.ServiceProvider;

//@ServiceProvider(service = StatusDecorator.class, position = 100)
public class ContextFileStatusDecorator implements StatusDecorator, PropertyChangeListener {
    private static final Logger log = Logger.getLogger(ContextFileStatusDecorator.class.getName());

    public ContextFileStatusDecorator() {
        log.info("ENTRY ContextFileStatusDecorator()");
        ContextFiles.getInstance().addPropertyChangeListener(this);
        log.info("EXIT ContextFileStatusDecorator()");
    }

    @Override
    public String annotateName(String name, Set<? extends FileObject> files) {
        final String fileNames = files.stream().map(FileObject::getNameExt).collect(Collectors.joining(", "));
        log.log(Level.INFO, "ENTRY annotateName(name={0}, files=[{1}])", new Object[]{name, fileNames});
        String result = annotateNameHtml(name, files);
        log.log(Level.INFO, "EXIT annotateName: Returning ''{0}''", result);
        return result;
    }

    @Override
    public String annotateNameHtml(String name, Set<? extends FileObject> files) {
        final String fileNames = files.stream().map(FileObject::getNameExt).collect(Collectors.joining(", "));
        log.log(Level.INFO, "ENTRY annotateNameHtml(name={0}, files=[{1}])", new Object[]{name, fileNames});
        
        if (files.size() != 1) {
            log.log(Level.INFO, "EXIT annotateNameHtml: Returning original name because file count is not 1.");
            return name;
        }

        FileObject fo = files.iterator().next();
        File file = FileUtil.toFile(fo);

        if (file != null && ContextFiles.getInstance().contains(file)) {
            log.log(Level.INFO, "Annotating name for: {0}", fo.getNameExt());
            String annotatedName = "<html><font color=''!Tree.foreground''>[AI] </font>" + name + "</html>";
            log.log(Level.INFO, "EXIT annotateNameHtml: Returning annotated name ''{0}'' for {1}", new Object[]{annotatedName, file.getName()});
            return annotatedName;
        }
        
        log.log(Level.INFO, "EXIT annotateNameHtml: Returning original name ''{0}'' for {1}", new Object[]{name, file != null ? file.getName() : "null file"});
        return name;
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        log.log(Level.INFO, "ENTRY propertyChange(evt={0})", evt);
        if (ContextFiles.CONTEXT_FILES_PROPERTY.equals(evt.getPropertyName())) {
            log.info("Received context files change event in StatusDecorator.");

            Set<String> oldFiles = (Set<String>) evt.getOldValue();
            Set<String> newFiles = (Set<String>) evt.getNewValue();

            Set<String> changedPaths = new HashSet<>();
            changedPaths.addAll(oldFiles);
            changedPaths.addAll(newFiles);

            for (String path : changedPaths) {
                FileObject fo = FileUtil.toFileObject(new File(path));
                if (fo != null) {
                    try {
                        StatusDecorator sd = fo.getFileSystem().getDecorator();
                        log.log(Level.INFO, "Firing attribute change for status decorator on: {0} decorator= " + sd + " fileSystem=" + fo.getFileSystem(), path);
                        fo.setAttribute("inAiContextStatus", newFiles.contains(path));
                        log.log(Level.INFO, "Fired attribute change for status decorator on: {0}", path);
                    } catch (IOException e) {
                        log.log(Level.SEVERE, "Could not fire attribute change for status decorator on: " + path, e);
                    }
                }
            }
        }
        log.log(Level.INFO, "EXIT propertyChange(evt={0})", evt);
    }
}
