package uno.anahata.nb.ai.functions.spi;

import com.google.gson.Gson;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dialog;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.text.EditorKit;
import javax.swing.text.JTextComponent;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.api.project.ui.OpenProjects;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.modules.Modules;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;
import uno.anahata.gemini.functions.AutomaticFunction;

public class IDE {

    @AutomaticFunction("Opens a specified file in the NetBeans editor.")
    public static String openFileInEditor(@AutomaticFunction("The absolute path of the file to open.") String filePath) throws Exception {
        if (filePath == null || filePath.trim().isEmpty()) {
            return "Error: The 'filePath' parameter was not set.";
        }
        File file = new File(filePath);
        if (!file.exists()) {
            return "Error: File does not exist at path: " + filePath;
        }
        FileObject fileObject = FileUtil.toFileObject(FileUtil.normalizeFile(file));
        if (fileObject == null) {
            return "Error: Could not find or create a FileObject for: " + filePath;
        }
        DataObject dataObject = DataObject.find(fileObject);
        EditorCookie editorCookie = dataObject.getLookup().lookup(EditorCookie.class);
        if (editorCookie != null) {
            SwingUtilities.invokeLater(editorCookie::open);
            return "Successfully requested to open file in editor: " + filePath;
        } else {
            return "Error: The specified file is not an editable text file.";
        }
    }

    @AutomaticFunction("Reads the content of all tabs in the NetBeans Output Window.")
    public static String getOutputWindowContent(@AutomaticFunction("The number of lines to retrieve from the end of each tab.") int linesToRead) throws Exception {
        final String[] result = new String[1];
        final Exception[] exception = new Exception[1];
        SwingUtilities.invokeAndWait(() -> {
            try {
                TopComponent outputWindow = WindowManager.getDefault().findTopComponent("output");
                if (outputWindow == null) throw new RuntimeException("Error: Could not find the Output Window TopComponent.");
                List<Component> outputTabs = findComponentsByClassName(outputWindow, "org.netbeans.core.output2.OutputTab");
                if (outputTabs.isEmpty()) throw new RuntimeException("Error: Could not find any 'OutputTab' components.");
                result[0] = getTabsSummary(outputTabs, linesToRead);
            } catch (Exception e) {
                exception[0] = e;
            }
        });
        if (exception[0] != null) throw exception[0];
        return result[0];
    }

    @AutomaticFunction("Opens a dialog to propose a code change to the user.")
    public static boolean proposeCodeChange(@AutomaticFunction("The absolute path of the file.") String filePath, @AutomaticFunction("The new code snippet.") String newContentSnippet, @AutomaticFunction("An explanation of the change.") String explanation) throws Exception {
        final AtomicBoolean result = new AtomicBoolean(false);
        final Exception[] exception = new Exception[1];
        SwingUtilities.invokeAndWait(() -> {
            try {
                JPanel mainPanel = new JPanel(new BorderLayout(5, 5));
                mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
                JTextPane explanationPane = new JTextPane();
                explanationPane.setContentType("text/plain");
                explanationPane.setText(explanation);
                explanationPane.setEditable(false);
                explanationPane.setOpaque(false);
                mainPanel.add(explanationPane, BorderLayout.NORTH);
                JEditorPane codePane = new JEditorPane();
                codePane.setEditable(false);
                String mimeType = FileUtil.getMIMEType(FileUtil.toFileObject(new java.io.File(filePath)));
                if (mimeType == null) mimeType = "text/plain";
                if ("text/x-java".equals(mimeType)) {
                    try {
                        Class.forName("org.netbeans.modules.java.source.parsing.JavacParser", true, Modules.getDefault().findCodeNameBase("org.netbeans.modules.java.source").getClassLoader());
                    } catch (ClassNotFoundException e) { System.err.println("Could not pre-load Java source module."); }
                }
                EditorKit editorKit = JEditorPane.createEditorKitForContentType(mimeType);
                if (editorKit == null) editorKit = JEditorPane.createEditorKitForContentType("text/plain");
                codePane.setEditorKit(editorKit);
                codePane.setText(newContentSnippet);
                mainPanel.add(new JScrollPane(codePane), BorderLayout.CENTER);
                JButton approveButton = new JButton("Approve");
                DialogDescriptor descriptor = new DialogDescriptor(mainPanel, "Propose Code Change", true, new Object[]{approveButton, new JButton("Reject")}, approveButton, DialogDescriptor.DEFAULT_ALIGN, null, null);
                Dialog dialog = DialogDisplayer.getDefault().createDialog(descriptor);
                dialog.setVisible(true);
                if (descriptor.getValue() == approveButton) result.set(true);
            } catch (Exception e) {
                exception[0] = e;
            }
        });
        if (exception[0] != null) throw exception[0];
        return result.get();
    }

    @AutomaticFunction("Reads the last N lines of the NetBeans IDE's log file (messages.log).")
    public static String getLogs(@AutomaticFunction("The number of lines to read from the end of the log file.") int linesToRead) throws Exception {
        String userHome = System.getProperty("user.home");
        Path netbeansUserDir = Paths.get(System.getProperty("netbeans.user"));
        Path logFilePath = netbeansUserDir.resolve("var/log/messages.log");
        if (Files.isReadable(logFilePath)) {
            return "Reading last " + linesToRead + " lines from: " + logFilePath + "\\n\\n" + readLastLines(logFilePath, linesToRead);
        }
        Path netbeansRootDir = Paths.get(userHome, ".netbeans");
        if (Files.isDirectory(netbeansRootDir)) {
            try (Stream<Path> stream = Files.walk(netbeansRootDir, 5)) {
                Optional<Path> latestLog = stream.filter(p -> p.toString().endsWith("var/log/messages.log") && Files.isReadable(p)).max(Comparator.comparingLong(p -> p.toFile().lastModified()));
                if (latestLog.isPresent()) {
                    Path foundPath = latestLog.get();
                    return "Reading last " + linesToRead + " lines from fallback location: " + foundPath + "\\n\\n" + readLastLines(foundPath, linesToRead);
                }
            }
        }
        throw new IOException("Could not find a readable 'messages.log' file in the primary or fallback locations.");
    }
    
