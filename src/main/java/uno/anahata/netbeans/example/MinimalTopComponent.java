package uno.anahata.netbeans.example;

import javax.swing.JLabel;
import org.openide.util.NbBundle.Messages;
import org.openide.windows.TopComponent;

@TopComponent.Registration(
 displayName = "#CTL_MinimalTopComponent",
 iconBase = "path/to/icon.png",
 mimeType = "text/plain",
 mode = "editor",
 openAtStartup = false
)
@TopComponent.Description(
 displayName = "#CTL_MinimalTopComponent",
 iconBase = "path/to/icon.png"
)
@Messages({
 "CTL_MinimalTopComponent=Minimal Top Component Updated"
})
public class MinimalTopComponent extends TopComponent {

 public MinimalTopComponent() {
 setName(Bundle.CTL_MinimalTopComponent());
 setToolTipText(Bundle.CTL_MinimalTopComponent());
 add(new JLabel(Bundle.CTL_MinimalTopComponent()));
 }
}
