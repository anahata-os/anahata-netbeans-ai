package uno.anahata.nb.ai;

import java.awt.BorderLayout;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.windows.TopComponent;
import uno.anahata.gemini.GeminiChat;
import uno.anahata.gemini.ui.GeminiPanel;
import uno.anahata.nb.ai.context.ContextFiles;
import uno.anahata.nb.ai.gemini.NetBeansGeminiConfig;


@ActionID(category = "Window", id = "uno.anahata.nb.ai.OpenAnahataAction")
@ActionReference(path = "Menu/Window", position = 333)
@TopComponent.Description(
        preferredID = "anahata",
        iconBase = "icons/anahata_16.png",
        persistenceType = TopComponent.PERSISTENCE_NEVER)
@TopComponent.Registration(mode = "output", openAtStartup = false)
@TopComponent.OpenActionRegistration(displayName = "Anahata", preferredID = "open_anahata")
public final class AnahataTopComponent extends TopComponent {

    private GeminiPanel geminiPanel;

    public AnahataTopComponent() {
        setName("Anahata");
        setToolTipText("Anahata AI Assistant");
    }

    @Override
    public void componentOpened() {
        setLayout(new BorderLayout());
        NetBeansGeminiConfig config = new NetBeansGeminiConfig();
        geminiPanel = new GeminiPanel(new NetBeansEditorKitProvider());
        geminiPanel.init(config);
        add(geminiPanel, BorderLayout.CENTER);
        geminiPanel.initComponents();
        
        // Register our context file listener
        //GeminiChat chat = geminiPanel.getChat();
        //ContextFiles contextFiles = ContextFiles.getInstance();
        //contextFiles.setFunctionManager(chat.getFunctionManager());
        //chat.getContextManager().addListener(contextFiles);
        
        geminiPanel.initChatInSwingWorker();
        
        // Manually trigger a rescan in case context was loaded from history
        //contextFiles.contextChanged(chat);
    }

    @Override
    public void componentClosed() {
        if (geminiPanel != null) {
            GeminiChat chat = geminiPanel.getChat();
            if (chat != null) {
                chat.getContextManager().removeListener(ContextFiles.getInstance());
            }
        }
    }
}
