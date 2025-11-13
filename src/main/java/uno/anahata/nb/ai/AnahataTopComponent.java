package uno.anahata.nb.ai;

import java.awt.BorderLayout;
import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.windows.TopComponent;
import uno.anahata.gemini.ui.GeminiPanel;
import uno.anahata.nb.ai.mime.NetBeansEditorKitProvider;

@ActionID(category = "Window", id = "uno.anahata.nb.ai.OpenAnahataAction")
@ActionReference(path = "Menu/Window", position = 333)
@TopComponent.Description(
        preferredID = "anahata",
        iconBase = "icons/anahata_16.png",
        persistenceType = TopComponent.PERSISTENCE_ALWAYS)
@TopComponent.Registration(mode = "output", openAtStartup = true, position = 0)
@TopComponent.OpenActionRegistration(displayName = "Anahata")
@Slf4j
public final class AnahataTopComponent extends TopComponent implements Externalizable {

    private transient GeminiPanel geminiPanel;
    
    @Getter
    @Setter
    private String sessionUuid;

    public AnahataTopComponent() {
        logId("AnahataTopComponent()");
        setName("Anahata");
        setDisplayName("Anahata");
        setHtmlDisplayName("<html><font color='#00802b'>Anahata</font></html>");
    }

    @Override
    public void componentOpened() {
        if (sessionUuid == null) {
            sessionUuid = UUID.randomUUID().toString();
            log.info("New session started with UUID: {}", sessionUuid);
        }
        setName(sessionUuid);
        setDisplayName("Anahata - " + sessionUuid.substring(0, 8));
        setToolTipText("Anahata Session: " + sessionUuid);
        
        logId("componentOpened()");
        
        if (geminiPanel == null) {
            setLayout(new BorderLayout());
            NetBeansChatConfig config = new NetBeansChatConfig(sessionUuid);
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
        if (geminiPanel != null && geminiPanel.getChat() != null) {
            geminiPanel.getChat().shutdown();
        }
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeObject(sessionUuid);
        super.writeExternal(out);
        log.info("Persisted session UUID: {}", sessionUuid);
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        sessionUuid = (String) in.readObject();
        super.readExternal(in);
        log.info("Restored session UUID: {}", sessionUuid);
    }

    private void logId(String mssg) {
        String id = (sessionUuid != null) ? sessionUuid : "NULL_UUID";
        log.info(Thread.currentThread().getName() + " hashCode=" + System.identityHashCode(this) + " sessionUuid=" + id + ": " + mssg);
    }
    
    public void setSessionUuidForHandoff(String sessionUuid) {
        this.sessionUuid = sessionUuid;
        log.info("Session UUID set for handoff: {}", sessionUuid);
    }
}
