# Project: anahata-netbeans-ai

## Purpose
This project is a NetBeans plugin that integrates the Gemini AI model directly into the IDE. It serves as the primary bridge between the user, the NetBeans environment, and the underlying `gemini-java-client`.

## Key Components / Overview

### `uno.anahata.nb.ai` package
*   **Summary**: Contains core NetBeans plugin classes, including the installer, the main TopComponent, and configuration specific to the NetBeans environment.
*   **Key Classes**:
    *   `GeminiInstaller`: Handles module lifecycle events, classpath setup, and editor warm-up.
    *   `GeminiTopComponent`: The main user interface component for the Gemini chat within NetBeans.
    *   `NetBeansCodeBlockRenderer`: (Deprecated) Renders code blocks with NetBeans syntax highlighting.
    *   `NetBeansEditorKitProvider`: Provides NetBeans' own syntax highlighting `EditorKit`s to the `gemini-java-client` UI.
    *   `NetBeansGeminiConfig`: Concrete `GeminiConfig` implementation for the NetBeans plugin, providing host-specific instructions and functions.
    *   `ShowDefaultCompilerClassPathAction`: Action to display the compiler's classpath.

### `uno.anahata.nb.ai.deprecated` package
*   **Summary**: Contains deprecated utility classes, kept for historical context or potential future reference. These classes are no longer actively used but might contain useful logic.
*   **Key Classes**:
    *   `ClassPathUtils`: Utilities for classpath manipulation (deprecated).
    *   `ModuleInfoHelper`: Helper for module information (deprecated).
    *   `NetBeansListener`: Listens for NetBeans events (deprecated).

### `uno.anahata.nb.ai.functions.spi` package
*   **Summary**: Service Provider Interface (SPI) for NetBeans-specific AI tools, allowing the model to interact with various aspects of the IDE.
*   **Key Classes**:
    *   `Editor`: Provides functions for interacting with the NetBeans editor (e.g., opening files, getting open files).
    *   `Git`: Provides functions for interacting with Git within NetBeans (e.g., opening commit dialog).
    *   `IDE`: Provides general IDE interaction functions (e.g., getting output window content, logs, alerts).
    *   `Maven`: (Currently an empty class, intended for future Maven-related functions).
    *   `Output`: Provides functions for interacting with the NetBeans Output Window.
    *   `Projects`: Provides functions for managing and querying open NetBeans projects.
    *   `TopComponents`: Provides functions for listing and interacting with NetBeans TopComponents.
    *   `Workspace`: Provides functions for getting an overview of the entire NetBeans workspace.

### `uno.anahata.nb.ai.mime` package
*   **Summary**: Utility classes for handling MIME types and language support within the NetBeans environment.
*   **Key Classes**:
    *   `LanguageMimeResolver`: Resolves MIME types for given language names.
    *   `LanguageSupport`: Provides information about supported languages in NetBeans.

## Current Goals
- Improve the plugin's startup performance and context-awareness.
- Continue improving the learning capabilities of the model by creating, deleting, or evolving functions as needed.
- Enhance the interactivity between the model and the IDE.

## Todo List
- [ ] **Refine `proposeCodeChange` Function**: The current `IDE.proposeCodeChange` function works but uses a basic modal dialog. This should be enhanced to be non-modal and ideally show a diff view of the changes for better usability.
- [ ] **Add a "copy to clipboard" button** for code blocks and other content in the chat UI.
- [ ] **Improve scrolling performance** in the chat window, which becomes laggy with large contexts.
- [ ] **Explore more efficient file editing strategies** (e.g., diff/patch) instead of full read/write cycles, potentially leveraging NetBeans' own file system APIs.
- [ ] **Explore non-blocking function calls** to allow the user to continue interacting with the chat while long-running tasks execute in the background.
- [ ] **Investigate screenshot functionality on Ubuntu**, which is currently not working.
- [ ] **Assess Gemini API limitation** regarding local functions vs. web search and implement a robust solution (e.g., the `setTools` function).
