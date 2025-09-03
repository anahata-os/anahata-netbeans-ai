package uno.anahata.nb.ai;

import java.awt.Image;
import uno.anahata.gemini.ui.GeminiPanel;
import java.util.logging.Logger;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.SwingConstants;

import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.windows.TopComponent;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.Utilities;




@ActionID(category = "Window", id = "uno.anahata.nb.ai.OpenGeminiAction")
@ActionReference(path = "Menu/Window", position = 333)
@TopComponent.Description(
        preferredID = "gemini",
        iconBase = "uno/anahata/nb/ai/gemini.png",
        persistenceType = TopComponent.PERSISTENCE_ONLY_OPENED)
@TopComponent.Registration(mode = "output", openAtStartup = true)
@TopComponent.OpenActionRegistration(displayName = "Gemini Assistant",preferredID = "gemini")
public final class GeminiTopComponent extends TopComponent  {

    private static final Logger log = Logger.getLogger(GeminiTopComponent.class.getName());

    private GeminiPanel gemini;
    private SystemInstructionProviderImpl systemInstructionProviderImpl = new SystemInstructionProviderImpl();

    public GeminiTopComponent() {
        log.info("init() -- entry");
        setName("Gemini");
        setToolTipText("Get Gemini to do your work");
        initComponents();
        log.info("init() -- exit");
    }

    private void initComponents() {

        gemini = new GeminiPanel(systemInstructionProviderImpl, null);
        setLayout(new java.awt.BorderLayout());
        add(gemini, java.awt.BorderLayout.CENTER);
        
    }
    
    
    @Override
    public void componentClosed() {
        log.info("super.componentClosed(); ");
        super.componentClosed();
    
    }

    @Override
    protected void componentDeactivated() {
        log.info("super.componentDeactivated(); ");
        super.componentDeactivated(); 
    }

    @Override
    protected void componentActivated() {
        log.info("super.componentActivated(); ");
        super.componentActivated(); 
    }

    @Override
    protected void componentHidden() {
        log.info("super.componentHidden(); ");
        super.componentHidden(); 
    }

    @Override
    protected void componentShowing() {
        log.info("super.componentShowing(); ");
        super.componentShowing(); 
    }

    @Override
    protected void componentOpened() {
        log.info("super.componentOpened(); ");
        super.componentOpened(); 
    }
    
    

    

}
