# Project: anahata-netbeans-ai - Anahata NetBeans AI Assistant Plugin

This document provides the essential, high-level overview of the `anahata-netbeans-ai` project. It is intended to be the stable, "always-in-context" guide to the plugin's purpose, architecture, and core principles.

## 1. Purpose
This project is the flagship host application for the `gemini-java-client`. It integrates the Anahata AI Assistant directly into the Apache NetBeans IDE, providing a deeply context-aware development partner.

This plugin's main responsibilities are:
-   Providing a `TopComponent` (`AnahataTopComponent`) to host the chat panel.
-   Implementing a NetBeans-specific configuration (`NetBeansChatConfig`) that injects live IDE state into the AI's context on every request.
-   Supplying a suite of AI tools (`tools` package) that allow the model to "see" and interact programmatically with the NetBeans IDE.

## 2. Architectural Overview

The plugin is designed with a clear separation of concerns, organized into the following core packages:

-   `uno.anahata.ai.nb`: The central hub that bridges the AI framework with the IDE. It contains the main UI windows (`AnahataTopComponent`, `LiveSessionsTopComponent`) and the `NetBeansChatConfig` which registers all IDE-specific components.

-   `uno.anahata.ai.nb.context`: The sensory system of the AI. This package contains `ContextProvider` implementations that feed the AI a live, just-in-time stream of information about the IDE's state, such as open projects, editor tabs, and compilation errors.

-   `uno.anahata.ai.nb.tools`: The heart of the plugin's unique capabilities. These are the AI-callable tools that allow the model to programmatically interact with the IDE, including tools for code analysis, refactoring, Maven builds, and project management.

-   `uno.anahata.ai.nb.model`: The data backbone of the plugin. This package and its sub-packages define all the DTOs and POJOs that structure the information flowing between the AI, tools, and UI.

-   `uno.anahata.ai.nb.nodes`: Provides visual integration with the NetBeans project tree. **(Note: This package is currently non-functional and contains known bugs).**

-   `uno.anahata.ai.nb.util`: A set of utility classes that act as a bridge to complex NetBeans Platform APIs, handling low-level tasks like classpath construction and source file resolution.

-   `uno.anahata.ai.nb.mime`: A sophisticated bridge to the NetBeans editor infrastructure, allowing the chat panel to leverage the IDE's native syntax highlighters for code blocks.

-   `uno.anahata.ai.nb.vcs`: Contains experimental, non-functional placeholders for a future native Version Control System to manage the AI's context.

## 3. Design Philosophy & Coding Principles

### Domain-Driven Design (DDD)
We embrace a Domain-Driven Design approach. Our model objects, defined in the `uno.anahata.ai.nb.model` package, are not just passive data carriers. Where appropriate, they contain business logic and helper methods related to the domain they represent. This creates a richer, more expressive, and more maintainable codebase, even though these objects are sometimes serialized for transport.

### Javadoc Integrity
As an open-source Java library, comprehensive documentation is paramount.
-   Existing Javadoc, comments, and blank lines **must never be removed**.
-   New public classes and methods **must have Javadoc**.
-   Changes should be made by patching, not regenerating, to preserve the original structure and comments.

## 4. Managing AI Tools
The set of available AI tools is defined in the `getToolClasses()` method in `NetBeansChatConfig`. To register a new tool, add its `.class` literal to the list. To unregister one, remove it.

## 5. Very Important Notes

-   **Do not "clean" the project**, as this will delete runtime JARs and cause classloader exceptions.
-   When using `NetBeansProjectJVM.compileAndExecuteInProject`, **do not include compileAndExecuteDependencies** unless absolutely necessary, as it can cause `LinkageError` exceptions with NetBeans Platform APIs.
-   To test changes to tools or add new dependencies, you **must reload the plugin** using the `install` Maven goal followed by the `nbmreload` Project Action.

## 6. Experimental Features

### Native VCS for Context Management
-   **Status:** Experimental
-   **Description:** A separate, experimental NetBeans project is being used to develop a custom, native Version Control System (VCS). The goal is to explore using a native VCS to track changes to files in the AI's context, providing a more robust alternative to the current `StatefulResource` tracking. The classes in the `uno.anahata.ai.nb.vcs` package are placeholders for this future integration.
