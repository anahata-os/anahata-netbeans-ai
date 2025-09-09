# Project: anahata-netbeans-ai

## Purpose
This project is a NetBeans plugin that integrates the Gemini AI model directly into the IDE. It serves as the primary bridge between the user, the NetBeans environment, and the underlying `gemini-java-client`.

## Key Components
- **`GeminiTopComponent.java`**: The main user interface, providing the chat window within the NetBeans IDE.
- **`Installer.java`**: A NetBeans module lifecycle class responsible for setting up necessary configurations, classpaths, and listeners when the plugin is loaded.
- **`GeminiConfigProviderImpl.java`**: Defines the System Instructions for the AI model and a startup manual. 
This is a critical file that configures the configurable model parameters for the conversation ("startup content" for the opening message
of the conversation, "per request manual" passed as system instructions along with dynamic environment settings, 
- **`NetBeansListener.java`**: Listens for IDE events like file changes or window focus, allowing for better contextual awareness. Currently not being used.
- **`ShowDefaultCompilerClassPathAction.java`**: Builds the compilers classpath at bootstrap. Using nb ModuleInfo apis, 
it instrospects this module's ModuleInfo and ClassLoader and collects the absolute file path of all the jars that 
this modules sees as runtime (the plugin jar artifact, the jars of any normal java dependencies (in the ext dir) 
and all the jars of all the nbm dependencies (recursively). 

## Current Goals
Improve the plugins startup performance and context-awareness so the model can assist the user more efficiently 
from the moment the IDE starts.

Continue trying new actions to see if there are any more nb modules or java dependencies that ned be declared in the pom 

Continue improving the learning capabilities of the model ensuring the model can "create", "delete" or "evolve" gems 
as needed
