package uno.anahata.nb.ai;

import org.openide.modules.ModuleInstall;

public class Installer extends ModuleInstall {

    @Override
    public void restored() {
        // Module has been installed and restored
        System.out.println("LLM Chat Plugin loaded successfully!");
    }

    @Override
    public void uninstalled() {
        // Module is being uninstalled
        System.out.println("LLM Chat Plugin uninstalled.");
    }
}
