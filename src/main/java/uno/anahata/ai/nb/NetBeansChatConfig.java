package uno.anahata.ai.nb;

import com.google.genai.types.Part;
import java.util.List;
import uno.anahata.gemini.content.ContextProvider;
import uno.anahata.gemini.ui.SwingChatConfig;
import uno.anahata.ai.nb.context.CoreNetBeansInstructionsProvider;
import uno.anahata.ai.nb.context.IdeAlertsContextProvider;
import uno.anahata.ai.nb.context.ProjectOverviewContextProvider;
import uno.anahata.ai.nb.context.OpenTopComponentsContextProvider;
import uno.anahata.ai.nb.context.OutputTabsContextProvider;
import uno.anahata.ai.nb.tools.Coding;
import uno.anahata.ai.nb.tools.Editor;
import uno.anahata.ai.nb.tools.Git;
import uno.anahata.ai.nb.tools.IDE;
import uno.anahata.ai.nb.tools.JavaDocs;
import uno.anahata.ai.nb.tools.JavaIntrospection;
import uno.anahata.ai.nb.tools.JavaSources;
import uno.anahata.ai.nb.tools.MavenTools;
import uno.anahata.ai.nb.tools.NetBeansProjectJVM;
import uno.anahata.ai.nb.tools.Output;
import uno.anahata.ai.nb.tools.Projects;
import uno.anahata.ai.nb.tools.TopComponents;
import uno.anahata.ai.nb.tools.Refactor;

public class NetBeansChatConfig extends SwingChatConfig {

    private final String sessionUuid;

    public NetBeansChatConfig(String sessionUuid) {
        this.sessionUuid = sessionUuid;
    }

    @Override
    public List<Class<?>> getToolClasses() {
        List<Class<?>> ret = super.getToolClasses();
        ret.add(NetBeansProjectJVM.class);
        ret.add(Git.class);
        ret.add(IDE.class);
        
        // Register the new consolidated Maven tool
        ret.add(MavenTools.class);
        
        // Deprecated Maven tools, to be removed.
        // ret.add(Maven.class);
        // ret.add(MavenSearch.class);
        // ret.add(MavenPom.class);
        
        ret.add(Output.class);
        ret.add(Projects.class);
        ret.add(Editor.class);
        ret.add(TopComponents.class);
        ret.add(Coding.class);
        ret.add(Refactor.class);
        ret.add(JavaIntrospection.class);
        ret.add(JavaSources.class);
        ret.add(JavaDocs.class);
        return ret;
    }

    @Override
    public String getSessionId() {
        return "netbeans-" + sessionUuid;
    }

    @Override
    public List<ContextProvider> getContextProviders() {
        List<ContextProvider> providers = super.getContextProviders();
        providers.add(new CoreNetBeansInstructionsProvider());
        providers.add(new IdeAlertsContextProvider());
        providers.add(new ProjectOverviewContextProvider());
        providers.add(new OpenTopComponentsContextProvider());
        providers.add(new OutputTabsContextProvider());
        return providers;
    }
}