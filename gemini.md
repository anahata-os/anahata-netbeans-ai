# Project: anahata-netbeans-ai

## Purpose
This project is a NetBeans plugin that integrates the Gemini AI model directly into the IDE. It serves as the primary bridge between the user, the NetBeans environment, and the underlying `gemini-java-client`.

## Key Components
- **`GeminiTopComponent.java`**: The main user interface, providing the chat window within the NetBeans IDE.
- **`Installer.java`**: A NetBeans module lifecycle class responsible for setting up configurations, warming up the editor rendering pipeline, and calculating the extensive classpath needed for runtime compilation.
- **`GeminiConfigImpl.java`**: Defines the System Instructions and startup content for the AI model.
- **`NetBeansEditorKitProvider.java`**: The critical integration point that provides NetBeans' own syntax highlighting `EditorKit`s to the generic `gemini-java-client` UI, enabling native-quality code rendering in the chat.
- **`functions/spi` package**: Contains the concrete implementations of NetBeans-specific functions (`IDE.java`, `Workspace.java`, `Git.java`) that allow the model to interact with the IDE.

## Current Goals
- Improve the plugin's startup performance and context-awareness.
- Continue improving the learning capabilities of the model by creating, deleting, or evolving functions as needed.
- Enhance the interactivity between the model and the IDE.

## Todo List
- [x] **Format code snippets in received parts from the model.** (Completed by implementing `NetBeansEditorKitProvider` and `ComponentContentRenderer`).
- [ ] **Refine `proposeCodeChange` Function:** The current `IDE.proposeCodeChange` function works but uses a basic modal dialog. This should be enhanced to be non-modal and ideally show a diff view of the changes for better usability.
- [ ] **Add a "copy to clipboard" button** for code blocks and other content in the chat UI.
- [ ] **Improve scrolling performance** in the chat window, which becomes laggy with large contexts.
- [ ] **Explore more efficient file editing strategies** (e.g., diff/patch) instead of full read/write cycles.
- [ ] **Explore non-blocking function calls** to allow the user to continue interacting with the chat while long-running tasks execute in the background.
- [ ] **Investigate screenshot functionality on Ubuntu**, which is currently not working.
- [ ] **Assess Gemini API limitation** regarding local functions vs. web search and implement a robust solution (e.g., the `setTools` function).
