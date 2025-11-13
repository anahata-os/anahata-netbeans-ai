package uno.anahata.nb.ai.tools;

import com.google.gson.Gson;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.api.project.ui.OpenProjects;
import org.openide.filesystems.FileObject;
import uno.anahata.gemini.functions.AIToolMethod;
import uno.anahata.gemini.functions.AIToolParam;
import uno.anahata.gemini.functions.spi.pojos.TextChunk;
import uno.anahata.gemini.internal.TextUtils;

@Slf4j
public class IDE {

    private static volatile String cachedIdeAlerts = "IDE Alert scanner is initializing...";

    static {
        Thread alertScannerThread = new Thread(() -> {
            while (true) {
                try {
                    cachedIdeAlerts = performScan();
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    log.error("IDE alert scanner interrupted", e);
                    Thread.currentThread().interrupt();
                    cachedIdeAlerts = "IDE Alert scanner was interrupted.";
                    break;
                } catch (Exception e) {
                    log.error("Exception fetching IDE alerts", e);
                    cachedIdeAlerts = "An error occurred during IDE alert scanning: " + e.getMessage();
                }
            }
        }, "anahata-IDE-Alert-Scanner");
        alertScannerThread.setDaemon(true);
        alertScannerThread.start();
    }

    @AIToolMethod("Reads the NetBeans IDE's log file (messages.log) with optional filtering and pagination.")
    public static String getLogs(
            @AIToolParam("A regex pattern to filter log lines. Can be null or empty to return all lines.") String grepPattern,
            @AIToolParam("The starting line number (0-based) for pagination.") int startIndex,
            @AIToolParam("The number of lines to return.") int pageSize,
            @AIToolParam("The maximum length of each line. Lines longer than this will be truncated. Set to 0 for no limit.") int maxLineLength) throws Exception {

        Path logFilePath = findLogFile();
        String content = new String(Files.readAllBytes(logFilePath));
        TextChunk processResult = TextUtils.processText(content, startIndex, pageSize, grepPattern, maxLineLength);
        long linesShown = processResult.getText().lines().filter(l -> !l.isEmpty()).count();
        String header = String.format("Showing %d of %d matching lines (from %d total lines) in %s",
                linesShown,
                processResult.getMatchingLineCount(),
                processResult.getTotalLineCount(),
                logFilePath);
        return header + "\n\n" + processResult.getText();
    }

    private static Path findLogFile() throws IOException {
        String userHome = System.getProperty("user.home");
        Path netbeansUserDir = Paths.get(System.getProperty("netbeans.user"));
        Path logFilePath = netbeansUserDir.resolve("var/log/messages.log");

        if (Files.isReadable(logFilePath)) {
            return logFilePath;
        }

        Path netbeansRootDir = Paths.get(userHome, ".netbeans");
        if (Files.isDirectory(netbeansRootDir)) {
            try (Stream<Path> stream = Files.walk(netbeansRootDir, 5)) {
                Optional<Path> latestLog = stream
                        .filter(p -> p.toString().endsWith("var/log/messages.log") && Files.isReadable(p))
                        .max(Comparator.comparingLong(p -> p.toFile().lastModified()));
                if (latestLog.isPresent()) {
                    return latestLog.get();
                }
            }
        }
        throw new IOException("Could not find a readable 'messages.log' file in the primary or fallback locations.");
    }

    //@AIToolMethod("Gets a cached JSON summary of all errors and warnings from the IDE's live parser. A background thread updates this cache continuously.")
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
}