    @AutomaticFunction("Scans all open projects and returns a JSON summary of all errors and warnings detected by the IDE's live parser.")
    public static String getAllIDEAlerts() throws Exception {
        List<ProjectDiagnostics> allDiagnostics = new ArrayList<>();
        Project[] openProjects = OpenProjects.getDefault().getOpenProjects();

        for (Project project : openProjects) {
            ProjectDiagnostics projectDiags = new ProjectDiagnostics(ProjectUtils.getInformation(project).getDisplayName());
            Sources sources = ProjectUtils.getSources(project);
            // Change to TYPE_GENERIC to include all source types
            SourceGroup[] sourceGroups = sources.getSourceGroups(Sources.TYPE_GENERIC);

            for (SourceGroup sg : sourceGroups) {
                FileObject root = sg.getRootFolder();
                // Use a recursive enumeration to visit all files
                Enumeration<? extends FileObject> files = root.getChildren(true);
                while (files.hasMoreElements()) {
                    FileObject fo = files.nextElement();
                    if (fo.isFolder()) {
                        continue;
                    }
                    try {
                        // Only process Java files for now
                        if ("text/x-java".equals(fo.getMIMEType())) {
                            JavaSource javaSource = JavaSource.forFileObject(fo);
                            if (javaSource != null) {
                                javaSource.runUserActionTask(controller -> {
                                    controller.toPhase(JavaSource.Phase.RESOLVED);
                                    List<?> diagnostics = controller.getDiagnostics();
                                    if (!diagnostics.isEmpty()) {
                                        for (Object d : diagnostics) {
                                            // Add file path to the alert for better context
                                            projectDiags.addAlert(fo.getPath() + ": " + d.toString());
                                        }
                                    }
                                }, true);
                            }
                        }
                        // Future enhancement: Add logic for other file types here.
                    } catch (IOException e) {
                        projectDiags.addAlert("Error processing file " + fo.getPath() + ": " + e.getMessage());
                    }
                }
            }
            if (!projectDiags.alerts.isEmpty()) {
                allDiagnostics.add(projectDiags);
            }
        }

        if (allDiagnostics.isEmpty()) {
            return "No IDE errors or warnings found in any open projects.";
        }
        return new Gson().toJson(allDiagnostics);
    }

    private static class ProjectDiagnostics {
        String projectName;
        List<String> alerts = new ArrayList<>();

        ProjectDiagnostics(String projectName) {
            this.projectName = projectName;
        }

        void addAlert(String alert) {
            alerts.add(alert);
        }
    }

    private static String getTabsSummary(List<Component> tabs, int linesToRead) {
        StringBuilder summary = new StringBuilder("Found " + tabs.size() + " output tabs:\\n");
        for (Component tab : tabs) {
            String title = tab.getName() != null ? tab.getName() : "[Untitled Tab]";
            summary.append("\\n========================================================\\n").append("Tab Title: '").append(title).append("'\\n").append("----------------- Last ").append(linesToRead).append(" Lines ---------------------\\n");
            String content = extractTextFromComponent(tab);
            if (content.startsWith("[")) {
                summary.append(content);
            } else {
                String[] lines = content.split("\\\\r?\\\\n");
                summary.append(Arrays.stream(lines).skip(Math.max(0, lines.length - linesToRead)).collect(Collectors.joining("\\n")));
            }
            summary.append("\\n========================================================\\n");
        }
        return summary.toString();
    }

    private static String extractTextFromComponent(Component component) {
        if (component instanceof Container) {
            JTextComponent textComponent = findComponentByClass((Container) component, JTextComponent.class);
            if (textComponent != null) {
                String text = textComponent.getText();
                return (text == null || text.trim().isEmpty()) ? "[Tab is empty]" : text;
            }
            return "[Could not find a text component in this tab]";
        }
        return "[Tab component is not a container]";
    }

    private static List<Component> findComponentsByClassName(Container start, String className) {
        List<Component> found = new ArrayList<>();
        for (Component comp : start.getComponents()) {
            if (comp.getClass().getName().equals(className)) found.add(comp);
            if (comp instanceof Container) found.addAll(findComponentsByClassName((Container) comp, className));
        }
        return found;
    }

    private static <T extends Component> T findComponentByClass(Container start, Class<T> clazz) {
        for (Component comp : start.getComponents()) {
            if (clazz.isInstance(comp)) return clazz.cast(comp);
            if (comp instanceof Container) {
                T found = findComponentByClass((Container) comp, clazz);
                if (found != null) return found;
            }
        }
        return null;
    }

    private static String readLastLines(Path path, int numLines) throws IOException {
        List<String> allLines = Files.readAllLines(path);
        return allLines.stream().skip(Math.max(0, allLines.size() - numLines)).collect(Collectors.joining("\\n"));
    }
}
