# Project: anahata-netbeans-ai - Anahata NetBeans AI Assistant Plugin

## 1. Purpose
This project is the flagship host application for the `gemini-java-client`. It integrates the Anahata AI Assistant directly into the Apache NetBeans IDE, providing a deeply context-aware development partner.

This plugin's main responsibilities are:
-   Providing a `TopComponent` (`AnahataTopComponent`) to host the chat panel.
-   Implementing a NetBeans-specific configuration (`NetBeansChatConfig`) that injects live IDE state into the AI's context on every request.
-   Supplying a suite of AI tools (`tools` package) that allow the model to "see" and interact programmatically with the NetBeans IDE.

## 2. Key Components & Packages

### `uno.anahata.nb.ai`
*   **Summary**: Contains the core NetBeans integration classes.
*   **Key Classes**:
    *   `AnahataTopComponent`: The main window for the AI assistant, responsible for managing the UI lifecycle.
    *   `NetBeansChatConfig`: A concrete `ChatConfig` implementation that provides host-specific system instructions and registers all the NetBeans-specific AI tool classes.
    *   `AnahataInstaller`: A standard NetBeans module installer class that handles setup tasks.

### `uno.anahata.nb.ai.tools`
*   **Summary**: The heart of the plugin's unique capabilities. These classes grant the AI model the ability to "see" and interact with the IDE.
*   **Key Classes**:
    *   `Projects`: Provides tools for listing and querying open NetBeans projects.
    *   `Editor`: Provides tools for interacting with the code editor.
    *   `IDE`: Provides general IDE interaction tools, most importantly `getAllIDEAlerts` which allows the AI to see compilation errors and warnings.
    *   `MavenTools`: A consolidated "super-tool" for all Maven-related operations, including searching, modifying the POM, and running goals.
    *   `Coding`: Provides the `proposeChange` tool, which shows a diff view for user approval.

## 3. Coding Principles

1.  **Javadoc Integrity:** As an open-source Java library, comprehensive documentation is paramount.
    *   Existing Javadoc, comments, and blank lines **must never be removed**.
    *   New public classes and methods **must have Javadoc**.
    *   Changes should be made by patching, not regenerating, to preserve the original structure and comments.
2.  **Dependency Management Workflow:** Adhere to this strict workflow when adding new dependencies to ensure project stability and maintainability:
    a. **Find Latest Version:** Use `MavenTools.searchMavenIndex` to identify the latest stable version of the desired artifact.
    b. **Check for Conflicts:** Before adding, use `MavenTools.getResolvedDependencies` to inspect the project's current transitive dependency tree. Check for existing versions of the artifact or potential conflicts with other libraries.
    c. **Add Dependency:** Use the `MavenTools.addDependency` tool to safely modify the `pom.xml`.
    d. **Download Sources:** Immediately after adding the dependency, use `MavenTools.downloadProjectDependencies` to download the `sources` and `javadoc` for the new artifact. This is crucial for future development and debugging.

## 4. Managing AI Tools

The set of Java classes available to the AI as tools is determined at startup by the `getToolClasses()` method in the `NetBeansChatConfig` class.

-   **To Register a New Tool:** Add the `.class` literal of your new tool class to the list returned by `getToolClasses()`.
    ```java
    @Override
    public List<Class<?>> getToolClasses() {
        List<Class<?>> ret = super.getToolClasses();
        ret.add(MyNewTool.class); // Add your new tool here
        return ret;
    }
    ```
-   **To Unregister a Tool:** Simply remove or comment out the line that adds the tool's `.class` from the list.

This mechanism provides a simple and centralized way to control which Java methods the AI is allowed to see and execute.

## 5. Competitive Advantage & V1 Launch Strategy

A competitive analysis has shown that the Anahata AI Assistant's current feature set is superior to existing alternatives for NetBeans. Our key differentiator is the **deep, programmatic IDE integration**.

-   **Anahata can see compilation errors and warnings in real-time.**
-   **Anahata can invoke high-level IDE actions like 'build' and 'run'.**

Therefore, the strategy is to proceed with a **V1 Launch** with the current feature set and postpone the larger "mega-refactor" (decoupling UI, multi-model support) for a V2 release.

## 6. V1 Launch Goals (Immediate Focus)

-   [ ] **AnahataNodeFactory doesnt refresh the folder and causes a memory leak on nb:** 
-   [ ] **Node Decoration (High Priority):** The file decoration in the NetBeans project tree for files that are "in-context" is currently non-functional. This is a critical pre-launch feature. *Next Step: Research NetBeans Git module source for correct implementation pattern.*
-   [ ] **Local History:** figure out how to write a local history user and message so it is seen in the nb local history.
-   [ ] **Tab NickName and Color Coding based on status / session id:** check how to change the colors on the tab
-   [ ] **Plugin Portal:** Package the plugin and submit it to the Apache NetBeans Plugin Portal.
-   [ ] **Performance:** Investigate and improve the initial startup time of the `AnahataTopComponent`.
-   [In Progress] **Task N: Research Refactoring APIs:**
    -   **Status:** In Progress.
    -   **Description:** Before refactoring the `StatusListener`, we must research the NetBeans programmatic refactoring APIs.
    -   **Sub-Tasks:**
        -   [x] **Explore API:** Initial exploration of the `org.netbeans.modules.refactoring.api` package has begun.
        -   [ ] Investigate key classes and interfaces to discover available tools (rename, find usages, change parameters, etc.).
        -   [ ] Analyze the compatibility of these source-level tools with Lombok's annotation processing.
        -   [ ] Document findings in `netbeans.md` notes.
