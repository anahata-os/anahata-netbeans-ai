package uno.anahata.nb.ai;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.logging.Logger;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle.Messages;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;
/*
@ActionID(
        category = "Window",
        id = "uno.anahata.nb.ai.GeminiToolbarAction"
)
@ActionRegistration(
        iconBase = "uno/anahata/nb/ai/gemini.png",
        displayName = "#CTL_GeminiToolbarAction"
)
@ActionReference(path = "Toolbars/File") // You can change the toolbar and position
@Messages("CTL_GeminiToolbarAction=Open Gemini Tab")
*/
public final class GeminiToolbarAction implements ActionListener {

    private static final Logger LOG = Logger.getLogger(GeminiToolbarAction.class.getName());
    
    @Override
    public void actionPerformed(ActionEvent e) {
        TopComponent tc = WindowManager.getDefault().findTopComponent("Gemini");
        if (tc == null) {
            LOG.info("passively creating ChatTopComponent");
            // This should ideally not happen if ChatTopComponent is properly registered
            // and its preferredID is "ChatTopComponent"
            tc = new GeminiTopComponent(); // Fallback, though not ideal for TopComponents
            
        } else {
            if (!tc.isOpened()) {
                LOG.info("Calling ChatTopComponent.open()");
                tc.open();
            }
            LOG.info("Calling ChatTopComponent.requestActive()");
            tc.requestActive();
        }
    }
}
