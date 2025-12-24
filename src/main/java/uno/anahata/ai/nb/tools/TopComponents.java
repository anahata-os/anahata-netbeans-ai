/* Licensed under the Apache License, Version 2.0 */
package uno.anahata.ai.nb.tools;

import java.awt.EventQueue;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import javax.swing.Action;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.windows.Mode;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;
import uno.anahata.ai.tools.AIToolMethod;
import uno.anahata.ai.nb.model.windows.TopComponentInfo;

public class TopComponents {

    private static List<TopComponentInfo> gatherTopComponentInfo() throws InterruptedException, InvocationTargetException {
        if (EventQueue.isDispatchThread()) {
            return gatherInfoOnEDT();
        } else {
            final List<TopComponentInfo> results = new ArrayList<>();
            EventQueue.invokeAndWait(() -> {
                results.addAll(gatherInfoOnEDT());
            });
            return results;
        }
    }

    private static List<TopComponentInfo> gatherInfoOnEDT() {
        List<TopComponentInfo> results = new ArrayList<>();
        Set<TopComponent> opened = WindowManager.getDefault().getRegistry().getOpened();
        if (opened.isEmpty()) {
            return Collections.emptyList();
        }
        TopComponent activated = WindowManager.getDefault().getRegistry().getActivated();

        for (TopComponent tc : opened) {
            String id = WindowManager.getDefault().findTopComponentID(tc);
            String name = tc.getName();
            String displayName = tc.getDisplayName();
            String htmlDisplayName = tc.getHtmlDisplayName();
            String tooltip = tc.getToolTipText();
            String className = tc.getClass().getName();
            boolean isActivated = (tc == activated);
            Mode mode = WindowManager.getDefault().findMode(tc);
            String modeName = (mode != null) ? mode.getName() : "N/A";

            String activatedNodes = "N/A";
            Node[] nodes = tc.getActivatedNodes();
            if (nodes != null && nodes.length > 0) {
                activatedNodes = Arrays.stream(nodes)
                        .map(Node::getDisplayName)
                        .collect(Collectors.joining(", "));
            }

            String supportedActions = "N/A";
            Action[] actions = tc.getActions();
            if (actions != null && actions.length > 0) {
                supportedActions = Arrays.stream(actions)
                        .filter(a -> a != null && a.getValue(Action.NAME) != null)
                        .map(action -> action.getValue(Action.NAME).toString())
                        .collect(Collectors.joining(", "));
            }

            String filePath = "N/A";
            FileObject fileObject = tc.getLookup().lookup(FileObject.class);
            if (fileObject != null) {
                filePath = fileObject.getPath();
            }

            String primaryFilePath = "N/A";
            long sizeInBytes = -1;
            DataObject dataObject = tc.getLookup().lookup(DataObject.class);
            if (dataObject != null) {
                FileObject primaryFile = dataObject.getPrimaryFile();
                if (primaryFile != null) {
                    sizeInBytes = primaryFile.getSize();
                    try {
                        File f = FileUtil.toFile(primaryFile);
                        if (f != null) {
                            primaryFilePath = f.getAbsolutePath();
                        } else {
                            URL url = primaryFile.getURL();
                            primaryFilePath = url.toExternalForm();
                        }
                    } catch (FileStateInvalidException e) {
                        primaryFilePath = "Error getting path: " + e.getMessage();
                    }
                }
            }

            results.add(new TopComponentInfo(id, name, isActivated, displayName, htmlDisplayName, tooltip, className, modeName, activatedNodes, supportedActions, filePath, primaryFilePath, sizeInBytes));
        }
        return results;
    }

    @AIToolMethod("Gets a detailed list of all open IDE windows as a structured list of objects.")
    public static List<TopComponentInfo> getOpenTopComponentsOverview() throws Exception {
        return gatherTopComponentInfo();
    }

    @AIToolMethod("Gets a detailed list of all open IDE windows, formatted as a Markdown table.")
    public static String getOpenTopComponentsMarkdown() throws Exception {
        List<TopComponentInfo> infos = gatherTopComponentInfo();
        if (infos.isEmpty()) {
            return "No TopComponents are currently open.";
        }

        StringBuilder sb = new StringBuilder();
        // Header
        sb.append("| Id | Name | Selected | Mode | Activated Nodes | Size | Path | Tooltip | ClassName |\n");
        sb.append("|---|---|---|---|---|---|---|---|---|\n");

        // Rows
        for (TopComponentInfo info : infos) {
            String bestName = info.htmlDisplayName() != null ? info.htmlDisplayName() : 
                              info.displayName() != null ? info.displayName() : info.name();

            sb.append(String.format("| %s | %s | %s | %s | %s | %d | %s | %s | %s |\n",
                    info.id() != null ? info.id().replace("|", "\\\\|") : "N/A",
                    bestName != null ? bestName.replace("|", "\\\\|") : "N/A",
                    info.selected() ? "Y" : "N",
                    info.mode() != null ? info.mode().replace("|", "\\\\|") : "N/A",
                    info.activatedNodes() != null ? info.activatedNodes().replace("|", "\\\\|") : "N/A",
                    info.sizeInBytes(),
                    info.primaryFilePath() != null ? info.primaryFilePath().replace("|", "\\\\|") : "N/A",
                    info.tooltip() != null ? info.tooltip().replace("|", "\\\\|") : "N/A",
                    info.className() != null ? info.className().replace("|", "\\\\|") : "N/A"
            ));
        }
        return sb.toString();
    }

    @AIToolMethod("Gets a detailed list of all open IDE windows, formatted as a simple string.")
    public static String getOpenTopComponentsDetailedString() throws Exception {
        List<TopComponentInfo> infos = gatherTopComponentInfo();
        if (infos.isEmpty()) {
            return "No TopComponents are currently open.";
        }

        StringBuilder sb = new StringBuilder();
        for (TopComponentInfo info : infos) {
            sb.append("--------------------------------------------------\n");
            sb.append(String.format("ID: %s\n", info.id() != null ? info.id() : "N/A"));
            sb.append(String.format("Name: %s\n", info.name()));
            sb.append(String.format("Selected: %s\n", info.selected() ? "Y" : "N"));
            sb.append(String.format("DisplayName: %s\n", info.displayName()));
            sb.append(String.format("HtmlDisplayName: %s\n", info.htmlDisplayName()));
            sb.append(String.format("Tooltip: %s\n", info.tooltip()));
            sb.append(String.format("ClassName: %s\n", info.className()));
            sb.append(String.format("Mode: %s\n", info.mode()));
            sb.append(String.format("Activated Nodes: %s\n", info.activatedNodes()));
            sb.append(String.format("Supported Actions: %s\n", info.supportedActions()));
            sb.append(String.format("File Path: %s\n", info.filePath()));
            sb.append(String.format("Primary File Path: %s\n", info.primaryFilePath()));
            sb.append(String.format("Size (bytes): %d\n", info.sizeInBytes()));
            sb.append("--------------------------------------------------\n\n");
        }
        return sb.toString();
    }
}
