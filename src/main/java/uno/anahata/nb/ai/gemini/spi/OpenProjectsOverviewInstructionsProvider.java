package uno.anahata.nb.ai.gemini.spi;

import com.google.genai.types.Part;
import com.google.gson.Gson;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ui.OpenProjects;
import uno.anahata.gemini.GeminiChat;
import uno.anahata.gemini.internal.GsonUtils;
import uno.anahata.gemini.systeminstructions.SystemInstructionProvider;
import uno.anahata.nb.ai.functions.spi.Projects;
import uno.anahata.nb.ai.project.overview.ProjectOverview;

@Slf4j
public class OpenProjectsOverviewInstructionsProvider extends SystemInstructionProvider {
    private static final Gson GSON = GsonUtils.getGson();

    @Override
    public String getId() {
        return "netbeans-open-projects-overview";
    }

    @Override
    public String getDisplayName() {
        return "Open Projects Overview";
    }

    @Override
    public List<Part> getInstructionParts(GeminiChat chat) {
        if (!isEnabled()) {
            return Collections.emptyList();
        }
        List<Part> parts = new ArrayList<>();
        Project[] projects = OpenProjects.getDefault().getOpenProjects();

        for (Project p : projects) {
            try {
                ProjectOverview overview = Projects.getOverview(p.getProjectDirectory().getNameExt(), chat);
                String json = GSON.toJson(overview);
                String partContent = "#Output of Projects.getOverview(**" + overview.getId() + "**)\n" + json;
                parts.add(Part.fromText(partContent));
            } catch (Exception e) {
                log.warn("Could not get project overview for {}", p.getProjectDirectory().getNameExt(), e);
                parts.add(Part.fromText("Error getting overview for project " + p.getProjectDirectory().getNameExt() + ": " + e.getMessage()));
            }
        }

        return parts;
    }
}
