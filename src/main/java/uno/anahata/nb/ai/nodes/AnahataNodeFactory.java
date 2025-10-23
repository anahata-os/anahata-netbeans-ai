package uno.anahata.nb.ai.nodes;

import java.awt.Image;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ImageIcon;
import org.netbeans.api.project.Project;
import org.netbeans.spi.project.ui.support.NodeFactory;
import org.netbeans.spi.project.ui.support.NodeFactorySupport;
import org.netbeans.spi.project.ui.support.NodeList;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;
import org.openide.util.ImageUtilities;
import org.openide.util.lookup.Lookups;

@NodeFactory.Registration(projectType = {"org-netbeans-modules-maven", "org-netbeans-modules-java-j2seproject"}, position = 500, parentPath = "Projects/Anahata")
public class AnahataNodeFactory implements NodeFactory {

    private static final Logger log = Logger.getLogger(AnahataNodeFactory.class.getName());

    public AnahataNodeFactory() {
        log.info("ENTRY AnahataNodeFactory()");
        log.info("EXIT AnahataNodeFactory()");
    }

    @Override
    public NodeList<?> createNodes(Project project) {
        log.log(Level.INFO, "ENTRY createNodes(project={0})", project.getProjectDirectory().getName());
        AnahataFolderNode anahataNode = new AnahataFolderNode(project);
        NodeList<?> result = NodeFactorySupport.fixedNodeList(anahataNode);
        log.log(Level.INFO, "EXIT createNodes() -> {0}", result);
        return result;
    }

    private static class AnahataFolderNode extends AbstractNode {

        private static final String FOLDER_ICON_PATH = "org/openide/loaders/defaultFolder.gif";
        private static final String FOLDER_OPEN_ICON_PATH = "org/openide/loaders/defaultFolderOpen.gif";
        private static final String OVERLAY_ICON_PATH = "icons/anahata.png";

        public AnahataFolderNode(Project project) {
            super(new AnahataFileChildren(project), Lookups.singleton(project));
            log.log(Level.INFO, "ENTRY AnahataFolderNode(project={0})", project.getProjectDirectory().getName());
            setName("Anahata");
            setDisplayName("Anahata");
            log.info("EXIT AnahataFolderNode()");
        }

        @Override
        public Image getIcon(int type) {
            log.log(Level.INFO, "ENTRY getIcon(type={0})", type);
            Image folderIcon = ImageUtilities.loadImage(FOLDER_ICON_PATH);
            Image overlayIcon = ImageUtilities.loadImage(OVERLAY_ICON_PATH);
            // Scale the overlay icon to 12x12
            Image scaledOverlay = overlayIcon.getScaledInstance(12, 12, Image.SCALE_SMOOTH);
            // Use an ImageIcon to ensure the scaled image is fully loaded before merging
            Image finalOverlay = new ImageIcon(scaledOverlay).getImage();
            // Adjust position for the new size (16-12=4) to keep it in the bottom-right
            Image mergedIcon = ImageUtilities.mergeImages(folderIcon, finalOverlay, 4, 4);
            log.log(Level.INFO, "EXIT getIcon() -> {0}", mergedIcon);
            return mergedIcon;
        }

        @Override
        public Image getOpenedIcon(int type) {
            log.log(Level.INFO, "ENTRY getOpenedIcon(type={0})", type);
            Image folderIcon = ImageUtilities.loadImage(FOLDER_OPEN_ICON_PATH);
            Image overlayIcon = ImageUtilities.loadImage(OVERLAY_ICON_PATH);
            // Scale the overlay icon to 12x12
            Image scaledOverlay = overlayIcon.getScaledInstance(12, 12, Image.SCALE_SMOOTH);
            // Use an ImageIcon to ensure the scaled image is fully loaded before merging
            Image finalOverlay = new ImageIcon(scaledOverlay).getImage();
            // Adjust position for the new size (16-12=4) to keep it in the bottom-right
            Image mergedIcon = ImageUtilities.mergeImages(folderIcon, finalOverlay, 4, 4);
            log.log(Level.INFO, "EXIT getOpenedIcon() -> {0}", mergedIcon);
            return mergedIcon;
        }
    }

