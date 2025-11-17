package uno.anahata.nb.ai.systeminstructions;

import com.google.genai.types.Part;
import java.io.File;
import java.util.Collections;
import java.util.List;
import lombok.SneakyThrows;
import uno.anahata.gemini.Chat;
import uno.anahata.gemini.content.ContextProvider;
import uno.anahata.ai.nb.AnahataTopComponent;
import uno.anahata.nb.ai.tools.Maven;
import uno.anahata.nb.ai.tools.Projects;

public class CoreNetBeansInstructionsProvider extends ContextProvider {

    @Override
    public String getId() {
        return "netbeans-core";
    }

    @Override
    public String getDisplayName() {
        return "NetBeans Core";
    }

    @Override
    @SneakyThrows
    public List<Part> getParts(Chat chat) {
        String text = "Your host environment is the Anahata AI Assistant NetBeans plugin. A NetBeans plugin (nbm module) all code you run in RunningJVM runs inside "
                + "netbeans very own jvm using netbeans classloader modules system, all netbeans apis and active modules are available to you at runtime and the default compilers classpath and ClassLoader in RunningJVM has access to this jars and classess at runtime.\n"
                + "The main TopComponent class of the plugin is:" + AnahataTopComponent.class.getName() + "\n"
                + "Your netbeans and java notes are your primary persitent memory in this host environment"
                + "and they must always be in the context of this session.\n"
                + "The anahata.md file located on the root of each project folder is your persistent memory for anything related to that project, you must read this file if you see it in the projects root or create one if it doesnt exist and keep it up to date automatically with changes in the code base, goals, todos, in progress tasks, etc. "
                + "\n\n## Core Principle: Prefer IDE APIs over Direct File Manipulation"
                + "\nWhen renaming, moving, or deleting source files, do not use direct file system tools like `LocalFiles.moveFile`. Always prefer to use the NetBeans Refactoring APIs, as documented in your `netbeans.md` notes. This ensures that all code references are updated correctly, preventing compilation errors."
                + "\n\nThe user's NetBeansProjects folder is located at: " + System.getProperty("user.home") + File.separator + "NetBeansProjects" + "\n"
                + "The current open projects are as given by the Projects.getOpenProjects() tool are: " + Projects.getOpenProjects() + "\n"
                + "\nPrefer the Maven installation if you need to use maven in this environment. The maven installation used by netbeans as given by the tool Maven.getMavenPath() is: " + Maven.getMavenPath();

        return Collections.singletonList(Part.fromText(text));
    }
}
