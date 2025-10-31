# Project: anahata-netbeans-ai - Gemini NetBeans Plugin

## 1. Purpose
This project is a NetBeans plugin that integrates the Gemini AI model directly into the IDE. It serves as the primary bridge between the user, the NetBeans environment, and the underlying `gemini-java-client` library, which provides the core chat UI and API communication logic.

This plugin's main responsibilities are:
-   Providing a `TopComponent` to host the Gemini chat panel.
-   Implementing a NetBeans-specific configuration (`NetBeansGeminiConfig`).
-   Supplying a suite of AI tools (`functions.spi`) that allow the model to interact with the NetBeans IDE itself.

## 2. Key Components & Packages

### `uno.anahata.nb.ai`
*   **Summary**: Contains the core NetBeans integration classes.
*   **Key Classes**:
    *   `GeminiTopComponent`: The main window (`TopComponent`) for the Gemini chat UI within NetBeans.
    *   `NetBeansGeminiConfig`: A concrete `GeminiConfig` implementation that provides host-specific instructions and registers all the NetBeans-specific AI tool classes.
    *   `NetBeansEditorKitProvider`: A crucial class that implements the `EditorKitProvider` interface from the client library. It bridges NetBeans' powerful syntax highlighting capabilities to the `CodeBlockRenderer` in the `gemini-java-client` UI.
    *   `GeminiInstaller`: A standard NetBeans module installer class that handles setup tasks.

### `uno.anahata.nb.ai.functions.spi`
*   **Summary**: The Service Provider Interface (SPI) for all NetBeans-specific AI tools. These classes grant the AI model the ability to "see" and interact with the IDE.
*   **Key Classes**:
    *   `Projects`: Provides tools for listing and querying open NetBeans projects (`getOpenProjects`, `getOverview`).
    *   `Editor`: Provides tools for interacting with the code editor (`openFile`, `getOpenFiles`).
    *   `IDE`: Provides general IDE interaction tools (`getAllIDEAlerts`, `getLogs`).
    *   `Workspace`: Provides tools for getting an overview of the entire workspace.
    *   `Git`: Provides tools for basic Git integration (`openCommitDialog`).
    *   And others like `Maven`, `Output`, `TopComponents`.

### `uno.anahata.nb.ai.deprecated`
*   **Summary**: Contains deprecated utility classes that are no longer in active use but are kept for historical context.

## 3. Relationship with `gemini-java-client`
This project is a **host application**. It includes `gemini-java-client` as a Maven dependency.

-   `gemini-java-client` provides the entire user interface (`GeminiPanel`), the rendering pipeline, the `FunctionManager2`, and the core logic for communicating with the Gemini API.
-   `anahata-netbeans-ai` **launches** this UI and **injects** the NetBeans-specific tools and configurations into it, enabling the AI to perform IDE-aware tasks.

## 4. Current Goals & Todo List
-   **Improve Startup Performance**: Investigate ways to make the initial loading of the plugin and chat faster.
-   **Enhance Interactivity**: Continue to evolve the available tools to allow for more complex and seamless interactions between the AI and the IDE.
-   **Implement Diff/Patch Editing**: Act on the `diff-plan.md` to create a `proposeCodeChange` tool that uses a diff/patch mechanism for efficient and reviewable code modifications.
-   **Add UI Conveniences**:
    -   [ ] Add a "copy to clipboard" button for code blocks.
    -   [ ] Explore non-blocking function calls for long-running tasks.
-   **Bug Fixes**:
    -   [ ] Investigate and fix screenshot functionality on Linux environments.

## TODO - 2025-10-31

-   **`proposeChange` Dialog Context:** The modal diff dialog for `Coding.proposeChange` blocks the main UI, preventing the user from seeing the conversational context (my rationale) for the change. The `explanation` parameter should be displayed prominently within the dialog itself.
-   **EDT Responsiveness:** Investigate and fix performance issues where the Swing Event Dispatch Thread (EDT) becomes unresponsive for long periods during model responses. This likely involves moving more processing off the EDT.
-   **In-Context File Decoration:** The file decoration in the NetBeans project tree for files that are "in-context" is not working. This needs to be diagnosed and fixed to provide better visual feedback.
