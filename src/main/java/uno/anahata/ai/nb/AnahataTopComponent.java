/* Licensed under the Apache License, Version 2.0 */
package uno.anahata.ai.nb;

import com.google.genai.types.Content;
import com.google.genai.types.Part;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
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
import uno.anahata.ai.swing.ChatPanel;
import uno.anahata.ai.swing.SwingChatConfig;
import uno.anahata.ai.nb.mime.NetBeansEditorKitProvider;
import uno.anahata.ai.swing.IconUtils;

/**
 * The main TopComponent for the Anahata AI Assistant plugin.
 * This component hosts the chat interface and manages the lifecycle of the AI session.
 */
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

    private transient ChatPanel chatPanel;
    private transient boolean isInitialized = false;
    private transient String startupMessage;
    private transient List<Part> startupParts;
    private transient String nickname;

    @Getter
    @Setter
    private String sessionUuid;

    /**
     * Default constructor for AnahataTopComponent.
     * Initializes the component and adds it to the list of active sessions.
     */
    public AnahataTopComponent() {
        logId("AnahataTopComponent()");
        setName("Anahata");
        setDisplayName("Anahata");
        ALL_SESSIONS.add(this);
    }

    /**
     * Constructor that allows starting a session with an initial message and optional parts (like images).
     * @param initialMessage The first text message to send.
     * @param initialParts Optional parts (e.g., screenshots) to include in the first message.
     * @param nickname An optional nickname for the session.
     */
    public AnahataTopComponent(String initialMessage, List<Part> initialParts, String nickname) {
        this();
        this.startupMessage = initialMessage;
        this.startupParts = initialParts;
        this.nickname = nickname;
    }

    /**
     * Returns an unmodifiable list of all active AnahataTopComponent sessions.
     * @return a list of active sessions.
     */
    public static List<AnahataTopComponent> getAllSessions() {
        return Collections.unmodifiableList(ALL_SESSIONS);
    }

    /**
     * Disposes of a specific AnahataTopComponent session, closing its UI and shutting down its chat session.
     * @param tc the TopComponent to dispose.
     */
    public static void disposeSession(AnahataTopComponent tc) {
        if (tc != null) {
            tc.close(); // Ensure UI is closed first
            tc.performShutdown(); // Explicitly shut down the session
            ALL_SESSIONS.remove(tc);
        }
    }

    /**
     * Returns the unique ID of this TopComponent as registered in the WindowManager.
     * @return the TopComponent ID.
     */
    public String getTopComponentId() {
        return WindowManager.getDefault().findTopComponentID(this);
    }

    @Override
    public Action[] getActions() {
        List<Action> actions = new ArrayList<>(Arrays.asList(super.getActions()));
        actions.add(null); // Separator
        AbstractAction setNicknameAction = new AbstractAction("Set Nickname...") {
            @Override
            public void actionPerformed(ActionEvent e) {
                String currentNickname = getChat().getNickname();
                String newNickname = JOptionPane.showInputDialog(WindowManager.getDefault().getMainWindow(),
                        "Enter a nickname for this session:",
                        currentNickname != null ? currentNickname : "");
                if (newNickname != null) {
                    log.info("Changing nickname for session {} to: {}", sessionUuid, newNickname);
                    getChat().setNickname(newNickname);
                    updateTitleAndTooltip(getChat().getStatusManager().getCurrentStatus(), null);
                }
            }
        };
        setNicknameAction.putValue(Action.SMALL_ICON, IconUtils.getIcon("anahata_16.png"));
        actions.add(setNicknameAction);
        return actions.toArray(new Action[0]);
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
            
            chatPanel = new ChatPanel(config, new NetBeansEditorKitProvider());
            add(chatPanel, BorderLayout.CENTER); 
            
            getChat().addStatusListener(this);
            
            if (nickname != null) {
                getChat().setNickname(nickname);
            }

            // Handle programmatic startup content vs automatic backup/startup check
            if (StringUtils.isNotBlank(startupMessage) || (startupParts != null && !startupParts.isEmpty())) {
                log.info("Programmatic start detected. Skipping backup check.");
                final String msg = startupMessage;
                final List<Part> parts = startupParts != null ? new ArrayList<>(startupParts) : new ArrayList<>();
                
                new SwingWorker<Void, Void>() {
                    @Override
                    protected Void doInBackground() throws Exception {
                        List<Part> allParts = new ArrayList<>();
                        if (StringUtils.isNotBlank(msg)) {
                            allParts.add(Part.fromText(msg));
                        }
                        allParts.addAll(parts);
                        getChat().sendContent(Content.builder().role("user").parts(allParts).build());
                        return null;
                    }
                }.execute();
                
                startupMessage = null;
                startupParts = null;
            } else {
                chatPanel.checkAutobackupOrStartupContent();
            }

            updateTitleAndTooltip(getChat().getStatusManager().getCurrentStatus(), null);
            isInitialized = true;
        }
    }

    @Override
    public void componentClosed() {
        logId("componentClosed()");
    }

    /**
     * Performs the actual shutdown of the chat session. This should only be called
     * when the session is being permanently disposed of.
     */
    public void performShutdown() {
        logId("performShutdown()");
        if (chatPanel != null && chatPanel.getChat() != null) {
            getChat().removeStatusListener(this);
            chatPanel.getChat().shutdown();
        }
    }

    /**
     * Returns the Chat instance associated with this TopComponent.
     * @return the Chat instance, or null if not initialized.
     */
    public Chat getChat() {
        return chatPanel != null ? chatPanel.getChat() : null;
    }

    /**
     * Returns the NetBeans-specific chat configuration.
     * @return the NetBeansChatConfig instance.
     */
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

    /**
     * Sets the session UUID for handoff between components.
     * @param sessionUuid the UUID to set.
     */
    public void setSessionUuidForHandoff(String sessionUuid) {
        this.sessionUuid = sessionUuid;
        log.info("Session UUID set for handoff: {}", sessionUuid);
    }

    @Override
    public void statusChanged(ChatStatus newStatus, String lastExceptionToString) {
        SwingUtilities.invokeLater(() -> updateTitleAndTooltip(newStatus, lastExceptionToString));
    }

    private void updateTitleAndTooltip(ChatStatus status, String error) {
        if (getChat() == null) return;

        Color color = SwingChatConfig.getColor(status);
        String hexColor = String.format("#%02x%02x%02x", color.getRed(), color.getGreen(), color.getBlue());
        String displayName = getChat().getDisplayName();

        setDisplayName("Anahata - " + displayName);
        setHtmlDisplayName("<html><font color='" + hexColor + "'>Anahata - " + displayName + "</font></html>");

        String tooltip = "Anahata Session: " + sessionUuid + " [" + status.getDisplayName() + "]";
        if (error != null) {
            tooltip += " - Error: " + error;
        }
        setToolTipText(tooltip);
    }
}
