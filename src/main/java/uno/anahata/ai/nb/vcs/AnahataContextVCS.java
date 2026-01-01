/* Licensed under the Apache License, Version 2.0 */
package uno.anahata.ai.nb.vcs;

import java.io.File;
import java.util.logging.Logger;
import org.netbeans.modules.versioning.spi.VCSAnnotator;
import org.netbeans.modules.versioning.spi.VCSHistoryProvider;
import org.netbeans.modules.versioning.spi.VCSInterceptor;
import org.netbeans.modules.versioning.spi.VCSVisibilityQuery;
import org.netbeans.modules.versioning.spi.VersioningSystem;
import org.netbeans.modules.versioning.spi.VersioningSystem.Registration;
import org.openide.util.lookup.ServiceProvider;

/**
 * Registers our custom VersioningSystem, which is required to activate the
 * VCSAnnotator pipeline for projects that are managed by the Anahata AI Assistant.
 */
/*
@ServiceProvider(service = VersioningSystem.class)
@Registration(
    displayName = "Anahata AI Context",
    menuLabel = "Anahata AI",
    metadataFolderNames = {"anahata.md"}, // We'll use the presence of anahata.md to identify managed projects.
    actionsCategory = "Team"
)*/
public class AnahataContextVCS extends VersioningSystem {

    private static final Logger LOG = Logger.getLogger(AnahataContextVCS.class.getName());
    private final VCSAnnotator annotator = new AnahataContextAnnotator();

    /**
     * Default constructor for the VCS system.
     */
    public AnahataContextVCS() {
    }

    @Override
    public VCSAnnotator getVCSAnnotator() {
        return annotator;
    }

    @Override
    public File getTopmostManagedAncestor(File file) {
        File current = file;
        while (current != null) {
            if (new File(current, "anahata.md").exists()) {
                return current;
            }
            current = current.getParentFile();
        }
        return null;
    }

    //<editor-fold defaultstate="collapsed" desc="Unused SPI Methods">
    @Override
    public VCSInterceptor getVCSInterceptor() {
        return null;
    }

    @Override
    public VCSHistoryProvider getVCSHistoryProvider() {
        return null;
    }

    @Override
    public void getOriginalFile(File workingCopy, File originalFile) {
        // Not applicable
    }

    @Override
    public VCSVisibilityQuery getVisibilityQuery() {
        return null;
    }
    //</editor-fold>
}