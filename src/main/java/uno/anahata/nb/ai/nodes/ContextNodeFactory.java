package uno.anahata.nb.ai.nodes;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.project.Project;
import org.netbeans.spi.project.ui.support.NodeFactory;
import org.netbeans.spi.project.ui.support.NodeFactorySupport;
import org.netbeans.spi.project.ui.support.NodeList;
import org.openide.nodes.Node;
import org.openide.util.lookup.ServiceProvider;

/**
 * This factory is registered globally to demonstrate the correct implementation
 * of the NodeFactory pattern. However, this pattern is for ADDING new nodes to
 * a project's logical view, not for filtering or decorating existing file
 * nodes. Therefore, its implementation is a "no-op" that returns an empty
 * list. The actual UI updates will be handled by decorators triggered by a
 * proper refresh mechanism.
 */
@ServiceProvider(service = NodeFactory.class, position = 1000)
public class ContextNodeFactory implements NodeFactory {
    
    private static final Logger log = Logger.getLogger(ContextNodeFactory.class.getName());

    public ContextNodeFactory() {
        log.info("ContextNodeFactory initialized.");
    }

    @Override
    public NodeList<?> createNodes(Project p) {
        log.log(Level.INFO, "createNodes called for project: {0}", p.getProjectDirectory().getName());
        // This approach is not suitable for filtering existing nodes.
        // Returning a shared, empty, no-op list is the most efficient way
        // to fulfill the interface contract.
        // The method expects a varargs Node[], so we pass an empty array.
        return NodeFactorySupport.fixedNodeList(new Node[0]);
    }
}
