package uno.anahata.nb.ai.functions.spi;

import io.swagger.v3.oas.annotations.media.Schema;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.spi.project.ActionProvider;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;
import uno.anahata.gemini.GeminiChat;
import uno.anahata.gemini.context.ContextManager;
import uno.anahata.gemini.context.StatefulResourceStatus;
import uno.anahata.gemini.functions.AIToolMethod;
import uno.anahata.gemini.functions.AIToolParam;

/**
 *
 * @author pablo
 */
public class Projects {

    @AIToolMethod("Returns a List of project IDs (folder names) for all currently open projects.")
    public static List<String> getOpenProjects() {
        List<String> projectIds = new ArrayList<>();
        for (Project project : OpenProjects.getDefault().getOpenProjects()) {
            FileObject root = project.getProjectDirectory();
            projectIds.add(root.getNameExt()); // project ID = folder name
        }
        return projectIds;
    }

    @AIToolMethod("Opens a project in the IDE, waiting for the asynchronous open operation to complete.")
    public static String openProject(@AIToolParam("The project id (folder name) to open.") String projectId) throws Exception {
        // Assuming projects are in the default NetBeansProjects folder in the user's home directory
        String projectsFolderPath = System.getProperty("user.home") + File.separator + "NetBeansProjects";
        File projectDir = new File(projectsFolderPath, projectId);

        if (!projectDir.exists() || !projectDir.isDirectory()) {
            return "Error: Project directory not found at " + projectDir.getAbsolutePath();
        }

        FileObject projectFob = FileUtil.toFileObject(FileUtil.normalizeFile(projectDir));
        if (projectFob == null) {
            return "Error: Could not find project directory: " + projectDir.getAbsolutePath();
        }

        // Check if already open
        for (Project p : OpenProjects.getDefault().getOpenProjects()) {
            if (p.getProjectDirectory().equals(projectFob)) {
                return "Success: Project '" + projectId + "' is already open.";
            }
        }

        Project projectToOpen = ProjectManager.getDefault().findProject(projectFob);
        if (projectToOpen == null) {
            return "Error: Could not find a project in the specified directory: " + projectDir.getAbsolutePath();
        }

        final CountDownLatch latch = new CountDownLatch(1);
        final PropertyChangeListener listener = (PropertyChangeEvent evt) -> {
            if (OpenProjects.PROPERTY_OPEN_PROJECTS.equals(evt.getPropertyName())) {
                for (Project p : OpenProjects.getDefault().getOpenProjects()) {
                    if (p.equals(projectToOpen)) {
                        latch.countDown();
                        break;
                    }
                }
            }
        };

        OpenProjects.getDefault().addPropertyChangeListener(listener);

        try {
            OpenProjects.getDefault().open(new Project[]{projectToOpen}, false, true);

            // Wait for a maximum of 30 seconds for the project to open
            if (latch.await(30, TimeUnit.SECONDS)) {
                return "Success: Project '" + projectId + "' opened successfully.";
            } else {
                return "Error: Timed out after 30 seconds waiting for project '" + projectId + "' to open.";
            }
        } finally {
            OpenProjects.getDefault().removePropertyChangeListener(listener);
        }
    }

    @AIToolMethod("Gets an overview of a project: name, display name, listing of the project's root directory and directory tree of all source java files")
    public static String getOverview(@AIToolParam("The project id (not the 'display name'") String projectId) {
        Project target = null;
        for (Project p : OpenProjects.getDefault().getOpenProjects()) {
            if (p.getProjectDirectory().getNameExt().equals(projectId)) {
                target = p;
                break;
            }
        }
        if (target == null) {
            return "Project not found: " + projectId;
        }

        StringBuilder sb = new StringBuilder();
        ProjectInformation info = ProjectUtils.getInformation(target);
        FileObject root = target.getProjectDirectory();

        // --- Project header ---
        sb.append("=== Project Overview ===\n");
        sb.append("Id: ").append(root.getNameExt()).append("\n");
        sb.append("Display Name: ").append(info.getDisplayName()).append("\n");
        sb.append("Project Directory: ").append(root.getPath()).append("\n");

        ActionProvider ap = target.getLookup().lookup(ActionProvider.class);
        if (ap != null) {
            sb.append("Actions: ").append(Arrays.toString(ap.getSupportedActions())).append("\n");
        } else {
            sb.append("Actions: (none)\n");
        }

        sb.append("\nLegend: '+' folder  '-' file, 's=' size (folder sizes are recursive) 'lm=' last modified on disk\n");

        // --- Immediate children of project root ---
        sb.append("\n=== Root folder ===\n");

        for (FileObject child : root.getChildren()) {
            sb.append(toString(child));
        }

        // --- Source files (tree structure with folder sizes) ---
        sb.append("\n=== Java sources ===\n");
        Sources sources = ProjectUtils.getSources(target);

        SourceGroup[] groups = sources.getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);

        for (SourceGroup group : groups) {
            FileObject srcRoot = group.getRootFolder();
            sb.append(toString(srcRoot));
            listFilesRecursivelyTreeWithSize(srcRoot, 1, sb);
        }

