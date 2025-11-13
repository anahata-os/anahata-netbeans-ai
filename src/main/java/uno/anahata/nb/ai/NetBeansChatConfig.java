package uno.anahata.nb.ai;

import com.google.genai.types.Part;
import java.util.List;
import uno.anahata.gemini.content.ContextProvider;
import uno.anahata.gemini.ui.SwingChatConfig;
import uno.anahata.nb.ai.systeminstructions.CoreNetBeansInstructionsProvider;
import uno.anahata.nb.ai.workspace.IdeAlertsContextProvider;
import uno.anahata.nb.ai.workspace.ProjectOverviewContextProvider;
import uno.anahata.nb.ai.workspace.OpenTopComponentsContextProvider;
import uno.anahata.nb.ai.workspace.OutputTabsContextProvider;
import uno.anahata.nb.ai.tools.Coding;
import uno.anahata.nb.ai.tools.Editor;
import uno.anahata.nb.ai.tools.Git;
import uno.anahata.nb.ai.tools.IDE;
import uno.anahata.nb.ai.tools.JavaDocs;
import uno.anahata.nb.ai.tools.JavaIntrospection;
import uno.anahata.nb.ai.tools.JavaSources;
import uno.anahata.nb.ai.tools.Maven;
import uno.anahata.nb.ai.tools.NetBeansProjectJVM;
import uno.anahata.nb.ai.tools.Output;
import uno.anahata.nb.ai.tools.Projects;
import uno.anahata.nb.ai.tools.TopComponents;
import uno.anahata.nb.ai.tools.MavenPom;
import uno.anahata.nb.ai.tools.MavenSearch;

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
        ret.add(Maven.class);
        ret.add(MavenSearch.class);
        ret.add(MavenPom.class);
        ret.add(Output.class);
        ret.add(Projects.class);
        ret.add(Editor.class);
        ret.add(Coding.class);
        ret.add(TopComponents.class);
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
