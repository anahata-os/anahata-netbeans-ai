# Project: anahata-netbeans-ai

## Purpose
This project is a NetBeans plugin that integrates the Gemini AI model directly into the IDE. It serves as the primary bridge between the user, the NetBeans environment, and the underlying `gemini-java-client`.

## Key Components
- **`GeminiTopComponent.java`**: The main user interface, providing the chat window within the NetBeans IDE.
- **`Installer.java`**: A NetBeans module lifecycle class responsible for setting up necessary configurations, classpaths, and listeners when the plugin is loaded.
- **`GeminiConfigProviderImpl.java`**: Defines the System Instructions for the AI model. This is a critical file that tells me who I am, what my capabilities are, and how I should behave.
- **`NetBeansListener.java`**: Listens for IDE events like file changes or window focus, allowing for better contextual awareness. Currently not being used.
- **`ClassPathUtils.java` & `ModuleInfoHelper.java`**: These are vital utilities for dynamically inspecting the complex, modular NetBeans environment to construct the correct classpath for runtime code execution.

## Current Goal
To improve the plugins startup performance and context-awareness so the model can assist the user more efficiently from the moment the IDE starts.
