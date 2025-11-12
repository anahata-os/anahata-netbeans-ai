package uno.anahata.nb.ai;

import java.util.List;
import uno.anahata.gemini.config.systeminstructions.SystemInstructionProvider;
import uno.anahata.gemini.ui.SwingGeminiConfig;
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
import uno.anahata.nb.ai.systeminstructions.CoreNetBeansInstructionsProvider;
import uno.anahata.nb.ai.systeminstructions.IdeAlertsInstructionsProvider;
import uno.anahata.nb.ai.systeminstructions.OpenProjectsOverviewInstructionsProvider;
import uno.anahata.nb.ai.systeminstructions.OpenTopComponentsInstructionsProvider;
import uno.anahata.nb.ai.systeminstructions.OutputTabsInstructionsProvider;
import uno.anahata.nb.ai.tools.MavenPom;
import uno.anahata.nb.ai.tools.MavenSearch;

public class NetBeansGeminiConfig extends SwingGeminiConfig {

    private final String sessionUuid;

    public NetBeansGeminiConfig(String sessionUuid) {
        this.sessionUuid = sessionUuid;
    }

    @Override
    public List<Class<?>> getAutomaticFunctionClasses() {
        List<Class<?>> ret = super.getAutomaticFunctionClasses();
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
    public List<SystemInstructionProvider> getSystemInstructionProviders() {
        List<SystemInstructionProvider> providers = super.getSystemInstructionProviders();
        providers.add(new CoreNetBeansInstructionsProvider());
        providers.add(new IdeAlertsInstructionsProvider());
        providers.add(new OpenProjectsOverviewInstructionsProvider());
        providers.add(new OpenTopComponentsInstructionsProvider());
        providers.add(new OutputTabsInstructionsProvider());
        return providers;
    }
}
