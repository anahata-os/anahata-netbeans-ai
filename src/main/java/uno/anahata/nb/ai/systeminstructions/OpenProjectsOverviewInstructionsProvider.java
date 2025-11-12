package uno.anahata.nb.ai.systeminstructions;

import com.google.genai.types.Part;
import java.util.ArrayList;
import java.util.List;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import uno.anahata.gemini.GeminiChat;
import uno.anahata.gemini.internal.GsonUtils;
import uno.anahata.gemini.config.systeminstructions.SystemInstructionProvider;
import uno.anahata.gemini.functions.schema.GeminiSchemaGenerator;
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
        return "Contains the Output of **Projects.getOverview(String projectId)** for each open project. Check the **description** and **response schema** on the tools **FunctionDeclaration**";
    }

    @Override
    @SneakyThrows
    public List<Part> getInstructionParts(GeminiChat chat) {
        List<String> projectIds = Projects.getOpenProjects();
        List<Part> parts = new ArrayList<>();        
        String jsonSchemaString = GsonUtils.prettyPrint(GeminiSchemaGenerator.generateSchema(ProjectOverview.class, "Schema for " + ProjectOverview.class));
        parts.add(Part.fromText("Schema:\n" + jsonSchemaString));
        for (String projectId : projectIds) {
            ProjectOverview overview = Projects.getOverview(projectId, chat);
            //String header = "#Output of Projects.getOverview(**" + projectId + "**)\n\n";
            
            //parts.add(Part.fromText(header + "\n" + json));
            parts.add(Part.fromText("**Projects.getOverview('" + projectId + "')**"));
            String json = "```json\n" + GsonUtils.prettyPrint(overview) + "\n```";
            parts.add(Part.fromText(json));
        }
        return parts;
    }
    
}
