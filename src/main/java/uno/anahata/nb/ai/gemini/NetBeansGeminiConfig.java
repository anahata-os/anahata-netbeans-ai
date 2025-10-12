package uno.anahata.nb.ai.gemini;

import com.google.genai.types.Part;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.lang3.exception.ExceptionUtils;
import uno.anahata.gemini.GeminiAPI;
import uno.anahata.gemini.GeminiConfig;
import static uno.anahata.gemini.GeminiConfig.logger;
import uno.anahata.gemini.ui.SwingGeminiConfig;
import uno.anahata.nb.ai.AnahataTopComponent;
import uno.anahata.nb.ai.functions.spi.Coding;
import uno.anahata.nb.ai.functions.spi.Editor;
import uno.anahata.nb.ai.functions.spi.Git;
import uno.anahata.nb.ai.functions.spi.IDE;
import uno.anahata.nb.ai.functions.spi.Maven;
import uno.anahata.nb.ai.functions.spi.Projects;
import uno.anahata.nb.ai.functions.spi.Output;
import uno.anahata.nb.ai.functions.spi.TopComponents;
import uno.anahata.nb.ai.functions.spi.Workspace;

public class NetBeansGeminiConfig extends SwingGeminiConfig {

    
    @Override
    public List<Class<?>> getAutomaticFunctionClasses() {
        List<Class<?>> ret = new ArrayList<>();
        ret.add(Git.class);
        ret.add(IDE.class);
        ret.add(Maven.class);
        ret.add(Workspace.class);
        ret.add(Output.class);
        ret.add(Projects.class);
        ret.add(Editor.class);
        ret.add(Coding.class);
        ret.add(TopComponents.class);
        return ret;
    }

    /**
     * Provides the parts of the system instructions that are specific to the
     * NetBeans environment.
     */
    @Override
    public List<Part> getHostSpecificSystemInstructionParts() {
        List<Part> parts = new ArrayList<>();

        // Add the NetBeans role and gemini.md directive
        parts.add(Part.fromText("Your host environment is the Anahata AI Assistant NetBeans plugin."
                + "\nThe main TopComponent class of the plugin is:" + AnahataTopComponent.class.getName()
                + "\nYour netbeans and java notes are your primary persitent memory in this host environment"
                + "and they must always be in the context of this session."
                + "\nThe gemini.md file located on the root of each project folder is your persistent memory for anything related to that project, "
                + "you must read this file if you detect it or create one if it doesnt exist and keep it up to date automatically with changes in the code base, goals, todos, etc, "));

        // Add the live IDE alerts
        String ideAlerts = "IDE.getAllIDEAlerts:";
        try {
            ideAlerts += IDE.getAllIDEAlerts();
        } catch (Exception e) {
            ideAlerts += ExceptionUtils.getStackTrace(e);
            parts.add(Part.fromText(ideAlerts));
        }
        
        parts.add(Part.fromText(ideAlerts));
        String openProjects = "Open Projects:";
        try {
            openProjects+=Projects.getOpenProjects();
            parts.add(Part.fromText(openProjects));
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Exception in Projects.getOpenProjects()", e);
            parts.add(Part.fromText(openProjects + "\n" + ExceptionUtils.getStackTrace(e)));
        }
        
        // Add the critical file modification rule
        parts.add(Part.fromText(
            "**CRITICAL FILE READ/MODIFICATION RULEs:** Before using any tool that reads or writes a stateful resource (e.g., `writeFile`, `readFile`, `proposeChange`, etc), you MUST perform this check:\n" +
            "1. Find the file's `lm=` (last modified) timestamp in the `Projects.getOverview` output below.\n" +
            "2. Compare this `lm=` timestamp with the `lastModified` timestamp of the same file that you have in your current context (from a previous read/write operation such as `readFile`, `writeFile`, 'proposeChange').\n" +
            "3. **If the `lm=` timestamp from the overview is NEWER**, your context is stale. You MUST use `readFile` to get the latest version of the file *before* attempting to write or patch it. This is not optional; it is required to prevent overwriting user changes." +
            "4. **If the `lm=` timestamp from the overview is THE SAME** as the file in context, your context is valid. You DO NOT use `readFile` again as it is a waste of api, token and user time"
        ));
        
        
        try {
            for (String projectId : Projects.getOpenProjects()) {
                String projectOverView = "";
                try {
                    projectOverView = "Projects.getOverview(" + projectId + ")\n";
                    projectOverView += "----------------------------------------------\n";
                    projectOverView += Projects.getOverview(projectId);
                    parts.add(Part.fromText(projectOverView));
                } catch (Exception e) {
                    logger.log(Level.SEVERE, "Exception in Projects.getOverview(" + projectId + ")", e);
                    projectOverView += ExceptionUtils.getStackTrace(e);
                    parts.add(Part.fromText(projectOverView));
                }
                //openProjects += "\n-----------------------------------------";
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Exception in Projects.getOpenProjects()", e);
            String err = ExceptionUtils.getStackTrace(e);
            parts.add(Part.fromText(err));
        }

        

        String openEditorTabs = "Editor.getOpenFiles():";
        openEditorTabs += "\n-----------------------------------:";
        try {
            openEditorTabs += Editor.getOpenFiles();
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Exception in Editor.getOpenFiles()", e);
            openEditorTabs += ExceptionUtils.getStackTrace(e);
        }

        parts.add(Part.fromText(openEditorTabs));

        return parts;
    }

    @Override
    public String getApplicationInstanceId() {
        return "netbeans-plugin";
    }
}
