package uno.anahata.nb.ai;

import java.awt.BorderLayout;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JEditorPane;
import javax.swing.JTabbedPane;
import javax.swing.text.EditorKit;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.windows.TopComponent;

import uno.anahata.gemini.ui.CodeBlockRenderer;
import uno.anahata.gemini.ui.GeminiPanel;


@ActionID(category = "Window", id = "uno.anahata.nb.ai.OpenGeminiAction")
@ActionReference(path = "Menu/Window", position = 333)
@TopComponent.Description(
        preferredID = "gemini",
        iconBase = "uno/anahata/nb/ai/gemini.png",
        persistenceType = TopComponent.PERSISTENCE_NEVER)
@TopComponent.Registration(mode = "output", openAtStartup = false)
@TopComponent.OpenActionRegistration(displayName = "Gemini Assistant", preferredID = "gemini")
public final class GeminiTopComponent extends TopComponent {

    private static final Logger logger = Logger.getLogger(GeminiTopComponent.class.getName());

    public GeminiPanel geminiPanel;
    
    private final GeminiConfigImpl config = new GeminiConfigImpl();

    public GeminiTopComponent() {
        logger.info("init() -- entry ");
        setName("Gemini");
        setToolTipText("Get Gemini to do your work");
        
        // --- WARM-UP CODE ---
        // Force the Java EditorKit and its associated lexer to load before the UI is built.
        // This resolves a race condition where syntax highlighting would not be ready for the first message.
        try {
            logger.info("Warming up Java EditorKit...");
            JEditorPane warmupPane = new JEditorPane();
            EditorKit kit = MimeLookup.getLookup("text/x-java").lookup(EditorKit.class);
            if (kit != null) {
                warmupPane.setEditorKit(kit);
                logger.info("Java EditorKit successfully warmed up.");
            } else {
                logger.warning("Failed to find Java EditorKit during warm-up.");
            }
        } catch (Exception e) {
            logger.log(Level.WARNING, "Exception during Java EditorKit warm-up", e);
        }
        // --- END WARM-UP CODE ---
        
        initComponents();
        logger.info("init() -- exit ");
    }

    private void initComponents() {
        setLayout(new BorderLayout());

        JTabbedPane tabbedPane = new JTabbedPane();
        add(tabbedPane, BorderLayout.CENTER);
        
        CodeBlockRenderer netbeansRenderer = new NetBeansCodeBlockRenderer();
        geminiPanel = new GeminiPanel();    
        geminiPanel.init(config);
        geminiPanel.setCodeBlockRenderer(netbeansRenderer);
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
