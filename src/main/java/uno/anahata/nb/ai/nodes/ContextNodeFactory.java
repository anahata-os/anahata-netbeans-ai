package uno.anahata.nb.ai.nodes;

import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.Project;
import org.netbeans.spi.project.ui.support.NodeFactory;
import org.netbeans.spi.project.ui.support.NodeFactorySupport;
import org.netbeans.spi.project.ui.support.NodeList;
import org.openide.filesystems.FileObject;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ServiceProvider;

// Registering for a common base type to appear in more projects.
// The position ensures it runs after the default factories.
@NodeFactory.Registration(projectType = {"org-netbeans-modules-maven", "org-netbeans-modules-java-j2seproject"}, position = 500,parentPath = "Projects/Anahata")
//@ServiceProvider(service = Node)
public class ContextNodeFactory implements NodeFactory {
    private static final Logger log = Logger.getLogger(ContextNodeFactory.class.getName());

    public ContextNodeFactory() {
        log.info("ENTRY ContextNodeFactory()");
        log.info("EXIT ContextNodeFactory()");
    }
    
    @Override
    public NodeList<?> createNodes(Project project) {        
        log.log(Level.INFO, "ENTRY createNodes(project={0})", project.getProjectDirectory().getName());
        
        // This is the new, safe implementation.
        // We check for our specific file and, if it exists, we create our custom node.
        // We DO NOT call the LogicalViewProvider, thus avoiding the StackOverflowError.
        FileObject projectDir = project.getProjectDirectory();
        FileObject anahataMd = projectDir.getFileObject("anahata.md");
        
        if (anahataMd != null) {
            log.log(Level.INFO, "Found anahata.md in project {0}, creating Anahata folder node.", project.getProjectDirectory().getName());
            AnahataFolderNode anahataNode = new AnahataFolderNode(project);
            return NodeFactorySupport.fixedNodeList(anahataNode);
        }
        
        // If our file doesn't exist, we return an empty list and do nothing.
        log.log(Level.INFO, "No anahata.md in project {0}, returning empty list.", project.getProjectDirectory().getName());
        return NodeFactorySupport.fixedNodeList();
    }

    /**
     * A custom node representing the "Anahata" folder.
     */
    private static class AnahataFolderNode extends AbstractNode {
        private final Project project;

        public AnahataFolderNode(Project project) {
            // We pass a Children object that will create the child node (anahata.md)
            super(new AnahataFileChildren(project));
            this.project = project;
            setName("Anahata"); // Internal name
            setDisplayName("Anahata"); // Display name in the UI
            //setIconBaseWithExtension("icons/anahata.png");
        }
    }

    /**
     * The Children implementation for our Anahata folder. Its only job is
     * to create the node for the anahata.md file.
     */
    private static class AnahataFileChildren extends Children.Keys<String> {
        private final Project project;
        private static final String KEY = "anahata.md";

        public AnahataFileChildren(Project project) {
            this.project = project;
        }

        @Override
        protected void addNotify() {
            // This is called when the node is expanded.
            // We set our single key, which will trigger createNodes.
            setKeys(Collections.singleton(KEY));
        }

        @Override
        protected Node[] createNodes(String key) {
            if (KEY.equals(key)) {
                FileObject anahataMd = project.getProjectDirectory().getFileObject(key);
                if (anahataMd != null) {
                    try {
                        // The standard NetBeans way to get a node for a file
                        org.openide.loaders.DataObject dob = org.openide.loaders.DataObject.find(anahataMd);
                        return new Node[]{dob.getNodeDelegate()};
                    } catch (org.openide.loaders.DataObjectNotFoundException e) {
                        log.log(Level.WARNING, "Could not find DataObject for anahata.md", e);
                    }
                }
            }
            return new Node[0]; // Return empty array if something goes wrong
        }
    }
}
