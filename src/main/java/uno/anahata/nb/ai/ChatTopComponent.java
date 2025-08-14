package uno.anahata.nb.ai;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import javax.swing.*;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.windows.TopComponent;

@TopComponent.Description(
        preferredID = "ChatTopComponent",
        iconBase = "uno/anahata/nb/ai/chat-icon.png",
        persistenceType = TopComponent.PERSISTENCE_ALWAYS
)
@TopComponent.Registration(mode = "output", openAtStartup = false)
@ActionID(category = "Window", id = "uno.anahata.nb.ai.ChatTopComponent")
@ActionReference(path = "Menu/Window", position = 333)
@TopComponent.OpenActionRegistration(
        displayName = "LLM Chat",
        preferredID = "ChatTopComponent"
)
public final class ChatTopComponent extends TopComponent {
    
    private JTextArea chatArea;
    private JTextField inputField;
    private JButton sendButton;
    
    public ChatTopComponent() {
        initComponents();
        setName("LLM Chat Window");
        setToolTipText("Chat with AI Assistant");
    }
    
    private void initComponents() {
        setLayout(new BorderLayout());
        
        // Chat display area
        chatArea = new JTextArea();
        chatArea.setEditable(false);
        chatArea.setRows(20);
        chatArea.setColumns(50);
        chatArea.setLineWrap(true);
        chatArea.setWrapStyleWord(true);
        chatArea.setText("Welcome to LLM Chat!\nType your message below and press Enter or click Send.\n\n");
        
        JScrollPane scrollPane = new JScrollPane(chatArea);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        add(scrollPane, BorderLayout.CENTER);
        
        // Input panel
        JPanel inputPanel = new JPanel(new BorderLayout());
        
        inputField = new JTextField();
        inputField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    sendMessage();
                }
            }
        });
        
        sendButton = new JButton("Send");
        sendButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sendMessage();
            }
        });
        
        inputPanel.add(inputField, BorderLayout.CENTER);
        inputPanel.add(sendButton, BorderLayout.EAST);
        
        add(inputPanel, BorderLayout.SOUTH);
        
        // Focus on input field when window opens
        SwingUtilities.invokeLater(() -> inputField.requestFocus());
    }
    
    private void sendMessage() {
        String message = inputField.getText().trim();
        if (!message.isEmpty()) {
            // Add user message to chat
            appendToChatArea("You: " + message + "\n");
            
            // Clear input field
            inputField.setText("");
            
            // Simulate AI response (replace this with actual LLM integration later)
            simulateAIResponse(message);
        }
    }
    
    private void appendToChatArea(String text) {
        SwingUtilities.invokeLater(() -> {
            chatArea.append(text);
            chatArea.setCaretPosition(chatArea.getDocument().getLength());
        });
    }
    
    private void simulateAIResponse(String userMessage) {
        // Simulate thinking delay
        SwingUtilities.invokeLater(() -> {
            appendToChatArea("AI: I received your message: \"" + userMessage + "\"\n");
            appendToChatArea("AI: This is a placeholder response. LLM integration will be added later.\n\n");
        });
    }
    
    @Override
    public void componentOpened() {
        // Called when the component is opened
        super.componentOpened();
        inputField.requestFocus();
    }
    
    @Override
    public void componentClosed() {
        // Called when the component is closed
        super.componentClosed();
    }
    
    void writeProperties(java.util.Properties p) {
        // Better to use properties files for storing settings
        p.setProperty("version", "1.0");
    }
    
    void readProperties(java.util.Properties p) {
        String version = p.getProperty("version");
    }
}
