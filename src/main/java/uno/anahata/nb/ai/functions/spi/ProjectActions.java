package uno.anahata.nb.ai.functions.spi;

import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.spi.project.ActionProvider;
import org.openide.util.Lookup;
import uno.anahata.gemini.functions.AITool;

/**
 * Provides tools for interacting with NetBeans projects using high-level,
 * build-system-agnostic actions.
 */
public class ProjectActions {

    @AITool("Runs a standard high-level action (like 'run' or 'build') on a given open project.")
    public static String runProjectAction(
            @AITool("The display name of the open project (e.g., 'gemini-java-client').") String projectName,
            @AITool("The action to run. Common actions are 'run', 'build', 'clean', 'rebuild', 'test'.") String action) throws Exception {

        Project project = null;
        for (Project p : OpenProjects.getDefault().getOpenProjects()) {
            if (ProjectUtils.getInformation(p).getDisplayName().equals(projectName)) {
                project = p;
                break;
            }
        }

        if (project == null) {
            return "Error: Project '" + projectName + "' not found among open projects.";
        }

        ActionProvider ap = project.getLookup().lookup(ActionProvider.class);
        if (ap == null) {
            return "Error: Could not find an ActionProvider for project '" + projectName + "'.";
        }
        
        Lookup context = project.getLookup();

        if (ap.isActionEnabled(action, context)) {
            ap.invokeAction(action, context);
            return "Successfully invoked the '" + action + "' action on project '" + projectName + "'.";
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
                 return "Error: The '" + action + "' action is supported but not currently enabled for project '" + projectName + "'.";
            } else {
                 return "Error: The '" + action + "' action is not supported by project '" + projectName + "'. Supported actions are: " + String.join(", ", supportedActions);
            }
        }
    }
}
