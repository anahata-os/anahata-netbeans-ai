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

## Todo List
- [ ] Add a "copy to clipboard" button for code blocks, commit messages or any parts in general in the chat UI.
- [ ] Figure out ways to reduce context usage.
- [ ] Figure out ways to improve scrolling in the conversation. Currently super laggish after 70.000 tokens and nearly 
impossible to scroll through the last received Content for two reasons: the scroll bar chunk becomes super small and it jumps from the start of one cell to the start of another cell.
- [ ] Format code snippets in received parts from the model.
- [ ] Improve the function call confirmation dialog to also display the model's thought/rationale.
- [ ] Explore more efficient file editing strategies (e.g., diff/patch) instead of full read/write cycles.
- [ ] Explore more visually efficient file editing strategies. May be beter if the model could edit files in 
the editor itself navigating through the file like a human user so the actual user can understand better the models intentions and rationale
- [ ] Explore more efficient function calling strategies, currently the model sometimes makes two round 
trips for two function calls that could be done in one. 
- [ ] Explore the possibility to do non-blocking function calls as currently
 the chat disables the input field when a generateContent request is in progress, there may be function calls that the assistant/model
can perform without blocking the user (e.g. creating, testing or evolving a gem, attempting some other netbeans action,
 performing a research a task or any other task that can be triggered in a call that a) does not need any / all history or b) 
can be handled in a separate conversation / chat window that can be monitored by asistant of the "main" chat)
- [ ] Explore the possibility / benefits of notifying the model of user actions (user opened this fil in the editor, user triggered build, user modified this files, user opened project, user performed action, etc) via NetBeansListener
- [ ] Screenshot not working on ubuntu, produces os level error logs. Explore context usage of screenshots and alternatives for the model to "see" the uses screen without blowing up context usage
- [ ] Explore the possibility of asking questions to other models with free tiers.
- [ ] Explore the possibility of persisting some of the session's conversation or doing diffs or looking up commit message history 
or netbeans local history upon startup so the model is more aware of "were we left it" when the last nb session ended
- [ ] Assess the impact of the Gemini API limitation to combine local functions with web search tools (UNEXPECTED_TOOL_CALL)
to ensure the model can search the web or ask other models to do so if it needs to. It may be worth trying to show the the model how to
press the functions toggle button on the ui programmatically so it can toggle functions on or off when needed or to make a function called enableWebSearch that the model can call
 (there is an issue reported on googles genai java sdk github page https://github.com/googleapis/java-genai/issues/466)

