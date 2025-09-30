# Anahata NetBeans Plugin - The only plugin you need!

An unprecedented, deeply integrated AI assistant for the Apache NetBeans IDE, powered by Google's Gemini models.

This plugin is not just a chatbot in a side panel. It's a true AI assistant with direct, programmatic access to the IDE's core functionalities. It can read and understand your open projects, check for IDE errors, interact with the editor, and even invoke project actions like `build` and `run`.

This deep integration is made possible by its underlying framework, the [gemini-java-client](https://github.com/pablo-anahata/gemini-java-client), which provides the core logic for connecting to the Gemini API and managing a powerful, extensible local tool system.

## Features

- **Live IDE Context Awareness:** The assistant has real-time knowledge of your workspace. On every request, it knows which projects are open, which files are in the editor, and what errors or warnings the IDE has detected.
- **Direct Project Interaction:** Ask the assistant to read a file, and it will. Ask it to build a project, and it will invoke the Maven build action.
- **Semantic Code Understanding:** Because it can read all your source files, the assistant builds a comprehensive understanding of your project's architecture, allowing for more insightful and accurate assistance.
- **Extensible Toolset:** The plugin's capabilities are defined by a set of "Tools" that map directly to NetBeans APIs. This makes it easy to extend the assistant with new functionalities.
- **Full Chat Interface:** Includes support for file attachments, conversation history, and syntax highlighting for generated code snippets.

## How It Works: The Architectural Advantage

The key to this plugin's power is the `NetBeansGeminiConfig` class. Unlike traditional AI assistants that receive only a text prompt, this plugin's configuration class dynamically constructs a rich, detailed set of system instructions for the AI on *every single turn* of the conversation.

This payload includes:
1.  A complete overview of all open projects, including their file structure (`Projects.getOverview`).
2.  A list of all files currently open in the editor (`Editor.getOpenFiles`).
3.  A summary of all current IDE errors and warnings (`IDE.getAllIDEAlerts`).

By providing this live, structured data directly to the model, the assistant can reason about the state of your IDE and take meaningful, context-aware actions using its suite of tools.

### Core Components

- **`GeminiTopComponent`**: The main UI window for the assistant, registered to appear in the IDE's "output" area.
- **`NetBeansGeminiConfig`**: The central configuration class that bridges the generic `gemini-java-client` with the NetBeans environment. It provides the host-specific system instructions and registers the NetBeans-specific tools.
- **`functions.spi` package**: Contains all the tool implementations that interact directly with NetBeans APIs:
    - `Projects`: Manages project-level information and actions.
    - `Editor`: Interacts with the text editor.
    - `IDE`: Provides access to general IDE features like the output window and diagnostics.
    - `Workspace`: Offers a high-level overview of the entire IDE workspace.
    - `Git`: Integrates with the IDE's Git functionality.

## Getting Started

*(Instructions to be added once the plugin is packaged for distribution).*

## License

This project is licensed under the Apache License 2.0. See the [LICENSE](LICENSE) file for details.
