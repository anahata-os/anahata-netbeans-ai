package uno.anahata.nb.ai.nodes;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ImageIcon;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.ImageDecorator;
import org.openide.util.lookup.ServiceProvider;
import static uno.anahata.nb.ai.context.ContextFiles.ATTR_IN_AI_CONTEXT;

@ServiceProvider(service = ImageDecorator.class, position = 900)
public class ContextFileImageDecorator implements ImageDecorator {

    private static final Logger log = Logger.getLogger(ContextFileImageDecorator.class.getName());
    private static final Image ANAHATA_ICON;

    static {
        // Standardized path, loaded from the classpath root.
        URL iconUrl = ContextFileImageDecorator.class.getResource("/icons/anahata_16.png");
        if (iconUrl != null) {
            ANAHATA_ICON = new ImageIcon(iconUrl).getImage();
            log.info("Successfully loaded Anahata icon for ImageDecorator.");
        } else {
            ANAHATA_ICON = null;
            log.warning("Failed to load Anahata icon for ImageDecorator!");
        }
    }

    @Override
    public Image annotateIcon(Image originalIcon, int type, Set<? extends FileObject> files) {
        if (files == null || files.size() != 1) {
            return originalIcon;
        }
        FileObject fileObject = files.iterator().next();
        log.log(Level.INFO, "annotateIcon called for file: {0}, type: {1}", new Object[]{fileObject.getNameExt(), type});

        if (ANAHATA_ICON == null) {
            return originalIcon;
        }

        if (Boolean.TRUE.equals(fileObject.getAttribute(ATTR_IN_AI_CONTEXT))) {
            log.log(Level.INFO, "File is in context (via attribute), decorating icon for: {0}", fileObject.getName());

            int width = originalIcon.getWidth(null);
            int height = originalIcon.getHeight(null);

            if (width <= 0 || height <= 0) {
                return originalIcon;
            }

            BufferedImage mergedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = mergedImage.createGraphics();

            g.drawImage(originalIcon, 0, 0, null);

            int badgeWidth = ANAHATA_ICON.getWidth(null);
            int badgeHeight = ANAHATA_ICON.getHeight(null);
            int x = width - badgeWidth;
            int y = height - badgeHeight;

            g.drawImage(ANAHATA_ICON, x, y, null);
            g.dispose();

            return mergedImage;
        }

        return originalIcon;
    }
}
