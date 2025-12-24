/* Licensed under the Apache License, Version 2.0 */
package uno.anahata.ai.nb.tools;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;
import javax.tools.Diagnostic;
import lombok.extern.slf4j.Slf4j;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.spi.project.ui.ProjectProblemsProvider;
import org.openide.filesystems.FileObject;
import uno.anahata.ai.AnahataExecutors;
import uno.anahata.ai.internal.TextUtils;
import uno.anahata.ai.nb.model.ide.JavacAlert;
import uno.anahata.ai.nb.model.ide.ProjectAlert;
import uno.anahata.ai.nb.model.ide.ProjectDiagnostics;
import uno.anahata.ai.tools.AIToolMethod;
import uno.anahata.ai.tools.AIToolParam;
import uno.anahata.ai.tools.spi.pojos.TextChunk;

@Slf4j
public class IDE {

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

    @AIToolMethod("Performs a live scan of a specific project to find all high-level project problems and Java source file errors/warnings.")
    public static ProjectDiagnostics getProjectAlerts(@AIToolParam("The ID of the project to scan.") String projectId) throws Exception {
        Project targetProject = Projects.findProject(projectId);
        ProjectDiagnostics projectDiags = new ProjectDiagnostics(ProjectUtils.getInformation(targetProject).getDisplayName());

        // 1. Scan for Javac-style alerts in parallel
        Sources sources = ProjectUtils.getSources(targetProject);
        SourceGroup[] sourceGroups = sources.getSourceGroups(Sources.TYPE_GENERIC);
        List<FileObject> javaFiles = new ArrayList<>();

        for (SourceGroup sg : sourceGroups) {
            FileObject root = sg.getRootFolder();
            Enumeration<? extends FileObject> files = root.getChildren(true);
            while (files.hasMoreElements()) {
                FileObject fo = files.nextElement();
                if (!fo.isFolder() && "text/x-java".equals(fo.getMIMEType())) {
                    javaFiles.add(fo);
                }
            }
        }

        List<CompletableFuture<Void>> futures = new ArrayList<>();
        for (FileObject fo : javaFiles) {
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                try {
                    JavaSource javaSource = JavaSource.forFileObject(fo);
                    if (javaSource != null) {
                        javaSource.runUserActionTask(controller -> {
                            controller.toPhase(JavaSource.Phase.RESOLVED);
                            List<? extends Diagnostic> diagnostics = controller.getDiagnostics();
                            for (Diagnostic d : diagnostics) {
                                projectDiags.addJavacAlert(new JavacAlert(
                                        fo.getPath(),
                                        d.getKind().toString(),
                                        (int) d.getLineNumber(),
                                        (int) d.getColumnNumber(),
                                        d.getMessage(null)
                                ));
                            }
                        }, true);
                    }
                } catch (IOException e) {
                    projectDiags.addJavacAlert(new JavacAlert(fo.getPath(), "ERROR", -1, -1, "Error processing file: " + e.getMessage()));
                }
            }, AnahataExecutors.SHARED_CPU_EXECUTOR);
            futures.add(future);
        }

        // Wait for all parallel tasks to complete
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

        // 2. Scan for Project-level problems (this is typically fast and doesn't need parallelization)
        ProjectProblemsProvider problemProvider = targetProject.getLookup().lookup(ProjectProblemsProvider.class);
        if (problemProvider != null) {
            Collection<? extends ProjectProblemsProvider.ProjectProblem> problems = problemProvider.getProblems();
            for (ProjectProblemsProvider.ProjectProblem problem : problems) {
                projectDiags.addProjectAlert(new ProjectAlert(
                        problem.getDisplayName(),
                        problem.getDescription(),
                        "PROJECT", // Use a hardcoded category as the API doesn't provide one
                        problem.getSeverity().toString(),
                        problem.isResolvable()
                ));
            }
        }

        return projectDiags;
    }
}
