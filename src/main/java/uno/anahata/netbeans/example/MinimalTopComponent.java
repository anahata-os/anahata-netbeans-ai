package uno.anahata.netbeans.example;

import javax.swing.JLabel;
import org.openide.windows.TopComponent;

@TopComponent.Registration(displayName = "Minimal TopComponent", 
 iconBase = "path/to/icon.png", 
 mimeType = "text/plain")
public class MinimalTopComponent extends TopComponent {

 public MinimalTopComponent() {
 setName("Minimal TopComponent");
 setToolTipText("This is a minimal TopComponent");
 add(new JLabel("Hello, World!"));
 }
}
