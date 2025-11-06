package uno.anahata.nb.ai.tools;

import com.google.gson.Gson;
import java.awt.Component;
import java.awt.Container;
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
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.swing.SwingUtilities;
import javax.swing.text.JTextComponent;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.api.project.ui.OpenProjects;
import org.openide.filesystems.FileObject;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;
import uno.anahata.gemini.functions.AIToolMethod;
import uno.anahata.gemini.functions.AIToolParam;

public class IDE {

    private static volatile String cachedIdeAlerts = "IDE Alert scanner is initializing...";

    static {
        Thread alertScannerThread = new Thread(() -> {
            while (true) {
                try {
                    cachedIdeAlerts = performScan();
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    cachedIdeAlerts = "IDE Alert scanner was interrupted.";
                    break; 
                } catch (Exception e) {
                    cachedIdeAlerts = "An error occurred during IDE alert scanning: " + e.getMessage();
                }
            }
        }, "IDE-Alert-Scanner");
        alertScannerThread.setDaemon(true);
        alertScannerThread.start();
    }

    @AIToolMethod("Reads the content of all tabs in the NetBeans Output Window.")
    public static String getOutputWindowContent(@AIToolParam("The number of lines to retrieve from the end of each tab.") int linesToRead) throws Exception {
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

    @AIToolMethod("Reads the last N lines of the NetBeans IDE's log file (messages.log).")
    public static String getLogs(@AIToolParam("The number of lines to read from the end of the log file.") int linesToRead) throws Exception {
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

    @AIToolMethod("Gets a cached JSON summary of all errors and warnings from the IDE's live parser. A background thread updates this cache continuously.")
    public static String getCachedIDEAlerts() {
        return cachedIdeAlerts;
    }
    
    @AIToolMethod("Performs a full scan of all open projects and returns a JSON summary of all errors and warnings detected by the IDE's live parser.")
    public static String getAllIDEAlerts() throws Exception {
        return performScan();
    }

    private static String performScan() throws Exception {
        List<ProjectDiagnostics> allDiagnostics = new ArrayList<>();
        Project[] openProjects = OpenProjects.getDefault().getOpenProjects();

        for (Project project : openProjects) {
            ProjectDiagnostics projectDiags = new ProjectDiagnostics(ProjectUtils.getInformation(project).getDisplayName());
            Sources sources = ProjectUtils.getSources(project);
            SourceGroup[] sourceGroups = sources.getSourceGroups(Sources.TYPE_GENERIC);

            for (SourceGroup sg : sourceGroups) {
                FileObject root = sg.getRootFolder();
                Enumeration<? extends FileObject> files = root.getChildren(true);
                while (files.hasMoreElements()) {
                    FileObject fo = files.nextElement();
                    if (fo.isFolder()) {
                        continue;
                    }
                    try {
                        if ("text/x-java".equals(fo.getMIMEType())) {
                            JavaSource javaSource = JavaSource.forFileObject(fo);
                            if (javaSource != null) {
                                javaSource.runUserActionTask(controller -> {
                                    controller.toPhase(JavaSource.Phase.RESOLVED);
                                    List<?> diagnostics = controller.getDiagnostics();
                                    if (!diagnostics.isEmpty()) {
                                        for (Object d : diagnostics) {
                                            projectDiags.addAlert(fo.getPath() + ": " + d.toString());
                                        }
                                    }
                                }, true);
                            }
                        }
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
