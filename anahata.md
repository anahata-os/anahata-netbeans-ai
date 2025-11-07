# Project: anahata-netbeans-ai - Anahata AI Assistant Plugin

## 1. Purpose
This project is the flagship host application for the `gemini-java-client`. It integrates the Anahata AI Assistant directly into the Apache NetBeans IDE, providing a deeply context-aware development partner.

This plugin's main responsibilities are:
-   Providing a `TopComponent` (`AnahataTopComponent`) to host the chat panel.
-   Implementing a NetBeans-specific configuration (`NetBeansGeminiConfig`) that injects live IDE state into the AI's context on every request.
-   Supplying a suite of AI tools (`tools` package) that allow the model to interact programmatically with the NetBeans IDE.

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

## 3. Competitive Advantage & V1 Launch Strategy

A competitive analysis has shown that the Anahata AI Assistant's current feature set is superior to existing alternatives for NetBeans. Our key differentiator is the **deep, programmatic IDE integration**.

-   **Anahata can see compilation errors and warnings in real-time.**
-   **Anahata can invoke high-level IDE actions like 'build' and 'run'.**

Therefore, the strategy is to proceed with a **V1 Launch** with the current feature set and postpone the larger "mega-refactor" (decoupling UI, multi-model support) for a V2 release.

## V1 Launch Goals (Immediate Focus)

-   **Node Decoration (High Priority):** The file decoration in the NetBeans project tree for files that are "in-context" is currently non-functional. This is a critical pre-launch feature. The next step is to research the NetBeans Git module's source code to find the correct implementation pattern for adding an Anahata badge and updating the node's tooltip in the Projects View.
-   **Plugin Portal:** Package the plugin and submit it to the Apache NetBeans Plugin Portal.
-   **Performance:** Investigate and improve the initial startup time of the `AnahataTopComponent`.
-   **UI Polish:**
    -   [ ] Implement the fix to display the `explanation` text in the `Coding.proposeChange` modal diff dialog (e.g., by wrapping it in a `TitledBorder`).

## V2 Mega-Refactor Plan (Future Focus)

The V2 plan remains to split the `gemini-java-client` into a modular architecture to support multiple AI models and UI frameworks. A key architectural goal is:
-   **Active Workspace Model:** Transition `LocalFiles.readFile` to an "Active Workspace" model where the file content is added to a central list and injected into the user prompt. This will eliminate the current context bloat where `writeFile` keeps the file content twice in the context (FunctionCall and FunctionResponse).
-   **Code Cleanup:** Remove obsolete singleton-based classes like `ContextFiles` which are incompatible with the multi-instance architecture.
