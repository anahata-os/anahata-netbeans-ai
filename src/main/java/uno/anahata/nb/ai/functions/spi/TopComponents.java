package uno.anahata.nb.ai.functions.spi;


import org.openide.windows.WindowManager;
import org.openide.windows.TopComponent;
import org.openide.loaders.DataObject;
import java.util.Set;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.awt.EventQueue;
import org.netbeans.core.api.multiview.MultiViewHandler;
import org.netbeans.core.api.multiview.MultiViews;
import uno.anahata.gemini.functions.AIToolMethod;


/**
 *
 * @author pablo
 */
public class TopComponents {

    @AIToolMethod("gets a list of all TopComponent(s) open in the IDE")
    public static String getOpenTopComponents() throws Exception {
        final StringBuilder sb = new StringBuilder();
        sb.append("=== Open TopComponents by Class ===\n");
        EventQueue.invokeAndWait(() -> {
            Set<TopComponent> opened = WindowManager.getDefault().getRegistry().getOpened();
            if (opened.isEmpty()) {
                sb.append("No open TopComponents found.\n");
                return;
            }
            Map<String, List<TopComponent>> groupedByClass = opened.stream().collect(Collectors.groupingBy(tc -> tc.getClass().getName()));
            groupedByClass.entrySet().stream().sorted(Map.Entry.comparingByKey()).forEach(entry -> {
                sb.append("\n").append(entry.getKey()).append(":\n");
                for (TopComponent tc : entry.getValue()) {
                    sb.append("  - ").append(getTopComponentDescription(tc)).append("\n");
                }
            });
        });
        return sb.toString();
    }

    private static String getTopComponentDescription(TopComponent tc) {
        String displayName = tc.getDisplayName();
        if (displayName == null) {
            displayName = tc.getName();
        }
        MultiViewHandler mvh = MultiViews.findMultiViewHandler(tc);
        
        if (mvh != null) {
            DataObject dataObject = tc.getLookup().lookup(DataObject.class);
            if (dataObject != null && dataObject.getPrimaryFile() != null) {
                return String.format("'%s' [File: %s]", displayName, dataObject.getPrimaryFile().getPath());
            }
            return String.format("'%s' [Non-file MultiView]", displayName);
        }
        
        if (tc.getClass().getName().equals("org.netbeans.modules.git.ui.status.GitVersioningTopComponent")) {
            return String.format("'%s' [Git Status View]", displayName);
        }
        if (tc.getClass().getName().equals("org.netbeans.core.io.ui.IOWindow$IOWindowImpl")) {
            return "'Output' [IDE Output Window]";
        }
        if (tc.getClass().getName().equals("uno.anahata.nb.ai.AnahataTopComponent")) {
            return "'Anahata AI Assistant'";
        }
        DataObject dataObject = tc.getLookup().lookup(DataObject.class);
        if (dataObject != null && dataObject.getPrimaryFile() != null) {
            return String.format("'%s' [File: %s] (Generic)", displayName, dataObject.getPrimaryFile().getPath());
        }
        return String.format("'%s' (Generic)", displayName);
    }
}
