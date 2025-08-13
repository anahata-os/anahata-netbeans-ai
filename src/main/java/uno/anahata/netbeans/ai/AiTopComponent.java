/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package uno.anahata.netbeans.ai;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.logging.Logger;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTextField;
import org.netbeans.api.annotations.common.StaticResource;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.util.NbBundle.Messages;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

/**
 * Minimal TopComponent that shows a text field + Send button.
 * <p>
 * The component is registered with annotations only (no layer.xml),
 * appears under Window → Open → AI Chat, and remembers its opened/closed
 * state automatically.
 */
@TopComponent.Description(
        preferredID = "AiTopComponent",
        // optional icon – replace with your own if you like
        iconBase = "com/mycompany/ai/ai_icon.png",
        persistenceType = TopComponent.PERSISTENCE_ALWAYS
)
@TopComponent.Registration(
        mode = "output",   // show in the Output area; change to "editor", "explorer", etc. if you prefer
        openAtStartup = false
)
@ActionID(
        category = "Window",
        id = "com.mycompany.ai.AiTopComponent"
)
@ActionReferences({
        @ActionReference(
                path = "Menu/Window",
                position = 1300
        ),
        @ActionReference(
                path = "Toolbars/Window",
                position = 1300
        )
})
@Messages({
        "CTL_AiTopComponent=AI Chat",
        "CTL_AiTopComponent_Tooltip=Open a simple AI chat UI"
})
public final class AiTopComponent extends TopComponent {

    private static final Logger LOG = Logger.getLogger(AiTopComponent.class.getName());

    private final JTextField inputField = new JTextField();
    private final JButton sendButton = new JButton(Bundle.CTL_AiTopComponent()); // button text = "AI Chat"
    private final JPanel panel = new JPanel(new BorderLayout(5, 5));

    public AiTopComponent() {
        setName(Bundle.CTL_AiTopComponent());
        setToolTipText(Bundle.CTL_AiTopComponent_Tooltip());

        // Build the UI
        panel.add(inputField, BorderLayout.CENTER);
        panel.add(sendButton, BorderLayout.EAST);
        add(panel, BorderLayout.NORTH);

        // When the user presses ENTER in the text field or clicks the button
        ActionListener sendAction = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String text = inputField.getText().trim();
                if (!text.isEmpty()) {
                    // Here you would normally forward the message to your AI backend.
                    // For this minimal example we just log it.
                    LOG.info("User sent: " + text);
                    // clear the field
                    inputField.setText("");
                }
            }
        };
        inputField.addActionListener(sendAction);
        sendButton.addActionListener(sendAction);
    }

    /** Open the component programmatically (optional convenience method) */
    public static void openComponent() {
        AiTopComponent tc = (AiTopComponent) WindowManager.getDefault()
                .findTopComponent("AiTopComponent"); // the preferredID
        if (tc != null) {
            tc.open();
            tc.requestActive();
        }
    }

    @Override
    public void componentOpened() {
        // Called when the component is opened; you can initialise resources here.
    }

    @Override
    public void componentClosed() {
        // Called when the component is closed; clean up if needed.
    }

    // -----------------------------------------------------------------
    // Optional: persist UI state (e.g., size/position) – NetBeans does this automatically.
    // -----------------------------------------------------------------
}