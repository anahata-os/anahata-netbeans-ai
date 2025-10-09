package uno.anahata.nb.ai;

import java.awt.BorderLayout;

import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.windows.TopComponent;
import uno.anahata.gemini.ui.GeminiPanel;
import uno.anahata.nb.ai.gemini.NetBeansGeminiConfig;


@ActionID(category = "Window", id = "uno.anahata.nb.ai.OpenGeminiAction")
@ActionReference(path = "Menu/Window", position = 333)
@TopComponent.Description(
        preferredID = "gemini",
        iconBase = "icons/anahata_16.png",
        persistenceType = TopComponent.PERSISTENCE_NEVER)
@TopComponent.Registration(mode = "output", openAtStartup = false)
@TopComponent.OpenActionRegistration(displayName = "Anahata", preferredID = "anahata")
public final class AnahataTopComponent extends TopComponent {

    private GeminiPanel geminiPanel;

    public AnahataTopComponent() {
        setName("Anahata");
        setToolTipText("Anahata AI Assistant");
    }

    @Override
    public void componentOpened() {
        if (geminiPanel == null) {
            setLayout(new BorderLayout());
            NetBeansGeminiConfig config = new NetBeansGeminiConfig();
            geminiPanel = new GeminiPanel(new NetBeansEditorKitProvider());
            geminiPanel.init(config);
            add(geminiPanel, BorderLayout.CENTER);
            geminiPanel.initComponents();
            geminiPanel.initChatInSwingWorker();
        }
    }

    @Override
    public void componentClosed() {
        // Consider cleanup tasks here if necessary
    }


}
