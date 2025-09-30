package uno.anahata.nb.ai.functions.spi;

import com.google.gson.Gson;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import javax.swing.text.JTextComponent;
import org.netbeans.api.editor.EditorRegistry;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.modules.editor.NbEditorUtilities;
import org.netbeans.spi.project.ActionProvider;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.util.Lookup;
import uno.anahata.gemini.functions.AITool;

/**
 *
 * @author pablo
 */
public class Projects {

    /**
     * Returns a List of project IDs (folder names) for all currently open
     * projects.
     */
    @AITool("Returns a List of project IDs (folder names) for all currently open projects.")
    public static List<String> getOpenProjects() {
        List<String> projectIds = new ArrayList<>();
        for (Project project : OpenProjects.getDefault().getOpenProjects()) {
            FileObject root = project.getProjectDirectory();
            projectIds.add(root.getNameExt()); // project ID = folder name
        }
        return projectIds;
    }

    @AITool("Gets an overview of a project: name, display name, listing of the project's root directory and directory tree of all source java files")
    public static String getOverview(@AITool("The project id (not the 'display name'")String projectId) {
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
        sb.append("=== Project Info ===\n");        
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
    
    private static String toString(FileObject fo) {
        StringBuilder sb = new StringBuilder();
        //String type = fo.isFolder() ? "folder" : "file";
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

    @AITool("Runs a standard high-level action (like 'run' or 'build') on a given open project.")
    public static String invokeAction(
            @AITool("The netbeans project name (not the display name)") String projectId,
            @AITool("The action to invoke") String action) throws Exception {
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
        // Get children and sort them so folders come first, then files, alphabetically
        FileObject[] children = folder.getChildren();
        Arrays.sort(children, (f1, f2) -> {
            if (f1.isFolder() && !f2.isFolder()) return -1;
            if (!f1.isFolder() && f2.isFolder()) return 1;
            return f1.getNameExt().compareTo(f2.getNameExt());
        });

        for (FileObject child : children) {
            sb.append(prefix).append(toString(child)); // Use the existing toString helper
            if (child.isFolder()) {
                // This is the missing recursive call
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

    /**
     * Finds an open project by its ID (folder name).
     *
     * @param id the project ID (folder name)
     * @return the Project if found, or null if not found
     */
    public static Project findProject(String id) {
        StringBuilder sb = new StringBuilder();
        for (Project project : OpenProjects.getDefault().getOpenProjects()) {
            FileObject root = project.getProjectDirectory();
            sb.append(" " + root.getNameExt());
            if (root.getNameExt().equals(id)) {
                return project;
            }
        }
        throw new IllegalArgumentException("No open project with id: " + id + " all open projects: " + sb.toString());
    }
    
    /**
     * Lists preferences of a project for all known public modules that store project preferences.
     */
    public static String listAllKnownPreferences(String projectId) {
        Project project = findProject(projectId);
        StringBuilder sb = new StringBuilder();

        // List of known public context classes for core modules
        Class<?>[] contextClasses = new Class<?>[] {
            
            org.netbeans.api.project.ProjectUtils.class, // Project UI API
            //org.netbeans.modules.java.project.ui.JavaProject.class, // Java project
            //org.netbeans.modules.maven.api.NbMavenProject.class // Maven project API
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
