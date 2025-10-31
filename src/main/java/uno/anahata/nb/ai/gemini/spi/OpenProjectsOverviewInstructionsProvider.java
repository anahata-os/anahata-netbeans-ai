package uno.anahata.nb.ai.gemini.spi;

import com.google.genai.types.Part;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.apache.commons.lang3.exception.ExceptionUtils;
import uno.anahata.gemini.GeminiChat;
import uno.anahata.gemini.internal.GsonUtils;
import uno.anahata.gemini.systeminstructions.SystemInstructionProvider;
import uno.anahata.nb.ai.functions.spi.Projects;

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
    public List<Part> getInstructionParts(GeminiChat chat) {
        
        List<Part> ret = new ArrayList<>();
        List<String> openProjects = Projects.getOpenProjects();
        for (String projectId : openProjects) {
            StringBuilder sb = new StringBuilder();
            sb.append("#Output of Projects.getOverview(**").append(projectId).append("**)\n");
            try {
                sb.append(GsonUtils.prettyPrint(Projects.getOverview(projectId)));
            } catch (Exception e) {
                ExceptionUtils.getStackTrace(e);
            }
            sb.append("\n");
            ret.add(Part.fromText(sb.toString()));
        }
        return ret;

        //sb.append(Projects.getOverview2(project));
    }




}
