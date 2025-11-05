package uno.anahata.nb.ai.gemini.spi;

import com.google.genai.types.Part;
import java.util.ArrayList;
import java.util.List;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import uno.anahata.gemini.GeminiChat;
import uno.anahata.gemini.internal.GsonUtils;
import uno.anahata.gemini.systeminstructions.SystemInstructionProvider;
import uno.anahata.nb.ai.functions.spi.Projects;
import uno.anahata.nb.ai.project.overview.ProjectOverview;

@Slf4j
public class OpenProjectsOverviewInstructionsProvider extends SystemInstructionProvider {

    @Override
    public String getId() {
        return "netbeans-open-projects-overview";
    }

    @Override
    public String getDisplayName() {
        return "Open Projects Overview";
    }

    @Override
    @SneakyThrows
    public List<Part> getInstructionParts(GeminiChat chat) {
        List<String> projectIds = Projects.getOpenProjects();
        List<Part> parts = new ArrayList<>();
        for (String projectId : projectIds) {
            ProjectOverview overview = Projects.getOverview(projectId);
            String header = "#Output of Projects.getOverview(**" + projectId + "**)\n";
            String json = "```json\n" + GsonUtils.prettyPrint(overview) + "\n```";
            parts.add(Part.fromText(header + "\n" + json));
        }
        return parts;
    }
}
