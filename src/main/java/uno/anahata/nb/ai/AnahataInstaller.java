package uno.anahata.nb.ai;

import java.util.logging.Level;
import uno.anahata.nb.ai.deprecated.NetBeansListener;
import java.util.logging.Logger;
import javax.swing.JEditorPane;
import javax.swing.SwingUtilities;
import javax.swing.text.Document;
import javax.swing.text.EditorKit;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.TokenHierarchy;
import org.openide.modules.ModuleInstall;
import uno.anahata.gemini.functions.spi.RunningJVM;

public class AnahataInstaller extends ModuleInstall {
    
    private static NetBeansListener listener;
    private static final Logger log = Logger.getLogger(AnahataInstaller.class.getName());
    
    public AnahataInstaller() {
        logId("init() jva.class.path=" + System.getProperty("java.class.path"));
        logId("init() nb.dynamic.classpath=" + System.getProperty("netbeans.dynamic.classpath"));
        logId("init() RunningJVM.defaultCompilerClasspath=" + RunningJVM.getDefaultCompilerClasspath());
    }
    
    @Override
    public void validate() throws IllegalStateException {
        
        super.validate(); // Generated from 
        
    }
    
    @Override
    public void restored() {
        logId("restored() begins :");
        ShowDefaultCompilerClassPathAction.initRunningJVM();
        
        warmupEditor();
        logId("restored() finished");
    }
    
    private void warmupEditor() {
        // --- WARM-UP CODE ---
        // Must be run on the EDT
        SwingUtilities.invokeLater(() -> {
            try {
                log.info("Warming up Java highlighting pipeline...");
                JEditorPane warmupPane = new JEditorPane();
                EditorKit kit = MimeLookup.getLookup("text/x-java").lookup(EditorKit.class);
                if (kit != null) {
                    warmupPane.setEditorKit(kit);
                    Document doc = warmupPane.getDocument();
                    doc.putProperty(Language.class, Language.find("text/x-java"));
                    doc.putProperty("mimeType", "text/x-java");
                    TokenHierarchy.get(doc); // This is the key step to force initialization
                    warmupPane.setText("class Dummy {}"); // Setting text ensures all components are touched
                    log.info("Java highlighting pipeline successfully warmed up.");
                } else {
                    log.warning("Failed to find Java EditorKit during warm-up.");
                }
            } catch (Exception e) {
                log.log(Level.WARNING, "Exception during Java highlighting warm-up", e);
            }
        });
        // --- END WARM-UP CODE ---
        
    }
    
    @Override
    public void uninstalled() {
        if (listener != null) {
            logId("uninstalled() calling listener.cleanup()");
            listener.cleanup();
            logId("uninstalled() finished listener.cleanup()");
        } else {
            logId("uninstalled() no listener was created");
        }
        
        logId("uninstalled()");
        super.uninstalled();
    }
    
    @Override
    protected boolean clearSharedData() {
        boolean ret = super.clearSharedData();
        logId("clearSharedData(): " + ret);
        return ret;
    }
    
    @Override
    public boolean closing() {
        boolean ret = super.closing();
        logId("closing(): " + ret);
        return ret;
    }
    
    @Override
    public void close() {
        logId("close()");
        super.close();
    }
    
    private void logId(String mssg) {
        log.info(System.identityHashCode(this) + ":" + mssg);
    }
    
}
