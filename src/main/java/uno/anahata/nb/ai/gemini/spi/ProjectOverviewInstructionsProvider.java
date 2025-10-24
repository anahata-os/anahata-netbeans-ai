package uno.anahata.nb.ai.gemini.spi;

import com.google.genai.types.Part;
import java.util.Collections;
import java.util.List;
import uno.anahata.gemini.GeminiChat;
import uno.anahata.gemini.systeminstructions.SystemInstructionProvider;
import uno.anahata.nb.ai.functions.spi.Projects;

public class ProjectOverviewInstructionsProvider extends SystemInstructionProvider {

    @Override
    public String getId() {
        return "netbeans-project-overview";
    }

    @Override
    public String getDisplayName() {
        return "Project Overview";
    }

    @Override
    public List<Part> getInstructionParts(GeminiChat chat) {
        StringBuilder sb = new StringBuilder();
        List<String> openProjects = Projects.getOpenProjects();
        sb.append("Open Projects:").append(openProjects).append("\n");
        for (String project : openProjects) {
            sb.append(Projects.getOverview(project));
        }
        return Collections.singletonList(Part.fromText(sb.toString()));
    }
}