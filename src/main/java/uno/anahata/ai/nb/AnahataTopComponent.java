package uno.anahata.ai.nb;

import java.awt.BorderLayout;
import java.awt.Color;
import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.UUID;
import javax.swing.SwingUtilities;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.windows.TopComponent;
import uno.anahata.gemini.Chat;
import uno.anahata.gemini.status.ChatStatus;
import uno.anahata.gemini.status.StatusListener;
import uno.anahata.gemini.ui.AnahataPanel;
import uno.anahata.ai.nb.mime.NetBeansEditorKitProvider;

@ActionID(category = "Window", id = "uno.anahata.nb.ai.OpenAnahataAction")
@ActionReference(path = "Menu/Window", position = 333)
@TopComponent.Description(
        preferredID = "anahata",
        iconBase = "icons/anahata_16.png",
        persistenceType = TopComponent.PERSISTENCE_ONLY_OPENED)
@TopComponent.Registration(mode = "output", openAtStartup = true, position = 0)
@TopComponent.OpenActionRegistration(displayName = "Anahata")
@Slf4j
public final class AnahataTopComponent extends TopComponent implements Externalizable, StatusListener {

    private transient AnahataPanel geminiPanel;
    
    @Getter
    @Setter
    private String sessionUuid;

    public AnahataTopComponent() {
        logId("AnahataTopComponent()");
        setName("Anahata");
        setDisplayName("Anahata");
    }

    @Override
    public void componentOpened() {
        if (sessionUuid == null) {
            sessionUuid = UUID.randomUUID().toString();
            log.info("New session started with UUID: {}", sessionUuid);
        }
        setName(sessionUuid);
        
        logId("componentOpened()");
        
        if (geminiPanel == null) {
            setLayout(new BorderLayout());
            NetBeansChatConfig config = new NetBeansChatConfig(sessionUuid);
            geminiPanel = new AnahataPanel(new NetBeansEditorKitProvider());
            geminiPanel.init(config);
            geminiPanel.initComponents();
            add(geminiPanel, BorderLayout.CENTER);
            geminiPanel.checkAutobackupOrStartupContent();
            getChat().addStatusListener(this);
            // Initial status update
            statusChanged(getChat().getStatusManager().getCurrentStatus(), null);
        }
    }

    @Override
    public void componentClosed() {
        logId("componentClosed()");
        if (geminiPanel != null && geminiPanel.getChat() != null) {
            getChat().removeStatusListener(this);
            geminiPanel.getChat().shutdown();
        }
    }
    
    public Chat getChat() {
        return geminiPanel != null ? geminiPanel.getChat() : null;
    }
    
    public NetBeansChatConfig getNetBeansChatConfig() {
        return (NetBeansChatConfig) getChat().getConfig();
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

    @Override
    public void statusChanged(ChatStatus newStatus, String lastExceptionToString) {
        SwingUtilities.invokeLater(() -> {
            if (getChat() == null) return; // Guard against race conditions on close

            Color color = getNetBeansChatConfig().getColor(newStatus);
            String hexColor = String.format("#%02x%02x%02x", color.getRed(), color.getGreen(), color.getBlue());

            String displayName = StringUtils.isNotBlank(getChat().getNickname())
                               ? getChat().getNickname()
                               : getChat().getShortId();
            
            String statusText = newStatus.getDisplayName();

            setDisplayName("Anahata - " + displayName);
            setHtmlDisplayName("<html><font color='" + hexColor + "'>Anahata - " + displayName + "</font></html>");

            String tooltip = "Anahata Session: " + sessionUuid + " [" + statusText + "]";
            if (lastExceptionToString != null) {
                tooltip += " - Error: " + lastExceptionToString;
            }
            setToolTipText(tooltip);
        });
    }
}
