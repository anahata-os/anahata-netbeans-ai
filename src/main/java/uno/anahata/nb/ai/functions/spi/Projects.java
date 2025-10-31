package uno.anahata.nb.ai.functions.spi;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.api.project.ProjectManager;
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
import uno.anahata.gemini.context.ResourceStatus;
import uno.anahata.gemini.context.StatefulResourceStatus;
import uno.anahata.gemini.functions.AIToolMethod;
import uno.anahata.gemini.functions.AIToolParam;
import uno.anahata.nb.ai.project.overview.ProjectFile;
import uno.anahata.nb.ai.project.overview.ProjectOverview;
import uno.anahata.nb.ai.project.overview.SourceFolder;

@Slf4j
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
        String projectsFolderPath = System.getProperty("user.home") + File.separator + "NetBeansProjects";
        File projectDir = new File(projectsFolderPath, projectId);

        if (!projectDir.exists() || !projectDir.isDirectory()) {
            return "Error: Project directory not found at " + projectDir.getAbsolutePath();
        }

        FileObject projectFob = FileUtil.toFileObject(FileUtil.normalizeFile(projectDir));
        if (projectFob == null) {
            return "Error: Could not find project directory: " + projectDir.getAbsolutePath();
        }

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
            if (latch.await(30, TimeUnit.SECONDS)) {
                return "Success: Project '" + projectId + "' opened successfully.";
            } else {
                return "Error: Timed out after 30 seconds waiting for project '" + projectId + "' to open.";
            }
        } finally {
            OpenProjects.getDefault().removePropertyChangeListener(listener);
        }
    }

    @AIToolMethod("Gets a structured, context-aware overview of a project, including root files, source tree, and the in-context status of each file.")
    public static ProjectOverview getOverview(@AIToolParam("The project id (not the 'display name')") String projectId) throws FileStateInvalidException {
        Project target = findProject(projectId);
        if (target == null) {
            return null;
        }

        Map<String, ResourceStatus> statusMap = getContextStatusMap();
        ProjectInformation info = ProjectUtils.getInformation(target);
        FileObject root = target.getProjectDirectory();
        List<String> actions = Collections.emptyList();
        ActionProvider ap = target.getLookup().lookup(ActionProvider.class);
        if (ap != null) {
            actions = Arrays.asList(ap.getSupportedActions());
        }

        List<ProjectFile> rootFiles = new ArrayList<>();
        List<String> rootFolderNames = new ArrayList<>();
        List<SourceFolder> sourceFolders = new ArrayList<>();

        for (FileObject child : root.getChildren()) {
            if (child.isFolder()) {
                rootFolderNames.add(child.getNameExt());
            } else {
                rootFiles.add(createProjectFile(child, statusMap));
            }
        }

        Sources sources = ProjectUtils.getSources(target);
        List<SourceGroup> allSourceGroups = new ArrayList<>();
        allSourceGroups.addAll(Arrays.asList(sources.getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA)));
        allSourceGroups.addAll(Arrays.asList(sources.getSourceGroups(JavaProjectConstants.SOURCES_TYPE_RESOURCES)));
        
        for (SourceGroup group : allSourceGroups) {
            FileObject srcRoot = group.getRootFolder();
            sourceFolders.add(buildSourceFolderTree(srcRoot, group.getDisplayName(), statusMap));
        }

        return new ProjectOverview(
            root.getNameExt(),
            info.getDisplayName(),
            root.getPath(),
            rootFiles,
            rootFolderNames,
            sourceFolders,
            actions
        );
    }

    private static Map<String, ResourceStatus> getContextStatusMap() {
        try {
            GeminiChat chat = GeminiChat.getCallingInstance();
            if (chat != null) {
                ContextManager cm = chat.getContextManager();
                return cm.getStatefulResourcesOverview(chat.getFunctionManager())
                         .stream()
                         .collect(Collectors.toMap(
                             StatefulResourceStatus::getResourceId,
                             StatefulResourceStatus::getStatus,
                             (s1, s2) -> s2
                         ));
            }
        } catch (Exception e) {
            log.warn("Could not get context status map, possibly not in a tool call context.", e);
        }
        return Collections.emptyMap();
    }

    private static SourceFolder buildSourceFolderTree(FileObject folder, String displayName, Map<String, ResourceStatus> statusMap) throws FileStateInvalidException {
        if (!folder.isFolder()) {
            throw new IllegalArgumentException("FileObject must be a folder: " + folder.getPath());
        }

        List<ProjectFile> files = new ArrayList<>();
        List<SourceFolder> subfolders = new ArrayList<>();
        
        for (FileObject child : folder.getChildren()) {
            if (child.isFolder()) {
                subfolders.add(buildSourceFolderTree(child, child.getNameExt(), statusMap));
            } else {
                files.add(createProjectFile(child, statusMap));
            }
        }

        long recursiveSize = files.stream().mapToLong(ProjectFile::getSize).sum()
                           + subfolders.stream().mapToLong(SourceFolder::getRecursiveSize).sum();

        return new SourceFolder(folder.getNameExt(), displayName, folder.getPath(), recursiveSize, files, subfolders);
    }

    private static ProjectFile createProjectFile(FileObject fo, Map<String, ResourceStatus> statusMap) throws FileStateInvalidException {
        String path = fo.getPath();
        ResourceStatus status = statusMap.getOrDefault(path, ResourceStatus.NOT_IN_CONTEXT);
        return new ProjectFile(
            fo.getNameExt(),
            path,
            fo.getSize(),
            fo.lastModified().getTime(),
            status
        );
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
            boolean isSupported = Arrays.asList(supportedActions).contains(action);
            if (isSupported) {
                throw new IllegalArgumentException("The '" + action + "' action is supported but not currently enabled for project '" + project + "'.");
            } else {
                throw new IllegalArgumentException("The '" + action + "' action is not supported by project '" + project + "'. Supported actions are: " + String.join(", ", supportedActions));
            }
        }
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
}
