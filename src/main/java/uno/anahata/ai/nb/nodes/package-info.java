/* Licensed under the Apache License, Version 2.0 */
/**
 * This package provides the visual integration between the Anahata AI's context and
 * the NetBeans project tree UI, primarily using the NetBeans Nodes API. Its purpose
 * is to "decorate" files in the Projects window to indicate that they are currently
 * part of the AI's active context.
 *
 * <h2>Key Components:</h2>
 * <ul>
 *   <li>{@link uno.anahata.ai.nb.nodes.AnahataNodeFactory}: A NetBeans
 *       {@code NodeFactory} that creates the virtual "Anahata" folder. This
 *       component is responsible for dynamically managing the visibility of
 *       project-specific AI resources like {@code anahata.md}.</li>
 * </ul>
 *
 * The functionality of this package is a high-priority goal for the V1 launch,
 * providing users with immediate visual feedback on which files are being
 * tracked by the assistant.
 */
package uno.anahata.ai.nb.nodes;