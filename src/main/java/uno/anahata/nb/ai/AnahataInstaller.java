package uno.anahata.nb.ai;

import java.util.logging.Logger;
import lombok.extern.slf4j.Slf4j;
import org.openide.modules.ModuleInstall;
import uno.anahata.gemini.functions.spi.RunningJVM;
//@Slf4j
public class AnahataInstaller extends ModuleInstall {
    
    
    private static final Logger log = Logger.getLogger(AnahataInstaller.class.getName());
    
    public AnahataInstaller() {
        logId("AnahataInstaller()");
    }
    
    @Override
    public void validate() throws IllegalStateException {
        logId("validate()");
        super.validate(); // Generated from 
        
    }
    
    @Override
    public void restored() {
        logId("restored() begins Preparing classpath setup:");
        
        NetBeansModuleUtils.initRunningJVM();
        
        logId("restored() finished");
    }
    
    @Override
    public void uninstalled() {
        logId("uninstalled()");
        super.uninstalled();
        logId("uninstalled()");
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
        logId("closing(): super.closing()" + ret);
        return ret;
    }
    
    @Override
    public void close() {
        logId("close()");
        super.close();
    }
    
    private void logId(String mssg) {
        log.info(Thread.currentThread().getName() + " hashCode" + System.identityHashCode(this) + ": " + mssg);
    }
    
}
