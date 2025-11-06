package uno.anahata.nb.ai.tools;

import java.awt.Component;
import java.util.ArrayList;
import java.util.List;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;
import uno.anahata.gemini.functions.AIToolMethod;

/**
 *
 * @author pablo
 */
public class Output {

    @AIToolMethod("Lists all java.awt.Component(s) in the 'output' TopComponent" )
    public static List<String> getOutputComponents() {
        List<String> tabNames = new ArrayList<>();
        TopComponent outputTC = WindowManager.getDefault().findTopComponent("output"); // "output" ID
        if (outputTC != null) {
            java.awt.Component[] comps = outputTC.getComponents();
            for (java.awt.Component c : comps) {
                tabNames.add(c.getClass() + " -> " + c);
            }
        }
        return tabNames;
    }

}
