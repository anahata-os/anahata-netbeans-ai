package uno.anahata.netbeans.ai;

import javax.swing.*;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.windows.TopComponent;

@TopComponent.Registration(displayName = "#ChatTopComponent.displayName",
        iconBase = "path/to/icon.png",
        persistenceType = TopComponent.PERSISTENCE_NEVER)
public class ChatTopComponent extends TopComponent {

    public ChatTopComponent() {
        setName(NbBundle.getMessage(ChatTopComponent.class, "ChatTopComponent.displayName"));
        setToolTipText(NbBundle.getMessage(ChatTopComponent.class, "ChatTopComponent.toolTip"));
        setIconImage(new ImageIcon(Utilities.loadImage("path/to/icon.png", true)).getImage());
    }

    @Override
    public void componentOpened() {
        // Create a chat box
        JTextArea chatArea = new JTextArea(20, 40);
        chatArea.setEditable(false);

        JTextField inputField = new JTextField();
        JButton sendButton = new JButton("Send");

        // Add action listener to send button
        sendButton.addActionListener(e -> {
            String input = inputField.getText();
            chatArea.append("You: " + input + "\n");
            inputField.setText("");

            // Simulate AI response
            String response = "AI: I'm here to help!";
            chatArea.append(response + "\n");
        });

        // Add components to panel
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.add(new JScrollPane(chatArea), BorderLayout.CENTER);
        JPanel inputPanel = new JPanel();
        inputPanel.add(inputField);
        inputPanel.add(sendButton);
        panel.add(inputPanel, BorderLayout.SOUTH);

        add(panel);
    }

    @Override
    public void componentClosed() {
    }

    @Override
    public void componentShowing() {
    }

    @Override
    public void componentHidden() {
    }

    @Override
    public void componentActivated() {
    }

    @Override
    public void componentDeactivated() {
    }
}
