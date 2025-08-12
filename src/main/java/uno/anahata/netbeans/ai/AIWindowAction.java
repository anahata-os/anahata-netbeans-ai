package uno.anahata.netbeans.ai;

import javax.swing.AbstractAction;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.util.NbBundle;

@ActionID(
 category = "Menu/Edit",
 id = "uno.anahata.netbeans.ai.AIWindowAction"
)
@ActionReferences({
 @ActionReference(path = "Menu/Window", id = "AIWindowAction")
})
@NbBundle.Messages({
 "AIWindowAction.name=AI Assistant"
})
public class AIWindowAction extends AbstractAction {

 @Override
 public void actionPerformed(java.awt.event.ActionEvent evt) {
 AIWindow window = new AIWindow();
 window.open();
 window.requestActive();
 }
}
