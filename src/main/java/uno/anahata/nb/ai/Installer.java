package uno.anahata.nb.ai;

import uno.anahata.nb.ai.deprecated.NetBeansListener;
import java.util.logging.Logger;
import org.openide.modules.ModuleInstall;
import uno.anahata.gemini.functions.spi.RunningJVM;

public class Installer extends ModuleInstall {
    
    private static NetBeansListener listener;
    private static final Logger log = Logger.getLogger(Installer.class.getName());
    
    public Installer() {
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
        logId("restored() finished");
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
