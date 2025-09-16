package uno.anahata.nb.ai.functions.spi;

import java.awt.event.ActionEvent;
import javax.swing.Action;
import javax.swing.SwingUtilities;
import org.openide.awt.Actions;
import org.openide.util.Utilities;
import uno.anahata.gemini.functions.AutomaticFunction;

/**
 * A Core Function provider that groups all functions for interacting with Git.
 */
public class Git {

    @AutomaticFunction(
        "Opens the NetBeans Git Commit dialog. The dialog is context-aware "
                + "and will automatically find all modified files in the "
                + "currently open projects."
    )
    public static String openCommitDialog() throws Exception {
        final String actionCategory = "Git";
        final String actionId = "org.netbeans.modules.git.ui.commit.CommitAction";
        
        final Action commitAction = Actions.forID(actionCategory, actionId);

        if (commitAction == null) {
            return "Error: Could not find the Git Commit action. " +
                   "Ensure the plugin has a dependency on the 'org-netbeans-modules-git' module.";
        }

        SwingUtilities.invokeLater(() -> {
            ActionEvent event = new ActionEvent(
                Utilities.actionsGlobalContext(),
                ActionEvent.ACTION_PERFORMED,
                ""
            );
            commitAction.actionPerformed(event);
        });

        return "Successfully invoked the Git commit action. The dialog should now be open.";
    }
}
