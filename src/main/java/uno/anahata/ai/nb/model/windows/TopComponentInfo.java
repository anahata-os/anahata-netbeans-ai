/* Licensed under the Apache License, Version 2.0 */
package uno.anahata.ai.nb.model.windows;

/**
 * A POJO containing detailed information about a single open TopComponent (window) in the NetBeans IDE.
 * 
 * @param id The unique identifier of the TopComponent.
 * @param name The name of the TopComponent.
 * @param selected Whether the TopComponent is currently selected.
 * @param displayName The human-readable display name.
 * @param htmlDisplayName The HTML-formatted display name.
 * @param tooltip The tooltip text.
 * @param className The fully qualified class name of the TopComponent.
 * @param mode The windowing mode (e.g., "editor", "output").
 * @param activatedNodes A string representation of the currently activated nodes.
 * @param supportedActions A string representation of the supported actions.
 * @param filePath The path to the file associated with this window, if any.
 * @param primaryFilePath The primary file path, if different from filePath.
 * @param sizeInBytes The size of the associated file in bytes.
 */
public record TopComponentInfo(
    String id,
    String name,
    boolean selected,
    String displayName,
    String htmlDisplayName,
    String tooltip,
    String className,
    String mode,
    String activatedNodes,
    String supportedActions,
    String filePath,
    String primaryFilePath,
    long sizeInBytes
) {}