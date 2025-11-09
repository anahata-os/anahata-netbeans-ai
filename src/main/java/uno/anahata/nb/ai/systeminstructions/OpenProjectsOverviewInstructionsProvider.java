package uno.anahata.nb.ai.systeminstructions;

import com.google.genai.types.Part;
import java.util.ArrayList;
import java.util.List;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import uno.anahata.gemini.GeminiChat;
import uno.anahata.gemini.internal.GsonUtils;
import uno.anahata.gemini.systeminstructions.SystemInstructionProvider;
import uno.anahata.nb.ai.tools.Projects;
import uno.anahata.nb.ai.model.projects.ProjectOverview;

@Slf4j
public class OpenProjectsOverviewInstructionsProvider extends SystemInstructionProvider {

    @Override
    public String getId() {
        return "netbeans-open-projects-overview";
    }

    @Override
    public String getDisplayName() {
        return "Projects.getOverview(all open projects)";
    }

    @Override
    @SneakyThrows
    public List<Part> getInstructionParts(GeminiChat chat) {
        List<String> projectIds = Projects.getOpenProjects();
        List<Part> parts = new ArrayList<>();
        for (String projectId : projectIds) {
            ProjectOverview overview = Projects.getOverview(projectId);
            String header = "#Output of Projects.getOverview(**" + projectId + "**)\n\n";
            String json = "```json\n" + GsonUtils.prettyPrint(overview) + "\n```";
            parts.add(Part.fromText(header + "\n" + json));
        }
        return parts;
    }
}
