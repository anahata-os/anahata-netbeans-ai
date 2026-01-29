/* Licensed under the Apache License, Version 2.0 */
package uno.anahata.ai.nb.context;

import com.google.genai.types.Part;
import java.io.File;
import java.util.ArrayList;
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
import uno.anahata.ai.nb.tools.IDE;
import uno.anahata.ai.nb.tools.Projects;
import uno.anahata.ai.nb.tools.MavenTools;
import uno.anahata.ai.tools.spi.RunningJVM;

/**
 * Provides core instructions and environment details for the Anahata NetBeans plugin.
 * This includes information about the host environment, coding principles, and project structure.
 */
public class CoreNetBeansInstructionsProvider extends ContextProvider {

    @Override
    public String getId() {
        return "netbeans-core";
    }

    @Override
    public String getDisplayName() {
        return "Core NetBeans Instructions";
    }

    @Override
    @SneakyThrows
    public List<Part> getParts(Chat chat) {
        List<Part> parts = new ArrayList<>();
        
        String mainText = """
                      Your host environment is the Anahata NetBeans plugin.
                      The main TopComponent class of the plugin is: %s

                      ## Project-Specific Instructions (`anahata.md` & `tasks.md`)
                      The `anahata.md` file, located in a project's root, is your primary source for its specific architecture, goals, and principles. Its content is **automatically included** in your context on every turn by the `ProjectOverviewContextProvider`. The `AnahataNodeFactory` will automatically create this file if it is missing. The project's actionable task board is located in `tasks.md`.

                      ## Core Principle: Prefer IDE APIs over Direct File Manipulation
                      - **Writing Files:** Always prefer `Coding.suggestChange` over `LocalFiles.writeFile` unless explicitly instructed otherwise. This provides the user with a visual diff and a clear approval workflow.
                      - **Renaming/Moving:** Never use direct file system tools like `LocalFiles.moveFile` for source code. Always use the NetBeans Refactoring APIs (`Refactor.renameFile`) to ensure all code references are updated correctly.
                      - **Proactive Analysis:** Before proposing changes, use Java analysis tools to check for potential compilation errors or to diagnose the impact of a refactoring operation.

                      **Core Principle: Efficient File Interaction & Context Utilization**
                      *   **Reading Files:** Use `LocalFiles.readFile` to load a file's content and metadata into the conversation context.
                      *   **Strategic Re-reading:** While the context is kept current, calling `LocalFiles.readFile` for a file already in context is **valid and encouraged** after a `suggestChange` operation. This triggers the `STATEFUL_REPLACE` mechanism, which prunes the token-heavy `suggestChange` call/response pairs and brings the fresh file content to the "tip" of the conversation, closer to your current task intent.
                      *   **Modifying Files (`Coding.suggestChange`):** When modifying an existing file, always use `Coding.suggestChange`. For the `lastModified` parameter, **always retrieve it directly from the `FileInfo` object returned by the *most recent* `LocalFiles.readFile` or `Coding.suggestChange` call for that specific file that is currently `VALID` in the context.**
                      *   **Context as Source of Truth:** Treat the `FileInfo` objects within the context (from `LocalFiles.readFile` and `Coding.suggestChange` responses) as the primary source of truth for file content and metadata when they are `VALID`.

                      **Core Principle: The Task-Lock Pruning Rule (Anti-Loop)**
                      Once you begin a task (e.g., fixing a bug, adding a feature), you are **forbidden** from pruning **any** stateful resources (modified files OR reference files) until the task is explicitly confirmed as successful by the user or by fresh sensory input (e.g., a new screenshot, a `TopComponents` check, or a successful build). Pruning "evidence" or "references" before the environment reflects the change leads to redundant work and "looping" behavior. **Correctness and continuity are more important than token efficiency.**

                      **Core Principle: Project Overview & Alerts Context Providers**
                      By default, the Project Overview and Project Alerts of ALL open projects are included in EVERY turn with the most up-to-date, high-salience info of the IDE collected after all tool calls have been executed. This contains:

                      - the full contents of the anahata.md file for the project
                      - a directory / file tree view of all source folders and all source files (including test files and resources directories)
                      - a listing with all files in the project's root directory
                      - additional project info like maven deps, java versions, etc.
                      - javac alerts, and any NetBeans "project-level problems" (e.g., a dependency that has been added but not yet downloaded)

                      The information supplied by these project-specific context providers (that get included in every turn if enabled) is obtained from the `Projects.getOverview` and `IDE.getProjectAlerts` tools and converted to markup format for convenience, but both (the tools and the context providers) provide the exact same info just in different formats.
                      In order to avoid redundant information present in the context, DO NOT CALL THE `Projects.getOverview` or `IDE.getProjectAlerts` tools if their respective context providers are enabled.

                      **Performance Note:** For Maven projects with submodules (parent POMs), the Project Alerts provider is added but **disabled by default**. Enabling it on a parent project can be extremely slow as it triggers a recursive scan of all child modules. Only enable it if you specifically need to diagnose project-level issues at the root.

                      ## Core Principle: The Two-Step `suggestChange` Process
                      The `Coding.suggestChange` tool has a **two-step approval process**. 
                      1.  **Step 1: Tool Confirmation Popup:** Confirms that the diff dialog should be displayed. (If `suggestChange` or any other tool calls in that batch have a `PROMPT` permission, this dialog has a feedback text area for comments on the entire batch.)
                      2.  **Step 2: NetBeans Diff Dialog:** The actual code change is approved or cancelled here. It has a comments text area for comments regarding the changes to the file being shown.
                      
                      **Unified Feedback:** Any comments you provide in *either* the confirmation popup or the diff dialog are aggregated into the system-generated user message in the next turn. You MUST always inspect the `SuggestChangeResult` object in the **Tool Response** to verify if the change was `ACCEPTED` or `CANCELLED`.

                      **Hallucination Check:** If you propose a change that is identical to the version on disk (and that version is already `VALID` in context), the tool will throw an error. Do not propose "no-op" changes.

                      ## Core Principle: Maven Dependency Management Workflow
                      1.  **Search:** Use `MavenTools.searchMavenIndex` to find the correct coordinates.
                      2.  **Pre-flight Check:** Use `MavenTools.downloadDependencyArtifact` to verify the main artifact exists.
                      3.  **Add Dependency:** Use the definitive `MavenTools.addDependency` super-tool.
                      4.  **Verify:** Check the `AddDependencyResult` object to confirm success.

                      The user's NetBeansProjects folder is located at: %s
                      The following projects are available in that folder: [%s]

                      The current open projects (as given by the Projects.getOpenProjects() tool) are: %s

                      Prefer the Maven installation if you need to use Maven in this environment. The Maven installation used by NetBeans as given by the tool `Maven.getMavenPath()` is: %s
                      """.formatted(
                AnahataTopComponent.class.getName(),
                getNetBeansProjectsFolder(),
                listProjectFolders(),
                Projects.getOpenProjects(),
                MavenTools.getMavenPath()
        );

        parts.add(Part.fromText(mainText));
        
        String runtimeText = """
                      ## Runtime Environment & Classpath Visibility
                      You are executing within the NetBeans Platform's OSGi-like module system. Access to other IDE modules is strictly controlled:
                      - **Public API Access:** You can access public packages of **all** the NetBeans modules listed in the pretty-printed compiler classpath below.
                      - **Implementation Access:** You have access to the **private/internal packages** of the following modules (marked as `impl` in the plugin's configuration):
                        - `org.netbeans.modules.versioning.core`
                        - `org.netbeans.modules.java.source`
                        - `org.netbeans.modules.java.source.base`
                        - `org.netbeans.modules.java.sourceui`
                        - `org.netbeans.modules.java.project`
                        - `org.netbeans.modules.java.project.ui`
                        - `org.netbeans.modules.jumpto`
                        - `org.netbeans.modules.code.analysis`
                        - `org.netbeans.modules.maven`
                        - `org.netbeans.modules.maven.embedder`
                        - `org.netbeans.modules.maven.indexer`
                      - **Plugin Dependencies:** You have full access to all packages of the plugin's own dependencies (e.g., `gemini-java-client`, `gson`, `jsoup`).
                      
                      The default compiler classpath for JVM tools is initialized at startup to the plugin's own runtime classpath.
                      
                      ## Default Compiler Classpath (Grouped View)
                      The following is a grouped view of the JARs available on your default classpath. Note that the actual classpath contains hundreds of individual JARs. 
                      If you need the full, flat list of absolute paths (e.g., for debugging or complex compilation), call `RunningJVM.getDefaultCompilerClasspath()`.

                      %s
                      """.formatted(RunningJVM.getPrettyPrintedDefaultCompilerClasspath());
        
        parts.add(Part.fromText(runtimeText));

        return parts;
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
