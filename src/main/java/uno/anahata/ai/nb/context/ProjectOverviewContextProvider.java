package uno.anahata.ai.nb.context;

import com.google.common.base.Strings;
import com.google.genai.types.Part;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import uno.anahata.ai.Chat;
import uno.anahata.ai.context.provider.ContextPosition;
import uno.anahata.ai.context.provider.ContextProvider;
import uno.anahata.ai.nb.model.maven.DeclaredArtifact;
import uno.anahata.ai.nb.model.maven.DependencyGroup;
import uno.anahata.ai.nb.model.maven.DependencyScope;
import uno.anahata.ai.nb.model.projects.ProjectFile;
import uno.anahata.ai.nb.model.projects.ProjectOverview;
import uno.anahata.ai.nb.model.projects.SourceFolder;
import uno.anahata.ai.nb.tools.Projects;

@Slf4j
@Getter
public class ProjectOverviewContextProvider extends ContextProvider {

    String projectId;

    public ProjectOverviewContextProvider(String projectId) {
        super(ContextPosition.AUGMENTED_WORKSPACE);
        this.projectId = projectId;
    }

    @Override
    public String getId() {
        return projectId + "-project-overview";
    }

    @Override
    public String getDisplayName() {
        return projectId + " Overview";
    }

    @Override
    public String getDescription() {
        return "A compact, real-time overview of all open projects. (source file tree, anahata.md, maven dependencies, java versions and more";
    }

    @Override
    @SneakyThrows
    public List<Part> getParts(Chat chat) {
        List<Part> parts = new ArrayList<>();

        if (Projects.getOpenProjects().contains(projectId)) {
            ProjectOverview overview = Projects.getOverview(projectId, chat);
            parts.add(Part.fromText(generateCompactOverview(overview)));
        } else {
            parts.add(Part.fromText(projectId + " is not open, consider disabling this provider"));
        }
        

        return parts;
    }

    private String generateCompactOverview(ProjectOverview overview) {
        StringBuilder sb = new StringBuilder();
        sb.append("\n# Project: ").append(overview.getDisplayName()).append(" (`").append(overview.getId()).append("`)\n");
        sb.append("  - Path: `").append(overview.getProjectDirectory()).append("`\n");
        if (StringUtils.isNotBlank(overview.getPackaging())) {
            sb.append("  - Packaging: `").append(overview.getPackaging()).append("`\n");
        }
        sb.append("  - Java Version: ").append(overview.getJavaSourceLevel()).append(" (source), ").append(overview.getJavaTargetLevel()).append(" (target)\n");
        sb.append("  - Encoding: ").append(overview.getSourceEncoding()).append("\n");
        sb.append("  - Actions: `").append(String.join("`, `", overview.getActions())).append("`\n");

        sb.append("\n  ## Root Directory\n");
        sb.append("    - Folders: `").append(String.join("`, `", overview.getRootFolderNames())).append("`\n");
        for (ProjectFile file : overview.getRootFiles()) {
            sb.append(formatProjectFile(file, "    "));
        }

        sb.append("\n  ## Source Folders\n");
        for (SourceFolder sourceFolder : overview.getSourceFolders()) {
            String indent = "    ";
            sb.append(indent).append("Display Name: *").append(sourceFolder.getDisplayName()).append("*\n");
            sb.append(indent).append("Recursive size: ").append(FileUtils.byteCountToDisplaySize(sourceFolder.getRecursiveSize())).append("\n");
            sb.append(indent).append("Path: ").append(sourceFolder.getPath()).append("\n");
            sb.append(indent).append("Contents:\n");

            String childIndent = indent + "  ";
            if (sourceFolder.getFiles() != null) {
                for (ProjectFile file : sourceFolder.getFiles()) {
                    sb.append(formatProjectFile(file, childIndent));
                }
            }
            if (sourceFolder.getSubfolders() != null) {
                for (SourceFolder subfolder : sourceFolder.getSubfolders()) {
                    sb.append(formatSourceFolder(subfolder, childIndent, sourceFolder.getPath()));
                }
            }
        }

        if (overview.getMavenDeclaredDependencies() != null && !overview.getMavenDeclaredDependencies().isEmpty()) {
            sb.append("\n  ## Declared Maven Dependencies\n");
            for (DependencyScope scope : overview.getMavenDeclaredDependencies()) {
                sb.append(formatDependencyScope(scope, "    "));
            }
        }

        if (!Strings.isNullOrEmpty(overview.getAnahataMdContent())) {
            sb.append("\n  ## anahata.md (Project-Specific Instructions)\n");
            sb.append("  This file contains critical, high-level instructions for this specific project. You must read and adhere to these instructions before modifying any code.\n"
                    + "  The contents of this file are always provided to you on every turn so no need to read via explicit tool calling, just use the 'last Modified on disk' from your stateful resources overview to update it with suggestChange");
            sb.append(overview.getAnahataMdContent()).append("\n");
        }

        return sb.toString();
    }

    private String formatSourceFolder(SourceFolder folder, String indent, String basePath) {
        StringBuilder sb = new StringBuilder();
        String relativePath = folder.getPath().substring(basePath.length() + 1);
        sb.append(indent).append("- 📂 ").append(relativePath)
                .append(" [").append(FileUtils.byteCountToDisplaySize(folder.getRecursiveSize())).append("]\n");

        String childIndent = indent + "  ";
        if (folder.getFiles() != null) {
            for (ProjectFile file : folder.getFiles()) {
                sb.append(formatProjectFile(file, childIndent));
            }
        }
        if (folder.getSubfolders() != null) {
            for (SourceFolder subfolder : folder.getSubfolders()) {
                sb.append(formatSourceFolder(subfolder, childIndent, basePath));
            }
        }
        return sb.toString();
    }

    private String formatProjectFile(ProjectFile file, String indent) {
        String status = file.getResourceStatus() != null ? " [In context: " + file.getResourceStatus() + "]" : "";
        return String.format("%s- 📄 %s (%s)%s\n",
                indent,
                file.getName(),
                FileUtils.byteCountToDisplaySize(file.getSize()),
                status);
    }

    private String formatDependencyScope(DependencyScope scope, String indent) {
        StringBuilder sb = new StringBuilder();
        sb.append(indent).append("- Scope: `").append(scope.getScope()).append("`\n");
        String childIndent = indent + "  ";
        for (DependencyGroup group : scope.getGroups()) {
            String artifacts = group.getArtifacts().stream()
                    .map(DeclaredArtifact::getId)
                    .collect(Collectors.joining(", "));
            sb.append(childIndent).append("- `").append(group.getId()).append("`: ").append(artifacts).append("\n");
        }
        return sb.toString();
    }
}
