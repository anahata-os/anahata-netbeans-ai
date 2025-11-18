package uno.anahata.ai.nb;

import java.awt.BorderLayout;
import java.awt.Color;
import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import javax.swing.SwingUtilities;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;
import uno.anahata.ai.Chat;
import uno.anahata.ai.status.ChatStatus;
import uno.anahata.ai.status.StatusListener;
import uno.anahata.ai.swing.AnahataPanel;
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

    private static final List<AnahataTopComponent> ALL_SESSIONS = new CopyOnWriteArrayList<>();

    private transient AnahataPanel geminiPanel;
    private transient boolean isInitialized = false;

    @Getter
    @Setter
    private String sessionUuid;

    public AnahataTopComponent() {
        logId("AnahataTopComponent()");
        setName("Anahata");
        setDisplayName("Anahata");
        ALL_SESSIONS.add(this);
    }

    public static List<AnahataTopComponent> getAllSessions() {
        return Collections.unmodifiableList(ALL_SESSIONS);
    }

    public static void disposeSession(AnahataTopComponent tc) {
        if (tc != null) {
            tc.close(); // Ensure UI is closed first
            tc.performShutdown(); // Explicitly shut down the session
            ALL_SESSIONS.remove(tc);
        }
    }

    public String getTopComponentId() {
        return WindowManager.getDefault().findTopComponentID(this);
    }

    @Override
    public void componentOpened() {
        logId("componentOpened()");
        // This block runs only ONCE in the lifetime of the instance to create the session and UI.
        if (!isInitialized) {
            if (sessionUuid == null) {
                sessionUuid = UUID.randomUUID().toString();
                log.info("New session started with UUID: {}", sessionUuid);
            }
            setName(sessionUuid);
            setLayout(new BorderLayout());
            NetBeansChatConfig config = new NetBeansChatConfig(sessionUuid);
            geminiPanel = new AnahataPanel(new NetBeansEditorKitProvider());
            geminiPanel.init(config);
            geminiPanel.initComponents();
            add(geminiPanel, BorderLayout.CENTER); // Add the panel only once.
            geminiPanel.checkAutobackupOrStartupContent();
            getChat().addStatusListener(this);
            statusChanged(getChat().getStatusManager().getCurrentStatus(), null);
            isInitialized = true;
        }
    }

    @Override
    public void componentClosed() {
        logId("componentClosed()");
        // The UI panel is no longer removed. The NetBeans framework handles hiding/showing the TopComponent.
        // The underlying Chat session continues to run in the background.
    }

    /**
     * Performs the actual shutdown of the chat session. This should only be called
     * when the session is being permanently disposed of.
     */
    public void performShutdown() {
        logId("performShutdown()");
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
            if (getChat() == null) {
                return; // Guard against race conditions on close
            }
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