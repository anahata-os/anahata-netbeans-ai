/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/NetBeansModuleDevelopment-files/contextAction.java to edit this template
 */
package uno.anahata.nb.ai;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import org.netbeans.api.project.Project;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle.Messages;

@ActionID(
        category = "Project",
        id = "uno.anahata.nb.ai.AddProjectAction"
)
@ActionRegistration(
        iconBase = "icons/anahata_16.png",
        displayName = "#CTL_AddProjectAction"
)
@ActionReference(path = "Menu/File", position = 0)
@Messages("CTL_AddProjectAction=Adds this project to the chat's context")
public final class AddProjectAction implements ActionListener {

    private final List<Project> context;

    public AddProjectAction(List<Project> context) {
        this.context = context;
    }

    @Override
    public void actionPerformed(ActionEvent ev) {
        for (Project project : context) {
            // TODO use project
        }
    }
}
