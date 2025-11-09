[![Sponsor anahata-os](https://img.shields.io/badge/Sponsor-%E2%9D%A4-%23db61a2.svg?logo=GitHub)](https://github.com/sponsors/anahata-os)

# 🚀 Anahata AI Assistant for NetBeans: Code from the Heart

**Elevate your coding experience with a true AI partner that understands your workflow.**

Anahata is an unprecedented, deeply integrated AI assistant for the Apache NetBeans IDE, powered by Google's Gemini models. It's more than a chatbot—it's a harmonious extension of your creative process, designed to bring flow and intelligence directly to your work.

**Visit our new website: [anahata.uno](https://anahata.uno)**

## ✨ Why Anahata is the Future of IDE Assistance

Traditional AI assistants are disconnected. Anahata is built from the ground up to be a native part of your IDE, giving it unparalleled context and capability.

### 1. Deep, Real-Time Context Awareness (The Architectural Advantage)

Anahata doesn't guess. On every request, it receives a live, structured data payload that includes:

-   **Live Diagnostics:** Real-time visibility into all **IDE errors and warnings** across your open projects. Ask it to fix a compilation error, and it knows exactly where to look.
-   **Full Project Structure:** A complete overview of all open projects, their file structures, and the status of files in the conversation context.
-   **Editor State:** A list of all files currently open in the editor.

### 2. Programmatic IDE Control (NetBeans-Specific Tools)

Anahata can execute high-level IDE actions and perform deep code analysis with a simple natural language command:

| Feature | Tooling | Capability |
| :--- | :--- | :--- |
| **Code Introspection** | `JavaIntrospection`, `JavaSources`, `JavaDocs` | Inspect Java types, list members, and retrieve **paginated, filterable, and truncated** source code and Javadoc for any class or method. |
| **IDE Interaction** | `IDE`, `Output` | Read the main IDE log and interact with the Output Window, with powerful support for **pagination, regex filtering, and line truncation** to manage large outputs. |
| **Dependency Management** | `Maven` | Trigger Maven goals (`clean`, `install`), download missing dependency sources, and resolve Javadoc URLs. |
| **Project Lifecycle** | `Projects` | Open/close projects, invoke high-level actions (`build`, `run`), and query project structure. |
| **Code Modification** | `Coding` | Use the `proposeChange` tool to receive code patches via a **NetBeans modal diff dialog** for secure, explicit user approval. |
| **Live Workspace** | `ScreenCapture`, `TopComponents` | The AI can "see" the IDE by taking screenshots of all open JFrames and listing all open IDE components (windows, tabs). |
| **Runtime Execution** | `NetBeansProjectJVM` | Compile and execute arbitrary Java code directly within the IDE's running JVM, enabling hot-reload testing and complex runtime tasks. |
| **System Interaction** | `LocalShell`, `LocalFiles` | Run native shell commands and perform context-aware file operations (read, write, delete). |
| **Editor Control** | `Editor` | Open files in the NetBeans editor and scroll to specific line numbers. |

### 3. Advanced Text Processing & Safety

To ensure a smooth and efficient workflow, Anahata's core tools have been refactored with a powerful, centralized text processing engine. This provides:

-   **Pagination & Filtering:** Retrieve precise slices of data from large files (logs, source code, Javadocs) using line numbers and regex (`grep`) patterns.
-   **Line Truncation:** Automatically truncate excessively long lines to prevent context window overflow and keep the conversation focused.
-   **Rich Metadata:** Tool responses now include valuable context, such as the total number of lines and the number of matching lines, allowing the AI to make smarter follow-up decisions.

This makes interacting with large codebases and log files safer, faster, and more intelligent.

### 4. Core Gemini Client Features (Efficiency & Transparency)

Anahata is built on the `gemini-java-client`, inheriting powerful features for managing the conversation and token budget:

-   **Intelligent Context Management:** Automatic, dependency-aware pruning of old, ephemeral, or stale tool calls to maximize the context window and reduce API costs.
-   **Context Heatmap:** A visual UI component that breaks down the entire conversation context by message, part type, and token size, giving the user full transparency.
-   **Dynamic System Instructions:** Real-time injection of environment data (System Properties, Environment Variables, Project Status) into the system prompt for superior relevance.
-   **Inline Syntax Highlighting:** Uses NetBeans' native editor kits to provide accurate syntax highlighting for code blocks within the chat window.
-   **Session Persistence:** Saves and loads the entire chat history, including all tool results and dependencies, for instant session resume.

---
### **Powered by the Gemini Java Client**

The Anahata AI Assistant is the premier showcase for the [**`gemini-java-client`**](https://github.com/anahata-os/gemini-java-client), our powerful, enterprise-ready library for integrating Google Gemini into any Java application. 

The client features a robust tool-calling framework, built-in Swing UI components, and a flexible configuration system. It is available under a dual-license model (AGPLv3 for open-source, commercial for proprietary use).
---

## Getting Started

*(Instructions to be added once the plugin is packaged for distribution).*

## Licensing: Open Core Model

This project is available under a dual-license model to accommodate both open-source and commercial needs.

-   **Open Source:** For use in open-source projects, the software is licensed under the **GNU Affero General Public License v3 (AGPLv3)**. See the [LICENSE](LICENSE) file for the full license text.

-   **Commercial Use:** For use in proprietary, closed-source applications, a **commercial license is required**. Please see the [COMMERCIAL-LICENSE.md](COMMERCIAL-LICENSE.md) file for more information on how to obtain a commercial license.