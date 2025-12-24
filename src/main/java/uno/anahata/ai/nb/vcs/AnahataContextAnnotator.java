/* Licensed under the Apache License, Version 2.0 */
package uno.anahata.ai.nb.vcs;

import java.awt.Image;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Action;
import org.netbeans.modules.versioning.spi.VCSAnnotator;
import org.netbeans.modules.versioning.spi.VCSContext;
import org.netbeans.modules.versioning.spi.VersioningSupport;
import org.openide.util.ImageUtilities;
import org.openide.util.lookup.ServiceProvider;
//import uno.anahata.gemini.context.StatefulResource;
import uno.anahata.ai.nb.AnahataTopComponent;

/**
 * Annotates files present in the Anahata AI context with a badge and tooltip.
 */
//@ServiceProvider(service = VCSAnnotator.class, position = 2000)
public class AnahataContextAnnotator extends VCSAnnotator implements PropertyChangeListener {

    private static final Logger log = Logger.getLogger(AnahataContextAnnotator.class.getName());
    
    private static final String ANHATA_BADGE_PATH = "icons/anahata_16.png";
    private static final Image ANHATA_BADGE = ImageUtilities.loadImage(ANHATA_BADGE_PATH, true);

    public AnahataContextAnnotator() {
        log.info("AnahataContextAnnotator initialized.");
        // Listen for changes in the context so we can refresh the UI
        // AnahataTopComponent.getInstance().getContextManager().addPropertyChangeListener(this);
    }

    @Override
    public Image annotateIcon(Image icon, VCSContext context) {
        log.log(Level.INFO, "annotateIcon called for context with {0} files.", context.getRootFiles().size());
        
        for (File file : context.getRootFiles()) {
            log.log(Level.INFO, "Checking file: {0}", file.getName());

            // New logic: Annotate if the file name contains the letter 'a'
            if (file.getName().toLowerCase().contains("a")) {
                log.log(Level.INFO, "File name contains 'a', annotating: {0}", file.getName());
                
                String tooltip = "This file name contains the letter 'a'.";
                
                Image badgedIcon = ImageUtilities.mergeImages(icon, ANHATA_BADGE, 16, 9);
                return ImageUtilities.addToolTipToImage(badgedIcon, tooltip);
                
            } else {
                 log.log(Level.INFO, "File name does not contain 'a': {0}", file.getName());
            }
        }
        return null; // No change
    }

    @Override
    public String annotateName(String name, VCSContext context) {
        // We don't want to change the file name or color for now.
        return null; // No change
    }

    @Override
    public Action[] getActions(VCSContext context, ActionDestination destination) {
        // We don't need to add any custom actions to the versioning menus.
        return null; // No actions
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        // This logic is temporarily disabled.
        
//        log.log(Level.INFO, "Received context change event: {0}", evt.getPropertyName());
//        
//        if (evt.getNewValue() instanceof StatefulResource) {
//            StatefulResource resource = (StatefulResource) evt.getNewValue();
//            File file = new File(resource.getResourceId());
//            log.log(Level.INFO, "Firing file status changed for: {0}", file.getAbsolutePath());
//            // This is the magic call that tells NetBeans to refresh the node for this file
//            VersioningSupport.fireFileStatusChanged(file);
//        }
    }
}
