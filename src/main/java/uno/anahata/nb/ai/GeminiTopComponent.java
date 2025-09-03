package uno.anahata.nb.ai;

import java.awt.Image;
import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.rmi.server.RMIClassLoader;
import java.security.CodeSource;
import uno.anahata.gemini.ui.GeminiPanel;
import java.util.logging.Logger;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.SwingConstants;

import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.modules.ModuleInfo;
import org.openide.modules.Modules;
import org.openide.windows.TopComponent;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.Utilities;
import uno.anahata.gemini.functions.spi.ExecuteJavaCode;

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
        log.info("init() -- exit");
    }

    private void initComponents() {

        
        gemini = new GeminiPanel(sysInsProvider, null);
        setLayout(new java.awt.BorderLayout());
        add(gemini, java.awt.BorderLayout.CENTER);

    }
    
    private String getClassPath() {
        
                return "GeminiTopComponent ClassLoader ClassPath :\n" + getClassLoaderClasspath(getClass().getClassLoader()) 
                + "\nCurrent thread Context ClassLoader ClassPath :\n" + getClassLoaderClasspath(Thread.currentThread().getContextClassLoader()) 
                + "\nModule ClassPath :\n" + getClassLoaderClasspath(Thread.currentThread().getContextClassLoader()) 
                + "\nExecuteJavaCode.class Cpde Source ClassPath :\n" + getCodeSourceClasspath(ExecuteJavaCode.class) 
                + "\nGeminiTopComponent.class Code Source ClassPath :\n" + getCodeSourceClasspath(GeminiTopComponent.class) 
                + "\nModule classloader classpath:\n" + getModuleClassPath()
                + "\nAnd These Are my system properties: \n" + System.getProperties().toString();
    }
    
    private String getModuleClassPath() {
        ModuleInfo mi = Modules.getDefault().ownerOf(getClass());
        mi.getClassLoader();
        return getClassLoaderClasspath(mi.getClassLoader());
    }

    private static String getCodeSourceClasspath(Class c) {
        StringBuilder classpath = new StringBuilder();
        try {
            CodeSource codeSource = c.getProtectionDomain().getCodeSource();
            if (codeSource != null) {
                
                URL location = codeSource.getLocation();
                try {
                    classpath.append(new File(location.toURI()).getAbsolutePath());
                } catch (Exception e) {
                    log.warning("code source location not valid: " + codeSource.getLocation());
                }
                
            }
        } catch (Exception e) {
            log.warning("Invalid code source URL: " + e.getMessage());
        }
        // Append system classpath for completeness
        //classpath.append(File.pathSeparator).append(System.getProperty("java.class.path"));
        return classpath.toString();
    }

    
    public static String getClassLoaderClasspath(ClassLoader classLoader) {
        StringBuilder classpath = new StringBuilder();
        
        while (classLoader != null) {
            if (classLoader instanceof URLClassLoader) {
                URLClassLoader urlClassLoader = (URLClassLoader) classLoader;
                for (URL url : urlClassLoader.getURLs()) {
                    try {
                        classpath.append(File.pathSeparator).append(new File(url.toURI()).getAbsolutePath());
                    } catch (URISyntaxException e) {
                        log.warning("Invalid URL: " + url);
                    }
                }
            }
            classLoader = classLoader.getParent();
        }
        // Append system classpath as fallback
        classpath.append(File.pathSeparator).append(System.getProperty("java.class.path"));
        return classpath.length() > 0 ? classpath.substring(File.pathSeparator.length()) : "";
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
        log.info("super.componentShowing(); ");
        super.componentShowing();
    }

    @Override
    protected void componentOpened() {
        log.info("super.componentOpened(); " + getClassPath());
        super.componentOpened();
    }

}
