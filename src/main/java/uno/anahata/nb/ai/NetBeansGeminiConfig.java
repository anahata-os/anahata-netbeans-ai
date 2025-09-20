package uno.anahata.nb.ai;

import com.google.genai.types.Part;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.exception.ExceptionUtils;
import uno.anahata.gemini.GeminiAPI;
import uno.anahata.gemini.GeminiConfig;
import uno.anahata.nb.ai.functions.spi.Git;
import uno.anahata.nb.ai.functions.spi.IDE;
import uno.anahata.nb.ai.functions.spi.Maven;
import uno.anahata.nb.ai.functions.spi.ProjectActions;
import uno.anahata.nb.ai.functions.spi.Workspace;

public class NetBeansGeminiConfig extends GeminiConfig {
    
    private final GeminiAPI api = new GeminiAPI();

    @Override
    public GeminiAPI getApi() {
        return api;
    }

    @Override
    public List<Class<?>> getAutomaticFunctionClasses() {
        List<Class<?>> ret = new ArrayList<>();
        ret.add(Git.class);
        ret.add(IDE.class);
        ret.add(Maven.class);
        ret.add(Workspace.class);
        ret.add(ProjectActions.class);
        return ret;
    }
    
    /**
     * Provides the parts of the system instructions that are specific to the NetBeans environment.
     */
    @Override
    public List<Part> getHostSpecificSystemInstructionParts() {
        List<Part> parts = new ArrayList<>();
        
        // Add the NetBeans role and gemini.md directive
        parts.add(Part.fromText("Your host environment is the  Gemini NetBeans Plugin. "
                + "\nThe main TopComponent class of the plugin is:" + GeminiTopComponent.class.getName()
                + "\nYour netbeans and java notes are your primary persitent memory in this host environment"
                        + "and they must always be in the context of this session"
                + "\nThe gemini.md file located on the root of each project folder is your persistent memory for anything related to that project, "
                + "keep it up to date with changes in the code base, goals, todos, etc, "));
        
        // Add the live IDE alerts
        String ideAlerts = "IDE.getAllIDEAlerts:";
        try {
            ideAlerts += IDE.getAllIDEAlerts();
        } catch (Exception e) {
            ideAlerts += ExceptionUtils.getStackTrace(e);
        }
        parts.add(Part.fromText(ideAlerts));
        
        return parts;
    }
}
