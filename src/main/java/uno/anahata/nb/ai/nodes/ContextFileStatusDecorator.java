package uno.anahata.nb.ai.nodes;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.Set;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.StatusDecorator;
import org.openide.util.lookup.ServiceProvider;
import uno.anahata.nb.ai.context.ContextFiles;

@ServiceProvider(service = StatusDecorator.class, position = 900)
public class ContextFileStatusDecorator implements StatusDecorator, PropertyChangeListener {

    private final java.beans.PropertyChangeSupport pcs = new java.beans.PropertyChangeSupport(this);

    public ContextFileStatusDecorator() {
        ContextFiles.getInstance().addPropertyChangeListener(this);
    }

    @Override
    public String annotateNameHtml(String name, Set<? extends FileObject> files) {
        if (files == null || files.size() != 1) {
            return name;
        }
        FileObject fo = files.iterator().next();
        if (fo == null) {
            return name;
        }
        File file = FileUtil.toFile(fo);
        if (file != null && ContextFiles.getInstance().contains(file)) {
            // Using a NetBeans theme color for the annotation
            return "<html>" + name + " <font color='!Actions.Blue'>[Context]</font></html>";
        }
        return name;
    }

    @Override
    public String annotateName(String name, Set<? extends FileObject> files) {
        if (files == null || files.size() != 1) {
            return name;
        }
        FileObject fo = files.iterator().next();
        if (fo == null) {
            return name;
        }
        File file = FileUtil.toFile(fo);
        if (file != null && ContextFiles.getInstance().contains(file)) {
            return name + " [Context]";
        }
        return name;
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (ContextFiles.PROP_CONTEXT_FILES_CHANGED.equals(evt.getPropertyName())) {
            // The constant is not available on this interface, so we use the raw string.
            pcs.firePropertyChange("statusChanged", null, null);
        }
    }

    public void addPropertyChangeListener(PropertyChangeListener l) {
        pcs.addPropertyChangeListener(l);
    }

    public void removePropertyChangeListener(PropertyChangeListener l) {
        pcs.removePropertyChangeListener(l);
    }
}