    private static class AnahataFileChildren extends Children.Keys<FileObject> {

        private final Project project;

        public AnahataFileChildren(Project project) {
            super();
            log.log(Level.INFO, "ENTRY AnahataFileChildren(project={0})", project.getProjectDirectory().getName());
            this.project = project;
            log.info("EXIT AnahataFileChildren()");
        }

        @Override
        protected void addNotify() {
            log.info("ENTRY addNotify()");
            FileObject projectDir = project.getProjectDirectory();
            List<FileObject> mdFiles = new ArrayList<>();
            boolean anahataMdExists = false;
            for (FileObject child : projectDir.getChildren()) {
                if (child.isData() && "md".equalsIgnoreCase(child.getExt())) {
                    mdFiles.add(child);
                    if ("anahata.md".equalsIgnoreCase(child.getNameExt())) {
                        anahataMdExists = true;
                    }
                }
            }

            if (!anahataMdExists) {
                try {
                    log.info("anahata.md not found, creating it.");
                    FileObject newFile = projectDir.createData("anahata.md");
                    try (Writer writer = new OutputStreamWriter(newFile.getOutputStream())) {
                        writer.write("# Anahata Project Notes\n\nThis file is for Anahata AI Assistant's notes regarding the '" 
                                     + project.getProjectDirectory().getName() + "' project.\n");
                    }
                    mdFiles.add(newFile);
                } catch (IOException e) {
                    log.log(Level.SEVERE, "Failed to create anahata.md", e);
                }
            }

            setKeys(mdFiles);
            log.log(Level.INFO, "EXIT addNotify() - Set keys: {0}", mdFiles);
        }

        @Override
        protected Node[] createNodes(FileObject key) {
            log.log(Level.INFO, "ENTRY createNodes(key={0})", key.getPath());
            try {
                DataObject dob = DataObject.find(key);
                Node originalNode = dob.getNodeDelegate();

                if ("anahata.md".equals(key.getNameExt())) {
                    Node[] result = new Node[]{new AnahataMdNode(originalNode)};
                    log.log(Level.INFO, "EXIT createNodes() -> [AnahataMdNode for {0}]", key.getNameExt());
                    return result;
                } else {
                    Node[] result = new Node[]{originalNode};
                    log.log(Level.INFO, "EXIT createNodes() -> [Default Node for {0}]", key.getNameExt());
                    return result;
                }
            } catch (DataObjectNotFoundException e) {
                log.log(Level.WARNING, "Could not find DataObject for " + key.getPath(), e);
            }
            Node[] emptyResult = new Node[0];
            log.log(Level.INFO, "EXIT createNodes() -> Empty Array");
            return emptyResult;
        }
    }

    private static class AnahataMdNode extends FilterNode {

        private static final String ANAHATA_ICON_PATH = "icons/anahata.png";

        public AnahataMdNode(Node original) {
            super(original);
            log.log(Level.INFO, "ENTRY AnahataMdNode(original={0})", original);
            log.info("EXIT AnahataMdNode()");
        }

        @Override
        public Image getIcon(int type) {
            log.log(Level.INFO, "ENTRY getIcon(type={0})", type);
            Image icon = ImageUtilities.loadImage(ANAHATA_ICON_PATH);
            log.log(Level.INFO, "EXIT getIcon() -> {0}", icon);
            return icon;
        }

        @Override
        public Image getOpenedIcon(int type) {
            log.log(Level.INFO, "ENTRY getOpenedIcon(type={0})", type);
            Image icon = getIcon(type);
            log.log(Level.INFO, "EXIT getOpenedIcon() -> {0}", icon);
            return icon;
        }
    }
}
