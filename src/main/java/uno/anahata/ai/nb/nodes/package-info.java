/* Licensed under the Apache License, Version 2.0 */
/**
 * This package provides the visual integration between the Anahata AI's context and
 * the NetBeans project tree UI, primarily using the NetBeans Nodes API. Its purpose
 * is to "decorate" files in the Projects window to indicate that they are currently
 * part of the AI's active context.
 *
 * <p>
 * <b>NOTE: This entire package is currently NON-FUNCTIONAL and under review.</b>
 * The file decoration feature does not work, and there are known bugs that need
 * to be addressed before this package can be considered stable.
 *
 * <h2>Key Components & Known Issues:</h2>
 * <ul>
 *   <li>{@link uno.anahata.ai.nb.nodes.AnahataNodeFactory}: A NetBeans
 *       {@code NodeFactory} that creates the virtual "Anahata" folder.
 *       <b>Issue:</b> This component has a memory leak and does not refresh correctly
 *       when new files (e.g., an {@code anahata.md}) are added to the project root.</li>
 *
 *   <li>{@link uno.anahata.ai.nb.nodes.ContextFileImageDecorator} and
 *       {@link uno.anahata.ai.nb.nodes.ContextFileStatusDecorator}: These classes
 *       are intended to add visual annotations to file nodes.
 *       <b>Issue:</b> They are currently not functional.</li>
 *
 *   <li>{@link uno.anahata.ai.nb.nodes.ContextFilterNode}: A {@code FilterNode}
 *       implementation that wraps the original file nodes to apply the decorations.</li>
 * </ul>
 *
 * The central registry class, {@code ContextFiles.java}, has been deleted as it
 * was part of the non-functional V1 implementation. The functionality of this
 * package is a high-priority goal for the V1 launch, as noted in the project's
 * main {@code anahata.md} file.
 */
package uno.anahata.ai.nb.nodes;