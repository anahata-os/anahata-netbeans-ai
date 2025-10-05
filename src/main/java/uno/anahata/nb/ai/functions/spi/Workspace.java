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
import uno.anahata.gemini.functions.AIToolMethod;
import uno.anahata.gemini.functions.AIToolParam;

public class Workspace {

    @AIToolMethod("Scans all open projects and returns a JSON summary including project name, path, gemini.md summary, and a list of all source files.")
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
                    
                    FileObject pomXml = projectDir.getFileObject("pom.xml");
                    summary.pomXmlContent = (pomXml != null) ? pomXml.asText() : null;
                    
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

    public static class ProjectSummary {
        String name;
        String path;
        String summary;
        String pomXmlContent;
        List<String> sourceFiles = new ArrayList<>();
    }
    
    public static class ProjectFileContent {
        String projectName;
        String projectPath;
        Map<String, String> files = new HashMap<>();
    }
}
