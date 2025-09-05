package uno.anahata.nb.ai;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JTextArea;
import org.netbeans.api.java.classpath.ClassPath;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.modules.Dependency;
import org.openide.modules.ModuleInfo;
import org.openide.modules.Modules;
import org.openide.util.Lookup;
import org.openide.windows.TopComponent;
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

    private GeminiPanel gemini;
    private GeminiConfigProviderImpl sysInsProvider = new GeminiConfigProviderImpl();

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
        
        /*
        JTextArea centerTextArea = new JTextArea();
        JButton button = new JButton("Show classpath");
        button.setAction(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                    centerTextArea.setText(ClassPathUtils.getClassPathOfEverythingOpenInIDE());
            }
        });
        add(button, java.awt.BorderLayout.NORTH);
        add(centerTextArea, java.awt.BorderLayout.CENTER);
*/
        
        gemini = new GeminiPanel(sysInsProvider, null);        
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
        //centerTextArea.setText(ClassPathUtils.getClassPathOfEverythingOpenInIDE());
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
    protected void componentOpened() {
        log.info("super.componentOpened();");
        super.componentOpened();
    }

    
    
    
}
