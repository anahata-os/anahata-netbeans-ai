package uno.anahata.nb.ai;

import java.awt.BorderLayout;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JTextArea;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.windows.TopComponent;
import uno.anahata.gemini.functions.spi.ExecuteJavaCode;
import uno.anahata.gemini.ui.GeminiPanel;

@ActionID(category = "Window", id = "uno.anahata.nb.ai.OpenGeminiAction")
@ActionReference(path = "Menu/Window", position = 333)
@TopComponent.Description(
        preferredID = "gemini",
        iconBase = "uno/anahata/nb/ai/gemini.png",
        persistenceType = TopComponent.PERSISTENCE_NEVER)
@TopComponent.Registration(mode = "output", openAtStartup = true)
@TopComponent.OpenActionRegistration(displayName = "Gemini Assistant", preferredID = "gemini")
public final class GeminiTopComponent extends TopComponent {

    private static final Logger log = Logger.getLogger(GeminiTopComponent.class.getName());
    private static boolean initialized = false;

    private GeminiPanel geminiPanel;
    private final GeminiConfigProviderImpl sysInsProvider = new GeminiConfigProviderImpl();

    public GeminiTopComponent() {
        log.info("init() -- entry ");
        setName("Gemini");
        setToolTipText("Get Gemini to do your work");
        initComponents();
        log.info("init() -- exit ");
    }
    
    public GeminiPanel getGeminiPanel() {
        return geminiPanel;
    }
    

    private void initComponents() {
        setLayout(new java.awt.BorderLayout());
        geminiPanel = new GeminiPanel(sysInsProvider);        
        add(geminiPanel, java.awt.BorderLayout.CENTER);
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
        log.info("super.componentShowing();");
        super.componentShowing();
    }

    @Override
    public void componentOpened() {
        log.info("super.componentOpened();");
        super.componentOpened();
        
        
    }
}
