package uno.anahata.ai.nb;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import uno.anahata.ai.context.provider.ContextProvider;
import uno.anahata.ai.nb.context.CoreNetBeansInstructionsProvider;
import uno.anahata.ai.nb.context.IdeAlertsContextProvider;
import uno.anahata.ai.nb.context.OpenTopComponentsContextProvider;
import uno.anahata.ai.nb.context.OutputTabsContextProvider;
import uno.anahata.ai.nb.context.ProjectOverviewContextProvider;
import uno.anahata.ai.nb.tools.Coding;
import uno.anahata.ai.nb.tools.Editor;
import uno.anahata.ai.nb.tools.Git;
import uno.anahata.ai.nb.tools.IDE;
import uno.anahata.ai.nb.tools.JavaAnalysis;
import uno.anahata.ai.nb.tools.JavaDocs;
import uno.anahata.ai.nb.tools.JavaIntrospection;
import uno.anahata.ai.nb.tools.JavaSources;
import uno.anahata.ai.nb.tools.NetBeansProjectJVM;
import uno.anahata.ai.nb.tools.Output;
import uno.anahata.ai.nb.tools.Projects;
import uno.anahata.ai.nb.tools.Refactor;
import uno.anahata.ai.nb.tools.TopComponents;
import uno.anahata.ai.nb.tools.deprecated.MavenTools;
import uno.anahata.ai.swing.SwingChatConfig;

public class NetBeansChatConfig extends SwingChatConfig {

    private final String sessionUuid;
    
    // A map to hold and manage our dynamic project-specific providers.
    private final Map<String, List<ContextProvider>> projectProviders = new HashMap<>();

    public NetBeansChatConfig(String sessionUuid) {
        this.sessionUuid = sessionUuid;
    }

    @Override
    public List<Class<?>> getToolClasses() {
        List<Class<?>> ret = super.getToolClasses();
        ret.add(Coding.class);
        ret.add(Editor.class);
        ret.add(Git.class);
        ret.add(IDE.class);
        ret.add(JavaAnalysis.class);
        ret.add(JavaDocs.class);
        ret.add(JavaIntrospection.class);
        ret.add(JavaSources.class);
        ret.add(MavenTools.class);
        ret.add(NetBeansProjectJVM.class);
        ret.add(Output.class);
        ret.add(Projects.class);
        ret.add(Refactor.class);
        ret.add(TopComponents.class);
        return ret;
    }

    @Override
    public String getSessionId() {
        return "netbeans-" + sessionUuid;
    }

    @Override
    public List<ContextProvider> getContextProviders() {
        // 1. Start with the base list of providers from the superclass.
        List<ContextProvider> allProviders = super.getContextProviders();

        // 2. Synchronize our project-specific providers with the current IDE state.
        synchronizeProjectProviders();

        // 3. Add all currently active project providers to the final list.
        projectProviders.values().forEach(allProviders::addAll);

        // 4. Add the non-project-specific NetBeans providers.
        allProviders.add(new CoreNetBeansInstructionsProvider());
        allProviders.add(new OpenTopComponentsContextProvider());
        allProviders.add(new OutputTabsContextProvider());

        return allProviders;
    }

    /**
     * Synchronizes the managed list of project-specific context providers with the
     * currently open projects in the IDE. This method ensures that providers for
     * closed projects are removed and providers for newly opened projects are added,
     * while respecting the enabled/disabled state of existing providers.
     */
    private void synchronizeProjectProviders() {
        Set<String> openProjectIds = new HashSet<>(Projects.getOpenProjects());

        // Removal Step: Remove providers for any projects that are no longer open.
        projectProviders.keySet().removeIf(projectId -> !openProjectIds.contains(projectId));

        // Addition Step: Add providers for any newly opened projects.
        for (String projectId : openProjectIds) {
            // computeIfAbsent ensures we only create providers if they don't already exist for this project.
            // This is the key to not re-enabling a provider that a user has manually disabled.
            projectProviders.computeIfAbsent(projectId, id -> {
                List<ContextProvider> newProviders = new ArrayList<>();
                newProviders.add(new ProjectOverviewContextProvider(id));
                newProviders.add(new IdeAlertsContextProvider(id));
                return newProviders;
            });
        }
    }
}