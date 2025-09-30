package uno.anahata.nb.ai;

import uno.anahata.nb.ai.gemini.NetBeansGeminiConfig;
import java.awt.BorderLayout;
import java.util.logging.Logger;
import javax.swing.JTabbedPane;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.windows.TopComponent;
import uno.anahata.gemini.ui.GeminiPanel;


@ActionID(category = "Window", id = "uno.anahata.nb.ai.OpenGeminiAction")
@ActionReference(path = "Menu/Window", position = 333)
@TopComponent.Description(
        preferredID = "gemini",
        iconBase = "icons/anahata_32.png",
        persistenceType = TopComponent.PERSISTENCE_NEVER)
@TopComponent.Registration(mode = "output", openAtStartup = false)
@TopComponent.OpenActionRegistration(displayName = "Anahata", preferredID = "anahata")
public final class AnahataTopComponent extends TopComponent {

    private static final Logger logger = Logger.getLogger(AnahataTopComponent.class.getName());

    public GeminiPanel geminiPanel;
    
    private final NetBeansGeminiConfig config = new NetBeansGeminiConfig();

    public AnahataTopComponent() {
        logger.info("init() -- entry ");
        setName("Anahata");
        setToolTipText("Get Gemini to do your work");
        
        initComponents();
        logger.info("init() -- exit ");
    }

    private void initComponents() {
        setLayout(new BorderLayout());

        JTabbedPane tabbedPane = new JTabbedPane();
        add(tabbedPane, BorderLayout.CENTER);
        
        geminiPanel = new GeminiPanel(new NetBeansEditorKitProvider());    
        geminiPanel.init(config);
        
        tabbedPane.addTab("Gemini", geminiPanel);
        geminiPanel.initComponents();
        geminiPanel.initChatInSwingWorker();
    }
    
    
    @Override
    public void componentClosed() {
        logger.info("componentClosed(); ");
        super.componentClosed();
    }

    @Override
    protected void componentDeactivated() {
        logger.info("componentDeactivated(); ");
        super.componentDeactivated();
    }

    @Override
    protected void componentActivated() {
        logger.info("componentActivated(); ");
        super.componentActivated();
    }

    @Override
    protected void componentHidden() {
        logger.info("componentHidden(); ");
        super.componentHidden();
    }

    @Override
    protected void componentShowing() {
        logger.info("componentShowing();");
        super.componentShowing();
    }

    @Override
    public void componentOpened() {
        logger.info("componentOpened();");
        super.componentOpened();
    }
}
