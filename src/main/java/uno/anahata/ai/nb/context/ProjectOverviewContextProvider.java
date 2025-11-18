package uno.anahata.ai.nb.context;

import com.google.common.base.Strings;
import com.google.genai.types.Part;
import java.util.ArrayList;
import java.util.List;
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
public class ProjectOverviewContextProvider extends ContextProvider {

    public ProjectOverviewContextProvider() {
        super(ContextPosition.AUGMENTED_WORKSPACE);
    }

    @Override
    public String getId() {
        return "netbeans-project-overview";
    }

    @Override
    public String getDisplayName() {
        return "Project Overview (Compact, all open projects)";
    }
    
    @Override
    public String getDescription() {
        return "A compact, real-time overview of all open projects. (source file tree, anahata.md, maven dependencies, java versions and more";
    }

    @Override
    @SneakyThrows
    public List<Part> getParts(Chat chat) {
        List<String> projectIds = Projects.getOpenProjects();

        List<Part> parts = new ArrayList<>();
        
        for (String projectId : projectIds) {
            ProjectOverview overview = Projects.getOverview(projectId, chat);
            parts.add(Part.fromText(generateCompactOverview(overview)));
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
            sb.append(formatSourceFolder(sourceFolder, "    "));
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
                    +  "  The contents of this file are always provided to you on every turn so no need to read via explicit tool calling, just use the 'last Modified on disk' from your stateful resources overview to update it with suggestChange");
            sb.append(overview.getAnahataMdContent()).append("\n");
        }

        return sb.toString();
    }

    private String formatSourceFolder(SourceFolder folder, String indent) {
        StringBuilder sb = new StringBuilder();
        String displayName = folder.getDisplayName() != null ? " (" + folder.getDisplayName() + ")" : "";
        sb.append(indent).append("- 📂 ").append(folder.getPath()).append(displayName)
          .append(" [").append(FileUtils.byteCountToDisplaySize(folder.getRecursiveSize())).append("]\n");

        String childIndent = indent + "  ";
        if (folder.getFiles() != null) {
            for (ProjectFile file : folder.getFiles()) {
                sb.append(formatProjectFile(file, childIndent));
            }
        }
        if (folder.getSubfolders() != null) {
            for (SourceFolder subfolder : folder.getSubfolders()) {
                sb.append(formatSourceFolder(subfolder, childIndent));
            }
        }
        return sb.toString();
    }

    private String formatProjectFile(ProjectFile file, String indent) {
        String status = file.getResourceStatus() != null ? " [In context: " + file.getResourceStatus() +  "]": "";
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
            sb.append(childIndent).append("- Group: `").append(group.getId()).append("`\n");
            String artifactIndent = childIndent + "  ";
            for (DeclaredArtifact artifact : group.getArtifacts()) {
                sb.append(artifactIndent).append("- ").append(artifact.getId()).append("\n");
                if (artifact.getExclusions() != null && !artifact.getExclusions().isEmpty()) {
                    sb.append(artifactIndent).append("  - Exclusions: `").append(String.join("`, `", artifact.getExclusions())).append("`\n");
                }
            }
        }
        return sb.toString();
    }
}