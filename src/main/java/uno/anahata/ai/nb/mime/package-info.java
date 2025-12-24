/* Licensed under the Apache License, Version 2.0 */
/**
 * This package provides a sophisticated bridge between the Anahata AI framework's
 * Swing UI and the Apache NetBeans editor infrastructure. Its primary purpose is
 * to enable the AI's chat panel to leverage the IDE's native syntax highlighting
 * capabilities for code blocks.
 *
 * <h2>Key Components:</h2>
 * <ul>
 *   <li>{@link uno.anahata.ai.nb.mime.NetBeansEditorKitProvider}: An implementation
 *       of the framework's {@code EditorKitProvider} interface. It uses the NetBeans
 *       {@code MimeLookup} API to find the appropriate {@code EditorKit} for a given
 *       language (e.g., "java", "xml"). This allows code snippets in the chat to be
 *       rendered with the same rich syntax highlighting and formatting as the main
 *       code editor.</li>
 *
 *   <li>{@link uno.anahata.ai.nb.mime.MimeUtils} and
 *       {@link uno.anahata.ai.nb.mime.DisabledModulesMimeUtils}: These are utility
 *       classes that deeply inspect the NetBeans System Filesystem and module layers.
 *       They build a comprehensive map of all known MIME types and their associated
 *       file extensions, ensuring that the {@code NetBeansEditorKitProvider} can
 *       support the widest possible range of languages, even those from disabled
- *       modules.</li>
 * </ul>
 *
 * This package is a key example of the deep integration that makes the Anahata AI
 * Assistant a seamless part of the NetBeans development experience.
 */
package uno.anahata.ai.nb.mime;