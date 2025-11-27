package uno.anahata.ai.nb.context;

import com.google.genai.types.Part;
import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.prefs.Preferences;
import java.util.stream.Collectors;
import lombok.SneakyThrows;
import org.openide.util.NbPreferences;
import uno.anahata.ai.Chat;
import uno.anahata.ai.context.provider.ContextProvider;
import uno.anahata.ai.nb.AnahataTopComponent;
import uno.anahata.ai.nb.tools.Projects;
import uno.anahata.ai.nb.tools.deprecated.MavenTools;

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
        String text = """
                      Your host environment is the Anahata AI Assistant NetBeans plugin.
                      The main TopComponent class of the plugin is: %s

                      ## Runtime Environment & Classpath ("Hot Reload")
                      You are executing within the NetBeans Platform's OSGi-like module system. All active IDE modules are on the classpath.
                      The `NetBeansProjectJVM` tool provides a powerful "hot-reload" capability by leveraging NetBeans' **Compile on Save** feature. It dynamically constructs a classpath that **prioritizes** the target project's own build directories (e.g., `target/classes`). This ensures that any code you compile and execute will use the very latest, unsaved changes directly from the editor, without requiring a project rebuild.
                      **IMPORTANT:** Be extremely careful when using this tool on NetBeans Modules (NBMs). If you include dependencies that are already provided by the IDE (like NetBeans Platform APIs), you will cause fatal `LinkageError` exceptions.

                      ## Project-Specific Instructions (`anahata.md` & `tasks.md`)
                      The `anahata.md` file, located in a project's root, is your primary source for its specific architecture, goals, and principles. Its content is **automatically included** in your context on every turn by the `ProjectOverviewContextProvider`. The `AnahataNodeFactory` will automatically create this file if it is missing. The project's actionable task board is located in `tasks.md`.

                      ## Core Principle: Prefer IDE APIs over Direct File Manipulation
                      - **Writing Files:** Always prefer `Coding.suggestChange` over `LocalFiles.writeFile` unless explicitly instructed otherwise. This provides the user with a visual diff and a clear approval workflow.
                      - **Renaming/Moving:** Never use direct file system tools like `LocalFiles.moveFile` for source code. Always use the NetBeans Refactoring APIs (`Refactor.renameFile`) to ensure all code references are updated correctly.
                      - **Proactive Analysis:** Before proposing changes, use Java analysis tools to check for potential compilation errors or to diagnose the impact of a refactoring operation.

                      ## Core Principle: Interpreting `suggestChange` Results
                      The `Coding.suggestChange` tool has a **two-step approval process**. Your tool call being approved (`YES` or `Autopilot`) **only means the diff dialog was displayed to the user**. It **does not mean the user accepted your change**. You MUST always inspect the `SuggestChangeResult` object returned by the tool; the `status` field will tell you if the change was `ACCEPTED` or `CANCELLED`. Do not assume a change was applied until you have verified it in the tool's response.

                      ## Core Principle: Maven Dependency Management Workflow
                      1.  **Search:** Use `MavenTools.searchMavenIndex` to find the correct coordinates.
                      2.  **Pre-flight Check:** Use `MavenTools.downloadDependencyArtifact` to verify the main artifact exists.
                      3.  **Add Dependency:** Use the definitive `MavenTools.addDependency` super-tool.
                      4.  **Verify:** Check the `AddDependencyResult` object to confirm success.
                      **Known Bug:** Be aware that `MavenTools.addDependency` may sometimes incorrectly add a `<classifier>jar</classifier>` tag to the `pom.xml`. You must manually inspect and correct this if it occurs.

                      The user's NetBeansProjects folder is located at: %s
                      The following projects are available in that folder: [%s]
                      The current open projects are as given by the Projects.getOpenProjects() tool are: %s

                      Prefer the Maven installation if you need to use maven in this environment. The maven installation used by netbeans as given by the tool Maven.getMavenPath() is: %s
                      """.formatted(
                AnahataTopComponent.class.getName(),
                getNetBeansProjectsFolder(),
                listProjectFolders(),
                Projects.getOpenProjects(),
                MavenTools.getMavenPath()
        );

        return Collections.singletonList(Part.fromText(text));
    }

    /**
     * Programmatically finds the NetBeans projects folder by querying the IDE's preferences.
     * This is more robust than hardcoding the path.
     * @return The absolute path to the configured NetBeansProjects folder.
     */
    private String getNetBeansProjectsFolder() {
        String defaultProjectsFolder = System.getProperty("user.home") + File.separator + "NetBeansProjects";
        try {
            // The setting is stored by the ProjectsTab UI component, which is not a public API.
            Class<?> projectsTabClass = Class.forName("org.netbeans.modules.project.ui.ProjectsTab");
            Preferences prefs = NbPreferences.forModule(projectsTabClass);
            return prefs.get("projectsFolder", defaultProjectsFolder);
        } catch (ClassNotFoundException e) {
            // If the internal class isn't found, fall back to the standard default.
            return defaultProjectsFolder;
        }
    }
    
    /**
     * Lists the names of all subdirectories in the NetBeansProjects folder.
     * @return A comma-separated string of project folder names, or "N/A" if the folder doesn't exist or is empty.
     */
    private String listProjectFolders() {
        File projectsDir = new File(getNetBeansProjectsFolder());
        if (projectsDir.exists() && projectsDir.isDirectory()) {
            File[] projectDirs = projectsDir.listFiles(File::isDirectory);
            if (projectDirs != null && projectDirs.length > 0) {
                return Arrays.stream(projectDirs)
                             .map(File::getName)
                             .collect(Collectors.joining(", "));
            }
        }
        return "N/A";
    }
}
