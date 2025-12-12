package uno.anahata.ai.nb.tools;

import java.awt.Component;
import java.awt.Container;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import javax.swing.JEditorPane;
import javax.swing.SwingUtilities;
import javax.swing.text.JTextComponent;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;
import uno.anahata.ai.tools.AIToolMethod;
import uno.anahata.ai.tools.AIToolParam;
import uno.anahata.ai.nb.model.ide.OutputTabInfo;
import uno.anahata.ai.tools.spi.pojos.TextChunk;
import uno.anahata.ai.internal.TextUtils;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Output {
    private static final String OUTPUT_TAB_CLASS = "org.netbeans.core.output2.OutputTab";

    @AIToolMethod("Lists all tabs in the NetBeans Output Window, returning their display names, total lines, and running status.")
    public static List<OutputTabInfo> listOutputTabs() throws Exception {
        final List<OutputTabInfo> tabInfos = new ArrayList<>();
        SwingUtilities.invokeAndWait(() -> {
            TopComponent outputTC = WindowManager.getDefault().findTopComponent("output");
            if (outputTC == null) {
                log.warn("Output TopComponent not found.");
                return;
            }

            findOutputTabsRecursive(outputTC, tabInfos);
        });
        return tabInfos;
    }

    @AIToolMethod("Retrieves the paginated and filtered text content of a specific tab in the NetBeans Output Window, with line length truncation.")
    public static String getOutputTabContent(
            @AIToolParam("The unique ID of the tab to read.") long id,
            @AIToolParam("The starting line number (0-based) for pagination.") int startIndex,
            @AIToolParam("The number of lines to return.") int pageSize,
            @AIToolParam("A regex pattern to filter lines. Can be null or empty to return all lines.") String grepPattern,
            @AIToolParam("The maximum length of each line. Lines longer than this will be truncated. Set to 0 for no limit.") int maxLineLength) throws Exception {

        final AtomicReference<String> result = new AtomicReference<>();
        SwingUtilities.invokeAndWait(() -> {
            Optional<JTextComponent> targetComp = findTextComponentById(id);
            if (targetComp.isPresent()) {
                String text = targetComp.get().getText();
                TextChunk processResult = TextUtils.processText(text, startIndex, pageSize, grepPattern, maxLineLength);
                long linesShown = processResult.getText().lines().filter(l -> !l.isEmpty()).count();
                String header = String.format("Showing %d of %d matching lines (from %d total lines)",
                    linesShown,
                    processResult.getMatchingLineCount(),
                    processResult.getTotalLineCount());
                result.set(header + "\n\n" + processResult.getText());
            } else {
                result.set("Error: No tab found with ID '" + id + "'");
            }
        });
        return result.get();
    }

    private static void findOutputTabsRecursive(Component component, List<OutputTabInfo> tabInfos) {
        if (component == null) {
            return;
        }

        if (component.getClass().getName().equals(OUTPUT_TAB_CLASS)) {
            try {
                // Use getName() as getDisplayName() is not reliably accessible via reflection
                Method getNameMethod = component.getClass().getMethod("getName");
                String title = (String) getNameMethod.invoke(component);
                
                boolean isRunning = title != null && title.contains("<b>"); // Heuristic for running status

                findTextComponent(component).ifPresent(textComponent -> {
                    String text = textComponent.getText();
                    int contentSize = text.length();
                    int totalLines = text.lines().toArray().length;
                    long id = System.identityHashCode(textComponent);
                    tabInfos.add(new OutputTabInfo(id, title, contentSize, totalLines, isRunning));
                });
            } catch (Exception e) {
                log.error("Error processing OutputTab component: {}", component.getClass().getName(), e);
            }
        }

        if (component instanceof Container) {
            Container container = (Container) component;
            for (Component child : container.getComponents()) {
                findOutputTabsRecursive(child, tabInfos);
            }
        }
    }

    private static Optional<JTextComponent> findTextComponent(Component comp) {
        if (comp instanceof JEditorPane) {
            return Optional.of((JEditorPane) comp);
        }
        if (comp instanceof Container) {
            for (Component child : ((Container) comp).getComponents()) {
                Optional<JTextComponent> found = findTextComponent(child);
                if (found.isPresent()) {
                    return found;
                }
            }
        }
        return Optional.empty();
    }

    private static Optional<JTextComponent> findTextComponentById(long id) {
        final List<JTextComponent> foundTextComponents = new ArrayList<>();
        SwingUtilities.invokeLater(() -> {
            TopComponent outputTC = WindowManager.getDefault().findTopComponent("output");
            if (outputTC != null) {
                findTextComponentRecursive(outputTC, id, foundTextComponents);
            }
        });
        return foundTextComponents.stream().findFirst();
    }
    
    private static void findTextComponentRecursive(Component component, long targetId, List<JTextComponent> foundList) {
        if (component == null) {
            return;
        }
        
        if (component instanceof JTextComponent && System.identityHashCode(component) == targetId) {
            foundList.add((JTextComponent) component);
            return; 
        }

        if (component instanceof Container) {
            Container container = (Container) component;
            for (Component child : container.getComponents()) {
                findTextComponentRecursive(child, targetId, foundList);
                if (!foundList.isEmpty()) { // Stop early if found
                    return;
                }
            }
        }
    }
}