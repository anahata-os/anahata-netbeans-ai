package uno.anahata.nb.ai.functions.spi;

import com.google.gson.Gson;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.swing.SwingUtilities;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.api.project.ui.OpenProjects;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import uno.anahata.gemini.functions.AITool;

public class Workspace {

    @AITool("Scans all open projects and returns a JSON summary including project name, path, gemini.md summary, and a list of all source files.")
    public static String getOverview() throws Exception {
        final List<ProjectSummary> summaries = new ArrayList<>();
        final Exception[] exception = new Exception[1];
        SwingUtilities.invokeAndWait(() -> {
            try {
                for (Project project : OpenProjects.getDefault().getOpenProjects()) {
                    ProjectSummary summary = new ProjectSummary();
                    FileObject projectDir = project.getProjectDirectory();
                    summary.name = ProjectUtils.getInformation(project).getDisplayName();
                    summary.path = projectDir.getPath();
                    FileObject geminiMd = projectDir.getFileObject("gemini.md");
                    summary.summary = (geminiMd != null) ? geminiMd.asText() : "No gemini.md file found.";
                    
                    // --- Start of Change ---
                    FileObject pomXml = projectDir.getFileObject("pom.xml");
                    summary.pomXmlContent = (pomXml != null) ? pomXml.asText() : null;
                    // --- End of Change ---
                    
                    Sources sources = ProjectUtils.getSources(project);
                    for (String type : new String[]{JavaProjectConstants.SOURCES_TYPE_JAVA, JavaProjectConstants.SOURCES_TYPE_RESOURCES}) {
                        for (SourceGroup sg : sources.getSourceGroups(type)) {
                            Path rootPath = FileUtil.toFile(sg.getRootFolder()).toPath();
                            try (Stream<Path> stream = Files.walk(rootPath)) {
                                summary.sourceFiles.addAll(stream.filter(Files::isRegularFile).map(path -> rootPath.relativize(path).toString()).collect(Collectors.toList()));
                            }
                        }
                    }
                    summaries.add(summary);
                }
            } catch (Exception e) {
                exception[0] = e;
            }
        });
        if (exception[0] != null) throw exception[0];
        return new Gson().toJson(summaries);
    }
/*
    @AITool("Gets the name and display name of all currently open projects in the IDE.")
    public static String getOpenProjects() throws Exception {
        Project[] projects = OpenProjects.getDefault().getOpenProjects();
        if (projects.length == 0) return "There are no projects open in the IDE.";
        return "The following projects are open:\\n" + Arrays.stream(projects)
                .map(p -> ProjectUtils.getInformation(p).getName() + " (DisplayName:" +  ProjectUtils.getInformation(p).getDisplayName() + ")")
                .collect(Collectors.joining("\\n"));
    }*/

    //@AITool("Reads the content of a specific file within a specific open project.")
    public static String readFile(@AITool("The display name of the open project.") String projectName, @AITool("The path to the file relative to the project's root directory.") String relativeFilePath) throws Exception {
        Optional<Project> targetProjectOpt = Arrays.stream(OpenProjects.getDefault().getOpenProjects())
                .filter(p -> ProjectUtils.getInformation(p).getDisplayName().equals(projectName)).findFirst();
        if (targetProjectOpt.isEmpty()) return "Error: Could not find an open project with the display name '" + projectName + "'.";
        FileObject targetFile = targetProjectOpt.get().getProjectDirectory().getFileObject(relativeFilePath);
        if (targetFile == null || !targetFile.isValid()) return "Error: Could not find the file '" + relativeFilePath + "' in the project '" + projectName + "'.";
        if (targetFile.isFolder()) return "Error: The specified path '" + relativeFilePath + "' is a directory, not a file.";
        return targetFile.asText();
    }

    //@AITool("Scans all open projects' source directories. Reads text files, lists binary files, and respects .gitignore.")
    public static String readAllSourceFiles() throws Exception {
        List<ProjectFileContent> allProjectsContent = new ArrayList<>();
        for (Project project : OpenProjects.getDefault().getOpenProjects()) {
            ProjectFileContent pfc = new ProjectFileContent();
            FileObject projectDir = project.getProjectDirectory();
            Path projectPath = FileUtil.toFile(projectDir).toPath();
            pfc.projectName = ProjectUtils.getInformation(project).getDisplayName();
            pfc.projectPath = projectPath.toString();
            List<String> ignorePatterns = loadGitIgnorePatterns(projectPath);
            try (Stream<Path> stream = Files.walk(projectPath)) {
                stream.filter(Files::isRegularFile)
                      .filter(path -> !isIgnored(projectPath.relativize(path), ignorePatterns))
                      .forEach(path -> {
                          String relativePath = projectPath.relativize(path).toString();
                          if (isTextFile(path)) {
                              try {
                                  pfc.files.put(relativePath, Files.readString(path));
                              } catch (Exception e) {
                                  pfc.files.put(relativePath, "Error reading file: " + e.getMessage());
                              }
                          } else {
                              try {
                                  pfc.files.put(relativePath, "[Binary File: " + Files.size(path) + " bytes]");
                              } catch (IOException e) {
                                   pfc.files.put(relativePath, "[Binary File: Error getting size]");
                              }
                          }
                      });
            }
            allProjectsContent.add(pfc);
        }
        return new Gson().toJson(allProjectsContent);
    }

    private static final Set<String> TEXT_FILE_EXTENSIONS = Set.of("java", "md", "xml", "properties", "txt", "gitignore", "html", "css", "js", "json", "yml", "yaml", "sh", "bat", "sql", "groovy");

    private static boolean isTextFile(Path path) {
        String fileName = path.getFileName().toString();
        if (fileName.equalsIgnoreCase("pom.xml")) return true;
        int lastDot = fileName.lastIndexOf('.');
        if (lastDot == -1) return false;
        String extension = fileName.substring(lastDot + 1).toLowerCase();
        return TEXT_FILE_EXTENSIONS.contains(extension);
    }

    private static List<String> loadGitIgnorePatterns(Path projectRoot) {
        Path gitignorePath = projectRoot.resolve(".gitignore");
        if (!Files.exists(gitignorePath)) return Collections.singletonList(".git/");
        try (Stream<String> lines = Files.lines(gitignorePath)) {
            List<String> patterns = lines.map(String::trim).filter(line -> !line.isEmpty() && !line.startsWith("#")).map(line -> line.startsWith("/") ? line.substring(1) : line).map(line -> line.endsWith("/") ? line : line + "/").collect(Collectors.toList());
            patterns.add(".git/");
            return patterns;
        } catch (IOException e) {
            return Collections.singletonList(".git/");
        }
    }

    private static boolean isIgnored(Path relativePath, List<String> ignorePatterns) {
        String pathString = relativePath.toString();
        for (String pattern : ignorePatterns) {
            if (pattern.endsWith("/") && pathString.startsWith(pattern)) return true;
            if (pathString.equals(pattern)) return true;
        }
        return false;
    }
    
    // Helper classes for JSON serialization
    public static class ProjectSummary {
        String name;
        String path;
        String summary;
        // --- Start of Change ---
        String pomXmlContent;
        // --- End of Change ---
        List<String> sourceFiles = new ArrayList<>();
    }
    
    public static class ProjectFileContent {
        String projectName;
        String projectPath;
        Map<String, String> files = new HashMap<>();
    }
}
