package uno.anahata.nb.ai;

import java.awt.BorderLayout;
import java.util.logging.Logger;
import javax.swing.JTabbedPane;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.windows.TopComponent;
import uno.anahata.gemini.ui.GeminiPanel;
import uno.anahata.gemini.ui.GeminiPanel2;

@ActionID(category = "Window", id = "uno.anahata.nb.ai.OpenGeminiAction")
@ActionReference(path = "Menu/Window", position = 333)
@TopComponent.Description(
        preferredID = "gemini",
        iconBase = "uno/anahata/nb/ai/gemini.png",
        persistenceType = TopComponent.PERSISTENCE_NEVER)
@TopComponent.Registration(mode = "output", openAtStartup = true)
@TopComponent.OpenActionRegistration(displayName = "Gemini Assistant", preferredID = "gemini")
/**
 * The plugins main component and integration point in the IDE.
 * Now contains a JTabbedPane to compare different UI implementations.
 */
public final class GeminiTopComponent extends TopComponent {

    private static final Logger log = Logger.getLogger(GeminiTopComponent.class.getName());

    // --- MODIFIED: Separate fields for each panel ---
    private GeminiPanel geminiPanel1; // The original JList implementation
    private GeminiPanel2 geminiPanel2; // The new JTextPane implementation
    private final GeminiConfigProviderImpl sysInsProvider = new GeminiConfigProviderImpl();

    public GeminiTopComponent() {
        log.info("init() -- entry ");
        setName("Gemini");
        setToolTipText("Get Gemini to do your work");
        initComponents();
        log.info("init() -- exit ");
    }

    // --- MODIFIED: Main initComponents now creates the tabbed pane ---
    private void initComponents() {
        setLayout(new BorderLayout());

        JTabbedPane tabbedPane = new JTabbedPane();

        // --- Tab 1: Original JList version ---
        geminiPanel1 = new GeminiPanel(sysInsProvider);
        geminiPanel1.initComponents();
        tabbedPane.addTab("V1 - JList", geminiPanel1);

        // --- Tab 2: New JTextPane version ---
        geminiPanel2 = new GeminiPanel2(sysInsProvider);
        geminiPanel2.initComponents();
        tabbedPane.addTab("V2 - JTextPane", geminiPanel2);

        add(tabbedPane, BorderLayout.CENTER);

        // Initialize both chats so they are ready to use
        geminiPanel1.initChat();
        geminiPanel2.initChat();
    }
    
    // --- Getters for individual panels (optional, but good practice) ---
    public GeminiPanel getGeminiPanel1() {
        return geminiPanel1;
    }

    public GeminiPanel2 getGeminiPanel2() {
        return geminiPanel2;
    }

    // --- Life-cycle methods remain the same ---
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
