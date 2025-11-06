package uno.anahata.nb.ai;

import uno.anahata.nb.ai.mime.NetBeansEditorKitProvider;
import java.awt.BorderLayout;
import java.util.logging.Logger;
import lombok.extern.slf4j.Slf4j;

import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;
import uno.anahata.gemini.ui.GeminiPanel;

@ActionID(category = "Window", id = "uno.anahata.nb.ai.OpenAnahataAction")
@ActionReference(path = "Menu/Window", position = 333)
@TopComponent.Description(
        preferredID = "anahata",
        iconBase = "icons/anahata_16.png",
        persistenceType = TopComponent.PERSISTENCE_ALWAYS)
@TopComponent.Registration(mode = "output", openAtStartup = false, position = 0)
@TopComponent.OpenActionRegistration(displayName = "Anahata")
@Slf4j
public final class AnahataTopComponent extends TopComponent {

    //private static final Logger log = Logger.getLogger(AnahataTopComponent.class.getName());
    
    private transient GeminiPanel geminiPanel;

    public AnahataTopComponent() {
        logId("AnahataTopComponent()");
        setName("Anahata");
        setDisplayName("Anahata");
        setHtmlDisplayName("<html><font color='#00802b'>Anahata</font></html>");
        //setToolTipText("<font color='#00802b'>Anahata AI</font>");

    }

    public String getId() {
        return WindowManager.getDefault().findTopComponentID(this);
    }

    @Override
    public void componentOpened() {
        setName(getId());
        setDisplayName(getDisplayName() + " " + getId());
        //setDisplayName(getDisplayName() + " " + getId());
        setToolTipText(getId());
        
        logId("componentOpened()");
        log.info(Thread.currentThread().getName() + " " + System.identityHashCode(this) + " geminiPanel=" + geminiPanel);
        // Only create the panel if it's null. This preserves state on simple close/open.
        if (geminiPanel == null) {
            setName(getId());
            setDisplayName("Anahata - " + getId());
            setLayout(new BorderLayout());
            NetBeansGeminiConfig config = new NetBeansGeminiConfig(getId());
            geminiPanel = new GeminiPanel(new NetBeansEditorKitProvider());
            geminiPanel.init(config);
            add(geminiPanel, BorderLayout.CENTER);
            geminiPanel.initComponents();

            geminiPanel.checkAutobackupOrStartupContent();

        }
    }

    @Override
    public void componentClosed() {
        logId("componentClosed()");
        if (geminiPanel != null) {
            logId("componentClosed() geminiPanel was not null");
        } else {
            logId("componentClosed() exiting, geminiPanel was null");
        }
    }

    @Override
    protected void componentDeactivated() {
        //logId("componentDeactivated()");
        super.componentDeactivated();
    }

    @Override
    protected void componentActivated() {
        //logId("componentActivated()");
        super.componentActivated();
    }

    private void logId(String mssg) {
        log.info(Thread.currentThread().getName() + " hashCode" + System.identityHashCode(this) + ": " + mssg);
    }

    @Override
    public void requestVisible() {
        super.requestVisible(); 
    }

    @Override
    public void requestActive() {
        super.requestActive(); 
    }

    @Override
    public boolean requestFocusInWindow() {
        return super.requestFocusInWindow(); 
    }

    @Override
    public void requestFocus() {
        super.requestFocus(); 
    }

    @Override
    protected void componentHidden() {
        super.componentHidden(); 
    }

    @Override
    protected void componentShowing() {
        super.componentShowing(); 
        //logId("componentShowing()");
    }

    @Override
    public boolean canClose() {
        return super.canClose(); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/OverriddenMethodBody
    }

    
    @Override
    public void open() {
        super.open(); 
        logId("open()");
    }

}
