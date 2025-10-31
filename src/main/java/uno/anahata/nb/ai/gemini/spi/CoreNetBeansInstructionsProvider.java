package uno.anahata.nb.ai.gemini.spi;

import com.google.genai.types.Part;
import java.io.File;
import java.util.Collections;
import java.util.List;
import uno.anahata.gemini.GeminiChat;
import uno.anahata.gemini.systeminstructions.SystemInstructionProvider;
import uno.anahata.nb.ai.functions.spi.Projects;

public class CoreNetBeansInstructionsProvider extends SystemInstructionProvider {

    @Override
    public String getId() {
        return "netbeans-core";
    }

    @Override
    public String getDisplayName() {
        return "NetBeans Core";
    }

    @Override
    public List<Part> getInstructionParts(GeminiChat chat) {
        String netbeansProjectsDir = new File(System.getProperty("user.home"), "NetBeansProjects").getAbsolutePath();
        String text = "Your host environment is the Anahata AI Assistant NetBeans plugin.\n"
                + "The main TopComponent class of the plugin is:uno.anahata.nb.ai.AnahataTopComponent\n"
                + "Your netbeans and java notes are your primary persitent memory in this host environmentand they must always be in the context of this session.\n"
                + "The anahata.md file located on the root of each project folder is your persistent memory for anything related to that project, you must read this file if you detect it or create one if it doesnt exist and keep it up to date automatically with changes in the code base, goals, todos, etc, "
                + "The user's NetBeansProjects folder is located at: " + netbeansProjectsDir
                + "\nProjects.getOpenProjects(): " + Projects.getOpenProjects();
        return Collections.singletonList(Part.fromText(text));
    }
}