-   [ ] **UI Polish:**
    -   [ ] Implement the fix to display the `explanation` text in the `Coding.proposeChange` modal diff dialog (e.g., by wrapping it in a `TitledBorder`).

### Future Tool Enhancements
-   [ ] **Parallelize Maven Artifact Downloads:** Review the `downloadProjectDependencies` tool to execute downloads in parallel to improve performance.

## 7. V2 Mega-Refactor Plan (Future Focus)

The V2 plan remains to split the `gemini-java-client` into a modular architecture to support multiple AI models and UI frameworks. A key architectural goal is:
-   **Active Workspace Model:** Transition `LocalFiles.readFile` to an "Active Workspace" model where the file content is added to a central list and injected into the user prompt. This will eliminate the current context bloat where `writeFile` keeps the file content twice in the context (FunctionCall and FunctionResponse).
-   **Code Cleanup:** Remove obsolete singleton-based classes like `ContextFiles` which are incompatible with the multi-instance architecture.

## 8. Very Important Notes

**Dont "clean" the project**, because it would delete all the runtime jars and the classloader would start trhwoing exceptions.

**NetBeansProjectJVM and RunningJVM**

- When testing code in this project via NetBeansProject.compileAndExecuteJava, **do not include compileAndExecuteDependencies**
- If you are working on a tool and testing the returned value, serialize it to json using the methods in GsonUtils so i can see the output.
- If compileOnSave is on, new classes will not show becaues compileOnSave doesnt get triggered for new classess, or maybe it doesnt get triggered because we are writing them directly to disk and nb is not watching them so that would need an "install" (never a clean)
- Tools only get registered when the chat starts up, so even if compileOnSave is on, you have to test them via static method from the jvm.
- The classloader and the compiler classpath only get populated with all the plugins classess and resolved dependencies resolved dependencies at startup, if you add a depdency you would need to reload the plugin.

**How to reload the plugin**

1.  **Build Dependencies First:** If you have made any changes to the `gemini-java-client` project, you **must** build it first (`mvn install`). This project depends on it, and skipping this step will cause compilation errors.
2.  **Install Plugin:** Run the `install` Maven goal on this project (no `clean`).
3.  **Check for Success:** Verify that the build was successful in the output window.
4.  **Reload:** If the install succeeded, invoke the `nbmreload` Project Action. The IDE will restart, and your changes will be live.

## 9. Current Task Board (As of 2025-11-16)

This section tracks our active work items to ensure continuity across sessions.

-   **[Done] Task A: Implement Session Manager UI & Rich Status Display:**
    -   **Status:** Done.
    -   **Description:** Created the new `AnahataInstancesTopComponent` to act as a session manager. It displays a live list of all active chat windows, shows their real-time status with color-coding, and allows for quick navigation by double-clicking. The main chat tabs also now dynamically update their color and tooltip to reflect the current status.
    -   **Next Step:** Completed.

-   **[Done] Task J: Design and Refactor Maven Tools:**
    -   **Status:** Done.
    -   **Description:** The fragmented Maven tools (`Maven`, `MavenPom`, `MavenSearch`) have been consolidated into a single, robust `MavenTools` class. The old classes have been deprecated and de-registered from the `NetBeansChatConfig`.
    -   **Deprecation Note (2025-11-16):** The original classes (`Maven.java`, `MavenPom.java`, `MavenSearch.java`) have been marked with `@Deprecated` but their method bodies remain intact for a transitional period. They are scheduled for complete removal once the new `MavenTools` implementation is fully verified in production.
    -   **Next Step:** Completed.

-   **[On Hold] Task I: Test Schema Generation for `Tree` class:**
    -   **Status:** On Hold.
    -   **Description:** The investigation into serializing `com.google.genai.types` objects for debugging is complete. The findings have been documented in `jsonSchema.md`. This task is paused to focus on the higher-priority Maven tool redesign.
    -   **Next Step:** Awaiting completion of Maven tool refactoring.

-   **[High Priority] Task B: Implement Granular Tool Status:**
    -   **Status:** To Do.
    -   **Description:** Enhance the status reporting to show which specific tool is executing (e.g., "Tool Execution (Maven.runGoals)...").
    -   **Next Step:** Add `setExecutingToolName(String)` to `StatusManager` and integrate it with `ToolManager` and the UI.
