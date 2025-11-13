package uno.anahata.nb.ai.workspace;

import com.google.genai.types.Part;
import java.util.ArrayList;
import java.util.List;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import uno.anahata.gemini.Chat;
import uno.anahata.gemini.content.ContextPosition;
import uno.anahata.gemini.content.ContextProvider;
import uno.anahata.gemini.internal.GsonUtils;
import uno.anahata.gemini.functions.schema.GeminiSchemaGenerator;
import uno.anahata.gemini.functions.schema.GeminiSchemaGenerator2;
import uno.anahata.nb.ai.tools.Projects;
import uno.anahata.nb.ai.model.projects.ProjectOverview;

@Slf4j
public class OpenProjectsOverviewContextProvider extends ContextProvider {

    public OpenProjectsOverviewContextProvider() {
        super(ContextPosition.AUGMENTED_WORKSPACE);
    }

    
    @Override
    public String getId() {
        return "netbeans-open-projects-overview";
    }

    @Override
    public String getDisplayName() {
        return "Project Overview";
    }

    @Override
    @SneakyThrows
    public List<Part> getParts(Chat chat) {
        List<String> projectIds = Projects.getOpenProjects();
        List<Part> parts = new ArrayList<>();
        //String jsonSchemaString = GsonUtils.prettyPrint(GeminiSchemaGenerator.generateSchema(ProjectOverview.class, "Schema for " + ProjectOverview.class));
        String jsonSchemaString = GeminiSchemaGenerator2.generateSchemaAsString(ProjectOverview.class, "Schema for " + ProjectOverview.class.getSimpleName());
        String schemaText = "**Schema for" + ProjectOverview.class.getSimpleName() + "**:\n```json\n" + jsonSchemaString + "\n```";
        String chunk = "The following is a **full detailed schema** of ProjectOverview which contains nested tree elements to give you the exact "
                + "package structure of all open projects and a snapshot of the Projects.getOverview tool that has been taken "
                + "after all other tool execution has happened. It is the most up to date information you have of the workspace \n"
                /*
                + "\n**THIS IS FRESH AUGMENTED CONTEXT, GETS GENERATED ON EVERY TURN RIGHT BEFORE CALLING YOU, EVEN IF IT IS IN THE SYSTEM INSTRUCTIONS"
                + "\n USE THIS TO LOCATE ANY OPEN FILES OF ANY OPEN PROJECTS**"
*/
                + "\n"
                + "\n" + schemaText
                + "\n";

        for (String projectId : projectIds) {
            ProjectOverview overview = Projects.getOverview(projectId, chat);
            String header = "\n#ProjectOverview: **" + projectId + "**\n\n";

            //parts.add(Part.fromText(header + "\n" + json));
            String json = "```json\n" + GsonUtils.prettyPrint(overview) + "\n```";
            //parts.add(Part.fromText("**Projects.getOverview('" + projectId + "')**:\n\njson"));
            chunk = chunk + header + json;
        }
        parts.add(Part.fromText(chunk));
        return parts;
    }

}
