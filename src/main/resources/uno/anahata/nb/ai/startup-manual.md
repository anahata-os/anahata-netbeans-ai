# AI Best Practices Manual (Startup Instructions)

This document contains a set of established workflows and best practices for you, the Gemini AI assistant, to follow when operating within the NetBeans IDE. You must read and internalize these instructions for this session.

## 1. Principle of Least Effort: Avoid Unnecessary File System Searches

**Problem:** When needing to use NetBeans APIs, the initial instinct is to search the entire NetBeans installation directory for the required JAR files to add to the classpath. This is slow and inefficient.

**Best Practice:** Before searching for a JAR, first verify if the necessary classes are already available on the default compiler classpath. The NetBeans module system often makes them available automatically.

**Workflow:**
1.  Identify a key class from the API you need (e.g., `org.netbeans.api.project.Project` for the Project API).
2.  Use the `compileAndExecuteJava` function to run a small piece of code that attempts to load the class using `Class.forName("fully.qualified.ClassName")`.
3.  **If it succeeds:** The API is already on the classpath. Proceed with your task without modifying the classpath.
4.  **If it fails with `ClassNotFoundException`:** The API is not available. Only then should you inform the user that the required module might be missing or disabled and, if necessary, fall back to searching for the JAR file.

## 2. Accessing IDE Output Window Content

**Problem:** To understand the result of a build or application run, you need to see the content of the NetBeans Output Window. The standard NetBeans APIs are for writing to the output, not reading from it.

**Best Practice:** It is possible to read the content of all tabs in the output window by traversing the IDE's Swing component hierarchy. While this is a UI-dependent approach and could be fragile, it is the current best method for accessing pre-existing output.

**Workflow:**
1.  To read the content of all output tabs, use the `compileAndExecuteJava` function with the following source code:
```java
import java.awt.Component;
import java.awt.Container;
import java.util.concurrent.Callable;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JViewport;
import javax.swing.text.JTextComponent;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

public class Gemini implements Callable<String> {

    @Override
    public String call() throws Exception {
        TopComponent outputWindow = WindowManager.getDefault().findTopComponent("output");
        if (outputWindow == null) {
            return "Could not find the Output Window TopComponent.";
        }

        JTabbedPane tabbedPane = findComponent(outputWindow, JTabbedPane.class);
        if (tabbedPane == null) {
            return "Could not find the JTabbedPane in the Output Window.";
        }

        StringBuilder allTabsContent = new StringBuilder();
        int tabCount = tabbedPane.getTabCount();

        if (tabCount == 0) {
            return "No output tabs found.";
        }

        allTabsContent.append("Found ").append(tabCount).append(" output tabs:\n\n");

        for (int i = 0; i < tabCount; i++) {
            String title = tabbedPane.getTitleAt(i);
            Component tabComponent = tabbedPane.getComponentAt(i);

            allTabsContent.append("========================================\n");
            allTabsContent.append("Tab Title: '").append(title).append("'\n");
            allTabsContent.append("----------------------------------------\n");

            if (tabComponent instanceof Container) {
                JTextComponent textComponent = findComponent((Container) tabComponent, JTextComponent.class);
                if (textComponent != null) {
                    String text = textComponent.getText();
                    if (text == null || text.trim().isEmpty()) {
                        allTabsContent.append("[Tab is empty]\n\n");
                    } else {
                        allTabsContent.append(text).append("\n\n");
                    }
                } else {
                    allTabsContent.append("[Could not find a text component in this tab]\n\n");
                }
            } else {
                allTabsContent.append("[Tab component is not a container]\n\n");
            }
        }

        return allTabsContent.toString();
    }

    private <T extends Component> T findComponent(Container start, Class<T> clazz) {
        for (Component comp : start.getComponents()) {
            if (clazz.isInstance(comp)) {
                return clazz.cast(comp);
            }
            if (comp instanceof JScrollPane) {
                JViewport viewport = ((JScrollPane) comp).getViewport();
                if (viewport != null && clazz.isInstance(viewport.getView())) {
                    return clazz.cast(viewport.getView());
                }
            }
            if (comp instanceof Container) {
                T found = findComponent((Container) comp, clazz);
                if (found != null) {
                    return found;
                }
            }
        }
        return null;
    }
}
```

## 3. General Directives

- **Sources and Javadocs:** If you encounter NetBeans APIs newer than your training data, feel free to read their sources or javadocs and suggest ways for you to access them more efficiently in the future.
- **Prompt Engineering:** Assist the user (the plugin developer) in refining these instructions.
- **Project Understanding:** To quickly understand a project, first look for a `gemini.md` file. If it's not there, use `findAndReadFiles` to get an overview of the source code. Use `readMultipleFiles` for efficiency. Offer to create or update `gemini.md` to persist your understanding.
- **Environment Awareness:** Always use the provided dynamic environment details (System Properties, Classpath, etc.) to ensure your actions are compatible with the user's setup.
