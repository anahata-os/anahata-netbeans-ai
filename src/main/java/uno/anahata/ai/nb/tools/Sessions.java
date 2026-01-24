/* Licensed under the Apache License, Version 2.0 */
package uno.anahata.ai.nb.tools;

import com.google.genai.types.Part;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.swing.SwingUtilities;
import uno.anahata.ai.Chat;
import uno.anahata.ai.internal.PartUtils;
import uno.anahata.ai.nb.AnahataTopComponent;
import uno.anahata.ai.tools.AIToolMethod;

/**
 * A tool for managing and inspecting active Anahata AI sessions within NetBeans.
 */
public class Sessions {

    /**
     * Lists all active Anahata AI sessions in the IDE with detailed status information.
     * @return A list of session status strings.
     */
    @AIToolMethod("Lists all active Anahata AI sessions in the IDE.")
    public static List<String> listActiveSessions() {
        return AnahataTopComponent.getAllSessions().stream()
                .map(tc -> {
                    Chat chat = tc.getChat();
                    if (chat == null) {
                        return String.format("ID: %s, Status: Initializing...", tc.getSessionUuid());
                    }
                    return String.format("ID: %s, Name: %s, Status: %s, Messages: %d, Context: %s",
                            tc.getSessionUuid(),
                            chat.getDisplayName(),
                            chat.getStatusManager().getCurrentStatus().getDisplayName(),
                            chat.getContext().size(),
                            chat.getContextWindowUsageFormatted());
                })
                .collect(Collectors.toList());
    }

    /**
     * Gets a detailed, forensic-grade dump of another active session's context by its UUID.
     * @param sessionId The UUID of the session to dump.
     * @return The detailed Markdown dump of the session.
     * @throws IllegalArgumentException if the session is not found.
     */
    @AIToolMethod("Gets a detailed, forensic-grade dump of another active session's context by its UUID.")
    public static String dumpSession(String sessionId) {
        AnahataTopComponent target = AnahataTopComponent.getAllSessions().stream()
                .filter(tc -> sessionId.equals(tc.getSessionUuid()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Session not found: " + sessionId));

        if (target.getChat() == null) {
            return "Session " + sessionId + " is still initializing.";
        }

        return target.getChat().getContextManager().getSessionManager().getDetailedDump();
    }

    /**
     * Starts a new Anahata AI session with an initial user message and optional file attachments.
     * @param initialMessage The first message to send to the new session.
     * @param nickname An optional nickname for the new session.
     * @param attachmentPaths A list of absolute file paths to attach to the first message.
     * @return The UUID of the newly created session.
     * @throws IOException if an attachment cannot be processed.
     */
    @AIToolMethod("Starts a new Anahata AI session with an initial user message.")
    public static String startSession(final String initialMessage, final String nickname, List<String> attachmentPaths) throws IOException {
        final String uuid = UUID.randomUUID().toString();
        List<Part> parts = new ArrayList<>();
        if (attachmentPaths != null) {
            for (String path : attachmentPaths) {
                parts.add(PartUtils.toPart(new File(path)));
            }
        }
        
        SwingUtilities.invokeLater(() -> {
            AnahataTopComponent tc = new AnahataTopComponent(initialMessage, parts, nickname);
            tc.setSessionUuid(uuid);
            tc.open();
            tc.requestActive();
        });
        return uuid;
    }
}
