package uno.anahata.netbeans.ai;

import javax.swing.*;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;

public final class AIWindow extends TopComponent {

 public AIWindow() {
 setName(NbBundle.getMessage(AIWindow.class, "AIWindow.displayName"));
 setToolTipText(NbBundle.getMessage(AIWindow.class, "AIWindow.tooltip"));
 setIconImage(new ImageIcon(getClass().getResource("/uno/anahata/netbeans/ai/resources/icon.png")).getImage());

 JLabel label = new JLabel("Hello from Anahata AI");
 label.setHorizontalAlignment(JLabel.CENTER);
 add(label, BorderLayout.CENTER);
 }

 @Override
 public int getPersistenceType() {
 return PERSISTENCE_NEVER;
 }

 @Override
 public HelpCtx getHelpCtx() {
 return new HelpCtx(this.getClass());
 }
}
