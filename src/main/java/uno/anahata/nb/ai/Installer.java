/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/NetBeansModuleDevelopment-files/moduleInstall.java to edit this template
 */
package uno.anahata.nb.ai;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.modules.ModuleInstall;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;
import uno.anahata.gemini.functions.spi.ExecuteJavaCode;

public class Installer extends ModuleInstall {

    private static NetBeansListener listener;
    private static final Logger log = Logger.getLogger(Installer.class.getName());

    public Installer() {
        logId("init()");
    }
    
    

    @Override
    public void validate() throws IllegalStateException {
        logId("validate()");
        logId("Compiler classpath" + ExecuteJavaCode.getCompilerClasspath());
        super.validate(); // Generated from 
        
        // Called when the module is reloaded
        TopComponent tc = WindowManager.getDefault().findTopComponent("gemini");
        
        if (tc != null && tc.isOpened()) {
            logId("validate() will close the old TopComponent()");
            tc.close();  // Close existing instance
            
        }
    }

    @Override
    public void restored() {
        try {
            if (listener != null) {
                logId("restored() deleting old listener:" + listener);            
                listener = null;
            }
            logId("restored() creating new listener:" + listener);            
            //listener = new NetBeansListener();
        } catch (Exception e) {
            log.log(Level.SEVERE, "restored()", e);
        }
        
        logId("restored() finished");
    }
    
    @Override
    public void uninstalled() {
        if (listener != null) {
            logId("uninstalled() calling listener.cleanup()");
            listener.cleanup();
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
