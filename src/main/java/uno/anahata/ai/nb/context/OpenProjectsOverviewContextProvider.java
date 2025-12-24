/* Licensed under the Apache License, Version 2.0 */
package uno.anahata.ai.nb.context;

import com.google.genai.types.Part;
import java.util.ArrayList;
import java.util.List;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import uno.anahata.ai.Chat;
import uno.anahata.ai.context.provider.ContextPosition;
import uno.anahata.ai.context.provider.ContextProvider;
import uno.anahata.ai.internal.GsonUtils;
import uno.anahata.ai.nb.tools.Projects;
import uno.anahata.ai.nb.model.projects.ProjectOverview;

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
        
        /*
        Schema poSchema = GeminiSchemaGenerator.generateSchema(ProjectOverview.class, "Schema for " + ProjectOverview.class.getSimpleName());
        String schemaText = "**Schema for" + ProjectOverview.class.getSimpleName() + "**:\n```json\n" + jsonSchemaString + "\n```";
        String chunk = "The following is a **full detailed schema** of ProjectOverview which contains nested tree elements to give you the exact "
                + "package structure of all open projects and a snapshot of the Projects.getOverview tool that has been taken "
                + "after all other tool execution has happened. It is the most up to date information you have of the workspace \n"
                + "\n"
                + "\n" + schemaText
                + "\n";
                */
        for (String projectId : projectIds) {
            ProjectOverview overview = Projects.getOverview(projectId, chat);
            String header = "\n#ProjectOverview: **" + projectId + "**\n\n";

            //parts.add(Part.fromText(header + "\n" + json));
            String json = "```json\n" + GsonUtils.prettyPrint(overview) + "\n```";
            //parts.add(Part.fromText("**Projects.getOverview('" + projectId + "')**:\n\njson"));
            //chunk = chunk + header + json;
            parts.add(Part.fromText(header + json));
        }
        
        return parts;
    }

}
