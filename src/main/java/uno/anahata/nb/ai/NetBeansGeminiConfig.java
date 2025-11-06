package uno.anahata.nb.ai;

import java.util.List;
import uno.anahata.gemini.systeminstructions.SystemInstructionProvider;
import uno.anahata.gemini.ui.SwingGeminiConfig;
import uno.anahata.nb.ai.util.NetBeansJavaQueryUtils;
import uno.anahata.nb.ai.tools.Coding;
import uno.anahata.nb.ai.tools.Editor;
import uno.anahata.nb.ai.tools.Git;
import uno.anahata.nb.ai.tools.IDE;
import uno.anahata.nb.ai.tools.JavaIntrospection;
import uno.anahata.nb.ai.tools.JavaSources;
import uno.anahata.nb.ai.tools.Maven;
import uno.anahata.nb.ai.tools.NetBeansJVM;
import uno.anahata.nb.ai.tools.Projects;
import uno.anahata.nb.ai.tools.Output;
import uno.anahata.nb.ai.tools.TopComponents;
import uno.anahata.nb.ai.tools.Workspace;
import uno.anahata.nb.ai.systeminstructions.CoreNetBeansInstructionsProvider;
import uno.anahata.nb.ai.systeminstructions.IdeAlertsInstructionsProvider;
import uno.anahata.nb.ai.systeminstructions.OpenProjectsOverviewInstructionsProvider;
import uno.anahata.nb.ai.systeminstructions.OpenTopComponentsInstructionsProvider;

public class NetBeansGeminiConfig extends SwingGeminiConfig {

    String topComponentId;
    
    public NetBeansGeminiConfig(String topComponentId) {
        this.topComponentId = topComponentId;
    }
    
    @Override
    public List<Class<?>> getAutomaticFunctionClasses() {
        List<Class<?>> ret = super.getAutomaticFunctionClasses();
        ret.add(NetBeansJVM.class);
        ret.add(Git.class);
        ret.add(IDE.class);
        ret.add(Maven.class);
        ret.add(Workspace.class);
        ret.add(Output.class);
        ret.add(Projects.class);
        ret.add(Editor.class);
        ret.add(Coding.class);
        ret.add(TopComponents.class);
        ret.add(JavaIntrospection.class);
        ret.add(JavaSources.class);
        // NetBeansJavaQueryUtils is being deprecated in favor of JavaIntrospection and a future Javadoc/Sources tool
        // ret.add(NetBeansJavaQueryUtils.class); 
        return ret;
    }

    @Override
    public String getApplicationInstanceId() {
        return "netbeans-" + topComponentId;
    }

    @Override
    public List<SystemInstructionProvider> getApplicationSpecificInstructionProviders() {
        List<SystemInstructionProvider> providers = super.getApplicationSpecificInstructionProviders();
        providers.add(new CoreNetBeansInstructionsProvider());
        providers.add(new IdeAlertsInstructionsProvider());
        providers.add(new OpenProjectsOverviewInstructionsProvider());
        providers.add(new OpenTopComponentsInstructionsProvider());
        return providers;
    }
}
