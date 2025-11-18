package uno.anahata.ai.nb.tools;

import java.awt.Component;
import java.awt.Container;
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

public class Output {
    private static final String TABBED_PANE_CLASS = "org.netbeans.core.windows.view.ui.CloseButtonTabbedPane";
    private static final String OUTPUT_TAB_CLASS = "org.netbeans.core.output2.OutputTab";

    @AIToolMethod("Lists all tabs in the NetBeans Output Window, returning their display names, total lines, and running status.")
    public static List<OutputTabInfo> listOutputTabs() throws Exception {
        final List<OutputTabInfo> tabInfos = new ArrayList<>();
        SwingUtilities.invokeAndWait(() -> {
            findOutputTabbedPane().ifPresent(tabbedPane -> {
                findComponents(tabbedPane, OUTPUT_TAB_CLASS).forEach(tabComponent -> {
                    String title = tabComponent.getName();
                    boolean isRunning = title != null && title.contains("<b>");
                    findTextComponent(tabComponent).ifPresent(textComponent -> {
                        String text = textComponent.getText();
                        int contentSize = text.length();
                        int totalLines = text.lines().toArray().length;
                        long id = System.identityHashCode(textComponent);
                        tabInfos.add(new OutputTabInfo(id, title, contentSize, totalLines, isRunning));
                    });
                });
            });
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

    private static Optional<Container> findOutputTabbedPane() {
        TopComponent outputTC = WindowManager.getDefault().findTopComponent("output");
        if (outputTC == null) {
            return Optional.empty();
        }
        return findComponents(outputTC, TABBED_PANE_CLASS).stream().map(c -> (Container)c).findFirst();
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
        return findOutputTabbedPane()
                .flatMap(tabbedPane -> {
                    return findComponents(tabbedPane, OUTPUT_TAB_CLASS).stream()
                        .map(Output::findTextComponent)
                        .filter(Optional::isPresent)
                        .map(Optional::get)
                        .filter(textComp -> System.identityHashCode(textComp) == id)
                        .findFirst();
                });
    }

    private static List<Component> findComponents(Container start, String className) {
        List<Component> result = new ArrayList<>();
        for (Component comp : start.getComponents()) {
            if (comp.getClass().getName().equals(className)) {
                result.add(comp);
            }
            if (comp instanceof Container) {
                result.addAll(findComponents((Container) comp, className));
            }
        }
        return result;
    }
}
