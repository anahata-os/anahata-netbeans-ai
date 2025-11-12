# Project: anahata-netbeans-ai - Anahata NetBeans AI Assistant Plugin

## 1. Purpose
This project is the flagship host application for the `gemini-java-client`. It integrates the Anahata AI Assistant directly into the Apache NetBeans IDE, providing a deeply context-aware development partner.

This plugin's main responsibilities are:
-   Providing a `TopComponent` (`AnahataTopComponent`) to host the chat panel.
-   Implementing a NetBeans-specific configuration (`NetBeansGeminiConfig`) that injects live IDE state into the AI's context on every request.
-   Supplying a suite of AI tools (`tools` package) that allow the model to "see" and interact programmatically with the NetBeans IDE.

## 2. Key Components & Packages

### `uno.anahata.nb.ai`
*   **Summary**: Contains the core NetBeans integration classes.
*   **Key Classes**:
    *   `AnahataTopComponent`: The main window for the AI assistant, responsible for managing the UI lifecycle.
    *   `NetBeansGeminiConfig`: A concrete `GeminiConfig` implementation that provides host-specific system instructions and registers all the NetBeans-specific AI tool classes.
    *   `AnahataInstaller`: A standard NetBeans module installer class that handles setup tasks.

### `uno.anahata.nb.ai.tools`
*   **Summary**: The heart of the plugin's unique capabilities. These classes grant the AI model the ability to "see" and interact with the IDE.
*   **Key Classes**:
    *   `Projects`: Provides tools for listing and querying open NetBeans projects.
    *   `Editor`: Provides tools for interacting with the code editor.
    *   `IDE`: Provides general IDE interaction tools, most importantly `getAllIDEAlerts` which allows the AI to see compilation errors and warnings.
    *   `Maven`: Provides tools for invoking Maven actions.
    *   `Coding`: Provides the `proposeChange` tool, which shows a diff view for user approval.

## 3. Coding Principles

1.  **Javadoc Integrity:** As an open-source Java library, comprehensive documentation is paramount.
    *   Existing Javadoc, comments, and blank lines **must never be removed**.
    *   New public classes and methods **must have Javadoc**.
    *   Changes should be made by patching, not regenerating, to preserve the original structure and comments.

## 4. Competitive Advantage & V1 Launch Strategy

A competitive analysis has shown that the Anahata AI Assistant's current feature set is superior to existing alternatives for NetBeans. Our key differentiator is the **deep, programmatic IDE integration**.

-   **Anahata can see compilation errors and warnings in real-time.**
-   **Anahata can invoke high-level IDE actions like 'build' and 'run'.**

Therefore, the strategy is to proceed with a **V1 Launch** with the current feature set and postpone the larger "mega-refactor" (decoupling UI, multi-model support) for a V2 release.

## V1 Launch Goals (Immediate Focus)

-   [ ] **AnahataNodeFactory doesnt refresh the folder and causes a memory leak on nb:** 
-   [ ] **Node Decoration (High Priority):** The file decoration in the NetBeans project tree for files that are "in-context" is currently non-functional. This is a critical pre-launch feature. *Next Step: Research NetBeans Git module source for correct implementation pattern.*
-   [ ] **Local History:** figure out how to write a local history user and message so it is seen in the nb local history.
-   [ ] **Tab NickName and Color Coding based on status / session id:** check how to change the colors on the tab
-   [ ] **Plugin Portal:** Package the plugin and submit it to the Apache NetBeans Plugin Portal.
-   [ ] **Performance:** Investigate and improve the initial startup time of the `AnahataTopComponent`.
-   [ ] **Java:** see if we can add "find usages" "call hierarchy" and "refactor/rename"
-   [ ] **UI Polish:**
    -   [ ] Implement the fix to display the `explanation` text in the `Coding.proposeChange` modal diff dialog (e.g., by wrapping it in a `TitledBorder`).

### Future Tool Enhancements
-   [ ] **Parallelize Maven Artifact Downloads:** Review the `downloadProjectDependencies` tool to execute downloads in parallel to improve performance.
-   [ ] **Create `addDependency` Super-Tool:** Discuss and potentially create a high-level tool that orchestrates the entire process of adding a new dependency: verifying its existence, updating the `pom.xml`, and downloading the binary, sources, and Javadoc artifacts.

## V2 Mega-Refactor Plan (Future Focus)

The V2 plan remains to split the `gemini-java-client` into a modular architecture to support multiple AI models and UI frameworks. A key architectural goal is:
-   **Active Workspace Model:** Transition `LocalFiles.readFile` to an "Active Workspace" model where the file content is added to a central list and injected into the user prompt. This will eliminate the current context bloat where `writeFile` keeps the file content twice in the context (FunctionCall and FunctionResponse).
-   **Code Cleanup:** Remove obsolete singleton-based classes like `ContextFiles` which are incompatible with the multi-instance architecture.

## Very Important Notes

**Dont "clean" the project**, because it would delete all the runtime jars and the classloader would start trhwoing exceptions.

**NetBeansProjectJVM and RunningJVM**

- When testing code in this project via NetBeansProject.compileAndExecuteJava, **do not include compileAndExecuteDependencies**
- If you are working on a tool and testing the returned value, serialize it to json using the methods in GsonUtils so i can see the output.
- If compileOnSave is on, new classes will not show becaues compileOnSave doesnt get triggered for new classess, or maybe it doesnt get triggered because we are writing them directly to disk and nb is not watching them so that would need an "install" (never a clean)
- Tools only get registered when the chat starts up, so even if compileOnSave is on, you have to test them via static method from the jvm.
- The classloader and the compiler classpath only get populated with all the plugins classess and resolved dependencies resolved dependencies at startup, if you add a depdency you would need to reload the plugin.

**How to reload the plugin**

1 runGoal "install" (no cleaning) 
2 If install succeeds (and you have to check), on the next turn (once you have seen the install build SUCEEDED), invoke the Project Action "nbmreload". 
You need to check that installed succeed first. If the reload action succeeds, the current chat window will be close and we will be chatting on a new instance of AnahataTopComponent with a different topComponentId but will restore the chat from the autobackup associated to this chat.