        return sb.toString();
    }

    @AIToolMethod("Gets a structured overview of a project, including VCS and in-context status.")
    public static ProjectOverview getOverview2(@AIToolParam("The project id (not the 'display name')") String projectId) throws FileStateInvalidException {
        Project target = findProject(projectId);
        if (target == null) {
            // Returning null will be serialized to JSON null by the framework
            return null;
        }

        ProjectInformation info = ProjectUtils.getInformation(target);
        FileObject root = target.getProjectDirectory();
        ProjectOverview overview = new ProjectOverview();
        overview.setId(root.getNameExt());
        overview.setDisplayName(info.getDisplayName());
        overview.setProjectDirectory(root.getPath());

        ActionProvider ap = target.getLookup().lookup(ActionProvider.class);
        if (ap != null) {
            overview.setActions(Arrays.asList(ap.getSupportedActions()));
        }

        List<ProjectFile> rootFiles = new ArrayList<>();
        for (FileObject child : root.getChildren()) {
            rootFiles.add(toProjectFile(child));
        }
        overview.setRootFiles(rootFiles);

        List<ProjectFile> sourceFiles = new ArrayList<>();
        Sources sources = ProjectUtils.getSources(target);
        SourceGroup[] javaGroups = sources.getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
        for (SourceGroup group : javaGroups) {
            FileObject srcRoot = group.getRootFolder();
            addFilesRecursively(srcRoot, sourceFiles);
        }
        overview.setSourceFiles(sourceFiles);

        List<ProjectFile> resourceFiles = new ArrayList<>();
        // Use constant for generic resources type
        SourceGroup[] resourceGroups = sources.getSourceGroups(JavaProjectConstants.SOURCES_TYPE_RESOURCES);
        for (SourceGroup group : resourceGroups) {
            FileObject resRoot = group.getRootFolder();
            addFilesRecursively(resRoot, resourceFiles);
        }
        overview.setResourceFiles(resourceFiles);

        return overview;
    }

    private static ProjectFile toProjectFile(FileObject fo) throws FileStateInvalidException {
        ProjectFile pf = new ProjectFile();
        pf.setName(fo.getNameExt());
        pf.setPath(fo.getPath());
        pf.setFolder(fo.isFolder());
        pf.setSize(fo.isFolder() ? folderSize(fo) : fo.getSize());
        pf.setLastModified(fo.lastModified().getTime());

        // In-context status
        try {
            GeminiChat chat = GeminiChat.getCallingInstance();
            if (chat != null) {
                ContextManager cm = chat.getContextManager();
                List<StatefulResourceStatus> statuses = cm.getStatefulResourcesOverview(chat.getFunctionManager());
                for (StatefulResourceStatus status : statuses) {
                    if (status.getResourceId().equals(fo.getPath())) {
                        pf.setInContextStatus(status.getStatus().name());
                        break;
                    }
                }
            }
        } catch (Exception e) {
            // log is not available in this static context, so we'll just ignore the error
            // This can happen if called outside a tool execution thread.
        }

        // VCS Status
        pf.setVcsStatus(getVCSStatus(fo));

        return pf;
    }

    private static VCSStatus getVCSStatus(FileObject fo) throws FileStateInvalidException {
        String annotation = fo.getFileSystem().getDecorator().annotateName(fo.getNameExt(), Set.of(fo));

        // Simple parsing of common annotations from the file name itself
        // Note: This is a heuristic and might not cover all VCS systems or states.
        // The annotation might be part of the HTML display name.
        String lowerAnnotation = annotation.toLowerCase();

        if (lowerAnnotation.contains("[new]")) {
            return VCSStatus.NEW;
        } else if (lowerAnnotation.contains("[modified]")) {
            return VCSStatus.MODIFIED;
        } else if (lowerAnnotation.contains("[ignored]")) {
            return VCSStatus.IGNORED;
        } else if (lowerAnnotation.contains("<b>")) { // A common indicator of change
            return VCSStatus.MODIFIED;
        }

        return VCSStatus.UP_TO_DATE;
    }

    private static void addFilesRecursively(FileObject folder, List<ProjectFile> fileList) throws FileStateInvalidException {
        for (FileObject child : folder.getChildren()) {
            fileList.add(toProjectFile(child));
            if (child.isFolder()) {
                addFilesRecursively(child, fileList);
            }
        }
    }

    private static String toString(FileObject fo) {
        StringBuilder sb = new StringBuilder();
        String typeShort = fo.isFolder() ? "+" : "-";
        boolean folder = fo.isFolder();
        long size = folder ? folderSize(fo) : fo.getSize();
        long lastModifiedOnDisk = fo.lastModified().getTime();
        sb.append(typeShort);
        sb.append(fo.getNameExt())
                .append(" [")
                .append("s=").append(size)
                .append(", lm=").append(lastModifiedOnDisk)
                .append("]\n");
        return sb.toString();
    }

    @AIToolMethod("Runs a standard high-level action (like 'run' or 'build') on a given open project.")
    public static String invokeAction(
            @AIToolParam("The netbeans project name (not the display name)") String projectId,
            @AIToolParam("The action to invoke") String action) throws Exception {
        Project project = findProject(projectId);
        ActionProvider ap = project.getLookup().lookup(ActionProvider.class);
        if (ap == null) {
            throw new IllegalArgumentException(project + " does not have ActionProvider");
        }

        Lookup context = project.getLookup();

        if (ap.isActionEnabled(action, context)) {
            ap.invokeAction(action, context);
            return "Successfully invoked the '" + action + "' action on project '" + project + "'.";
        } else {
            String[] supportedActions = ap.getSupportedActions();
            boolean isSupported = false;
            for (String supportedAction : supportedActions) {
                if (supportedAction.equals(action)) {
                    isSupported = true;
                    break;
                }
            }
            if (isSupported) {
                throw new IllegalArgumentException("The '" + action + "' action is supported but not currently enabled for project '" + project + "'.");
            } else {
                throw new IllegalArgumentException("The '" + action + "' action is not supported by project '" + project + "'. Supported actions are: " + String.join(", ", supportedActions));
            }
        }
    }

    private static void listFilesRecursivelyTreeWithSize(FileObject folder, int indent, StringBuilder sb) {
        String prefix = "  ".repeat(indent * 2);
        FileObject[] children = folder.getChildren();
        Arrays.sort(children, (f1, f2) -> {
            if (f1.isFolder() && !f2.isFolder()) {
                return -1;
            }
            if (!f1.isFolder() && f2.isFolder()) {
                return 1;
            }
            return f1.getNameExt().compareTo(f2.getNameExt());
        });

        for (FileObject child : children) {
            sb.append(prefix).append(toString(child));
            if (child.isFolder()) {
                listFilesRecursivelyTreeWithSize(child, indent + 1, sb);
            }
        }
    }

    private static long folderSize(FileObject folder) {
        long total = 0;
        for (FileObject child : folder.getChildren()) {
            if (child.isFolder()) {
                total += folderSize(child);
            } else {
                total += child.getSize();
            }
        }
        return total;
    }

    public static Project findProject(String id) {
        for (Project project : OpenProjects.getDefault().getOpenProjects()) {
            FileObject root = project.getProjectDirectory();
            if (root.getNameExt().equals(id)) {
                return project;
            }
        }
        return null;
    }

    public static String listAllKnownPreferences(String projectId) {
        Project project = findProject(projectId);
        if (project == null) {
            return "Project not found: " + projectId;
        }
        StringBuilder sb = new StringBuilder();
        Class<?>[] contextClasses = new Class<?>[]{
            org.netbeans.api.project.ProjectUtils.class,
        };

        for (Class<?> ctx : contextClasses) {
            sb.append("Preferences for context: ").append(ctx.getName()).append("\n");
            Preferences prefs = ProjectUtils.getPreferences(project, ctx, true);
            try {
                String[] keys = prefs.keys();
                if (keys.length == 0) {
                    sb.append("  (no preferences)\n");
                } else {
                    for (String key : keys) {
                        sb.append("  ").append(key)
                                .append(" = ").append(prefs.get(key, "<no value>")).append("\n");
                    }
                }
            } catch (BackingStoreException e) {
                sb.append("  Failed to read preferences: ").append(e.getMessage()).append("\n");
            }
            sb.append("\n");
        }

        return sb.toString();
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Represents an overview of a project.")
    public static class ProjectOverview {

        @Schema(description = "The project ID (folder name).")
        private String id;
        @Schema(description = "The display name of the project.")
        private String displayName;
        @Schema(description = "The absolute path to the project directory.")
        private String projectDirectory;
        @Schema(description = "A list of available actions for the project.")
        private List<String> actions;
        @Schema(description = "A list of files and folders in the project's root directory.")
        private List<ProjectFile> rootFiles;
        @Schema(description = "A list of source files in the project.")
        private List<ProjectFile> sourceFiles;
        @Schema(description = "A list of resource files in the project.")
        private List<ProjectFile> resourceFiles;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Represents a file or folder within a project.")
    public static class ProjectFile {

        @Schema(description = "The name of the file or folder.")
        private String name;
        @Schema(description = "The absolute path to the file or folder.")
        private String path;
        @Schema(description = "True if this is a folder, false if it is a file.")
        private boolean folder;
        @Schema(description = "The size of the file in bytes, or the recursive size if it is a folder.")
        private long size;
        @Schema(description = "The last modified timestamp of the file.")
        private long lastModified;
        @Schema(description = "The in-context status of the file (e.g., VALID, STALE).")
        private String inContextStatus;
        @Schema(description = "The Version Control System (VCS) status of the file.")
        private VCSStatus vcsStatus;
    }

    @Schema(description = "Represents the Version Control System (VCS) status of a file.")
    public enum VCSStatus {
        @Schema(description = "File is new and not yet committed.")
        NEW,
        @Schema(description = "File has been modified.")
        MODIFIED,
        @Schema(description = "File is ignored by version control.")
        IGNORED,
        @Schema(description = "File is up-to-date with the repository.")
        UP_TO_DATE,
        @Schema(description = "The VCS status could not be determined.")
        UNKNOWN
    }
}
