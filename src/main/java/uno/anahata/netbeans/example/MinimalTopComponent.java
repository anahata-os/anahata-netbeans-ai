package uno.anahata.netbeans.example;

import org.openide.util.lookup.Lookups;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

@TopComponent.Registration(displayName = "Minimal TopComponent", 
                          iconBase = "path/to/icon.png", 
                          mimeType = "text/plain")
public class MinimalTopComponent extends TopComponent {

    public MinimalTopComponent() {
        setName("Minimal TopComponent");
        setToolTipText("This is a minimal TopComponent");
        add(new javax.swing.JLabel("Hello, World!"));
    }
}
