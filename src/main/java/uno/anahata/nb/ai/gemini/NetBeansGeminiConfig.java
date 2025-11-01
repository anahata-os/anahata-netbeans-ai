package uno.anahata.nb.ai.gemini;

import java.util.ArrayList;
import java.util.List;
import uno.anahata.gemini.systeminstructions.SystemInstructionProvider;
import uno.anahata.gemini.ui.SwingGeminiConfig;
import uno.anahata.nb.ai.functions.spi.Coding;
import uno.anahata.nb.ai.functions.spi.Editor;
import uno.anahata.nb.ai.functions.spi.Git;
import uno.anahata.nb.ai.functions.spi.IDE;
import uno.anahata.nb.ai.functions.spi.Maven;
import uno.anahata.nb.ai.functions.spi.Projects;
import uno.anahata.nb.ai.functions.spi.Output;
import uno.anahata.nb.ai.functions.spi.TopComponents;
import uno.anahata.nb.ai.functions.spi.Workspace;
import uno.anahata.nb.ai.gemini.spi.CoreNetBeansInstructionsProvider;
import uno.anahata.nb.ai.gemini.spi.IdeAlertsInstructionsProvider;
import uno.anahata.nb.ai.gemini.spi.OpenFilesInEditorInstructionsProvider;
import uno.anahata.nb.ai.gemini.spi.OpenProjectsOverviewInstructionsProvider;

public class NetBeansGeminiConfig extends SwingGeminiConfig {

    @Override
    public List<Class<?>> getAutomaticFunctionClasses() {
        List<Class<?>> ret = super.getAutomaticFunctionClasses();
        ret.add(Git.class);
        ret.add(IDE.class);
        ret.add(Maven.class);
        ret.add(Workspace.class);
        ret.add(Output.class);
        ret.add(Projects.class);
        ret.add(Editor.class);
        ret.add(Coding.class);
        ret.add(TopComponents.class);
        return ret;
    }

    @Override
    public String getApplicationInstanceId() {
        return "netbeans-plugin";
    }

    @Override
    public List<SystemInstructionProvider> getApplicationSpecificInstructionProviders() {
        List<SystemInstructionProvider> providers = new ArrayList<>();
        providers.add(new CoreNetBeansInstructionsProvider());
        providers.add(new IdeAlertsInstructionsProvider());
        providers.add(new OpenProjectsOverviewInstructionsProvider());
        providers.add(new OpenFilesInEditorInstructionsProvider());
        return providers;
    }
}
