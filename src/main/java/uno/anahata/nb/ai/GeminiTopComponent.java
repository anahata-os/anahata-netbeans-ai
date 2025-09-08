package uno.anahata.nb.ai;

import java.awt.BorderLayout;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JTextArea;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.windows.TopComponent;
import uno.anahata.gemini.functions.spi.ExecuteJavaCode;
import uno.anahata.gemini.ui.GeminiPanel;

@ActionID(category = "Window", id = "uno.anahata.nb.ai.OpenGeminiAction")
@ActionReference(path = "Menu/Window", position = 333)
@TopComponent.Description(
        preferredID = "gemini",
        iconBase = "uno/anahata/nb/ai/gemini.png",
        persistenceType = TopComponent.PERSISTENCE_NEVER)
@TopComponent.Registration(mode = "output", openAtStartup = true)
@TopComponent.OpenActionRegistration(displayName = "Gemini Assistant", preferredID = "gemini")
public final class GeminiTopComponent extends TopComponent {

    private static final Logger log = Logger.getLogger(GeminiTopComponent.class.getName());
    private static boolean initialized = false;

    private GeminiPanel gemini;
    private final GeminiConfigProviderImpl sysInsProvider = new GeminiConfigProviderImpl();

    public GeminiTopComponent() {
        log.info("init() -- entry ");
        setName("Gemini");
        setToolTipText("Get Gemini to do your work");
        initComponents();
        log.info("init() -- exit ");
    }
    
    JTextArea centerTextArea = new JTextArea();

    private void initComponents() {
        setLayout(new java.awt.BorderLayout());
        
        String initMessage = "Hello! Your short-term memory has been pre-populated by the plugin's startup routine. " +
                             "Please check your chatTemp map for the 'openProjectsList' key and greet the user with a summary of the open projects.";
        gemini = new GeminiPanel(sysInsProvider, initMessage);        
        add(gemini, java.awt.BorderLayout.CENTER);
    }

    @Override
    public void componentClosed() {
        log.info("super.componentClosed(); ");
        super.componentClosed();
    }

    @Override
    protected void componentDeactivated() {
        log.info("super.componentDeactivated(); ");
        super.componentDeactivated();
    }

    @Override
    protected void componentActivated() {
        log.info("super.componentActivated(); ");
        super.componentActivated();
    }

    @Override
    protected void componentHidden() {
        log.info("super.componentHidden(); ");
        super.componentHidden();
    }

    @Override
    protected void componentShowing() {
        log.info("super.componentShowing();");
        super.componentShowing();
    }

    @Override
    public void componentOpened() {
        log.info("super.componentOpened();");
        super.componentOpened();
        
        if (!initialized) {
            initialized = true;
            Thread initThread = new Thread(() -> {
                log.info("Starting background initialization of Gemini short-term memory in TopComponent...");
                try {
                    // Self-test to ensure the execution environment is ready.
                    Object selfTestResult = ExecuteJavaCode.compileAndExecuteJava("public class Gemini implements java.util.concurrent.Callable<String> { public String call() { return \"OK\"; } }", null, null);
                    if (!"OK".equals(selfTestResult)) {
                        throw new IllegalStateException("Gemini self-test failed, execution environment not ready.");
                    }
                    log.info("Gemini self-test successful.");

                    // Now proceed with the original logic
                    Path snippetPath = Paths.get(System.getProperty("user.home"), ".netbeans", "gemini_snippets", "getOpenProjects.java");
                    if (Files.exists(snippetPath)) {
                        String snippetCode = new String(Files.readAllBytes(snippetPath));
                        Object result = ExecuteJavaCode.compileAndExecuteJava(snippetCode, null, null);
                        ExecuteJavaCode.chatTemp.put("openProjectsList", result);
                        log.info("Successfully pre-cached open projects list.");
                    } else {
                        log.warning("Golden snippet for open projects not found. Skipping pre-caching.");
                    }
                } catch (Exception e) {
                    log.log(Level.SEVERE, "Error during background initialization of Gemini", e);
                    ExecuteJavaCode.chatTemp.put("initializationError", e.getMessage());
                }
            });
            initThread.setName("Gemini-TC-Init-Thread");
            initThread.setDaemon(true);
            initThread.start();
        }
    }
}
