package uno.anahata.nb.ai.nodes;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ImageIcon;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;
import org.openide.util.ImageUtilities;

public class ContextFilterNode extends FilterNode {
    
    private static final Logger log = Logger.getLogger(ContextFilterNode.class.getName());

    private static final Image ANAHATA_ICON;
    private static final String ICON_PATH = "icons/anahata_16.png"; // Standardized path
    private static final URL ICON_URL = ContextFilterNode.class.getResource("/" + ICON_PATH);

    static {
        log.info("ContextFilterNode static block initializing...");
        if (ICON_URL != null) {
            ANAHATA_ICON = new ImageIcon(ICON_URL).getImage();
            log.info("Successfully loaded icon for ContextFilterNode from /icons/anahata_16.png");
        } else {
            ANAHATA_ICON = ImageUtilities.loadImage("org/openide/nodes/defaultNode.gif");
            log.warning("Could not load icon for ContextFilterNode, fallback used.");
        }
    }

    public ContextFilterNode(Node original) {
        super(original);
        log.log(Level.INFO, "ContextFilterNode created for: {0}", original.getName());
    }

    @Override
    public String getHtmlDisplayName() {
        log.log(Level.INFO, "getHtmlDisplayName called for node: {0}", getOriginal().getName());
        String originalName = super.getDisplayName();
        if (ICON_URL != null) {
            return "<html><img src=\"nbresloc:/" + ICON_PATH + "\">&nbsp;" + originalName;
        }
        return "<html>" + originalName + " <font color='!controlShadow'>[c]</font>";
    }

    @Override
    public Image getIcon(int type) {
        log.log(Level.INFO, "getIcon called for node: {0} with type: {1}", new Object[]{getOriginal().getName(), type});
        Image originalIcon = super.getIcon(type);
        
        if (ANAHATA_ICON == null) {
            return originalIcon;
        }

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

    @Override
    public String getShortDescription() {
        log.log(Level.INFO, "getShortDescription called for node: {0}", getOriginal().getName());
        return super.getShortDescription() + " (In AI Context)";
    }
}
