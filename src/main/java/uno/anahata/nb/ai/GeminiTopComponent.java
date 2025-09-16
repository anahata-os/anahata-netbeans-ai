package uno.anahata.nb.ai;

import java.awt.BorderLayout;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JEditorPane;
import javax.swing.JTabbedPane;
import javax.swing.text.Document;
import javax.swing.text.EditorKit;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.TokenHierarchy;
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
        
        initComponents();
        logger.info("init() -- exit ");
    }

    private void initComponents() {
        setLayout(new BorderLayout());

        JTabbedPane tabbedPane = new JTabbedPane();
        add(tabbedPane, BorderLayout.CENTER);
        
        CodeBlockRenderer netbeansRenderer = new NetBeansCodeBlockRenderer();
        geminiPanel = new GeminiPanel();    
        geminiPanel.setCodeBlockRenderer(netbeansRenderer);
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
