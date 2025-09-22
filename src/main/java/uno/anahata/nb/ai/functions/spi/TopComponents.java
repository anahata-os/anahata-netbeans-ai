/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package uno.anahata.nb.ai.functions.spi;


import org.openide.windows.WindowManager;
import org.openide.windows.TopComponent;
import org.openide.loaders.DataObject;
import org.openide.filesystems.FileObject;
import java.util.Set;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.netbeans.core.multiview.MultiViewCloneableTopComponent;
import org.openide.nodes.Node;
import java.awt.EventQueue;
import org.netbeans.core.api.multiview.MultiViewHandler;
import org.netbeans.core.api.multiview.MultiViews;
import uno.anahata.gemini.functions.AITool;


/**
 *
 * @author pablo
 */
public class TopComponents {

    @AITool("gets a list of all TopComponent(s) open in the IDE")
    public String getOpenTopComponents() throws Exception {
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

    private String getTopComponentDescription(TopComponent tc) {
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
        /*
        if (tc instanceof ProjectTab) {
            Node[] nodes = tc.getActivatedNodes();
            if (nodes != null && nodes.length > 0) {
                FileObject fo = nodes[0].getLookup().lookup(FileObject.class);
                if (fo != null) {
                    return String.format("'%s' [Context: %s]", displayName, fo.getPath());
                }
                return String.format("'%s' [Active Node: %s]", displayName, nodes[0].getDisplayName());
            }
        }
        */
        if (tc.getClass().getName().equals("org.netbeans.modules.git.ui.status.GitVersioningTopComponent")) {
            return String.format("'%s' [Git Status View]", displayName);
        }
        if (tc.getClass().getName().equals("org.netbeans.core.io.ui.IOWindow$IOWindowImpl")) {
            return "'Output' [IDE Output Window]";
        }
        if (tc.getClass().getName().equals("uno.anahata.nb.ai.GeminiTopComponent")) {
            return "'Gemini AI Assistant'";
        }
        DataObject dataObject = tc.getLookup().lookup(DataObject.class);
        if (dataObject != null && dataObject.getPrimaryFile() != null) {
            return String.format("'%s' [File: %s] (Generic)", displayName, dataObject.getPrimaryFile().getPath());
        }
        return String.format("'%s' (Generic)", displayName);
    }
}
