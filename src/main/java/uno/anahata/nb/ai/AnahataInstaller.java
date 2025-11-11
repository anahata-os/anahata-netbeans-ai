package uno.anahata.nb.ai;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.modules.ModuleInstall;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;
import uno.anahata.gemini.AnahataConfig;
import uno.anahata.nb.ai.util.NetBeansModuleUtils;

public class AnahataInstaller extends ModuleInstall {

    private static final Logger log = Logger.getLogger(AnahataInstaller.class.getName());
    private static final String HANDOFF_FILE_NAME = "reload-handoff.dat";

    public AnahataInstaller() {
        logId("AnahataInstaller()");
    }

    @Override
    public void restored() {
        logId("restored() begins");
        NetBeansModuleUtils.initRunningJVM();
        
        File handoffFile = getHandoffFile();
        if (handoffFile.exists()) {
            log.info("Handoff file found. Proceeding with session restoration.");
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(handoffFile))) {
                List<String> sessionUuids = (List<String>) ois.readObject();
                log.info("Restoring " + sessionUuids.size() + " sessions from handoff file.");
                for (String uuid : sessionUuids) {
                    AnahataTopComponent tc = new AnahataTopComponent();
                    tc.setSessionUuidForHandoff(uuid);
                    tc.open();
                    tc.requestActive();
                    log.info("Restored and opened TopComponent for session: " + uuid);
                }
            } catch (IOException | ClassNotFoundException e) {
                log.log(Level.SEVERE, "Failed to read handoff file", e);
            } finally {
                if (handoffFile.delete()) {
                    log.info("Handoff file deleted successfully.");
                } else {
                    log.warning("Failed to delete handoff file.");
                }
            }
        } else {
            log.info("No handoff file found. Normal startup.");
        }
        
        logId("restored() finished");
    }

    @Override
    public void uninstalled() {
        logId("uninstalled() begins");
        
        List<String> sessionUuids = new ArrayList<>();
        Set<TopComponent> openTcs = WindowManager.getDefault().getRegistry().getOpened();
        
        for (TopComponent tc : openTcs) {
            if (tc instanceof AnahataTopComponent) {
                AnahataTopComponent atc = (AnahataTopComponent) tc;
                String uuid = atc.getSessionUuid();
                if (uuid != null) {
                    sessionUuids.add(uuid);
                    // It's good practice to ensure the latest state is saved.
                    // The autobackup is tied to the GeminiChat instance, so we might need a more direct way to save.
                    // For now, we rely on the existing autobackup mechanism.
                    log.info("Found open Anahata session to handoff: " + uuid);
                }
            }
        }
        
        if (!sessionUuids.isEmpty()) {
            File handoffFile = getHandoffFile();
            try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(handoffFile))) {
                oos.writeObject(sessionUuids);
                log.info("Successfully wrote " + sessionUuids.size() + " session UUIDs to handoff file.");
            } catch (IOException e) {
                log.log(Level.SEVERE, "Failed to write handoff file", e);
            }
        }
        
        // Now close the old components
        for (TopComponent tc : openTcs) {
            if (tc instanceof AnahataTopComponent) {
                tc.close();
                log.info("Closed old AnahataTopComponent: " + ((AnahataTopComponent) tc).getSessionUuid());
            }
        }
        
        super.uninstalled();
        logId("uninstalled() finished");
    }

    private File getHandoffFile() {
        return new File(AnahataConfig.getWorkingFolder(), HANDOFF_FILE_NAME);
    }

    private void logId(String mssg) {
        log.info(Thread.currentThread().getName()
                + " instance=" + System.identityHashCode(this)
                + " class=" + System.identityHashCode(AnahataInstaller.class)
                + ": " + mssg);
    }
}
