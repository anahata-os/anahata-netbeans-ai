/* Licensed under the Apache License, Version 2.0 */
/**
 * This package serves as the data backbone for the Anahata AI Assistant plugin.
 * It contains all the Data Transfer Objects (DTOs) and Plain Old Java Objects (POJOs)
 * that define the structure of information flowing between the AI, the IDE tools,
 * and the UI components.
 *
 * <p>The models are organized into sub-packages based on their domain:</p>
 *
 * <ul>
 *   <li><b>{@link uno.anahata.ai.nb.model.coding}</b>: Contains models related to
 *       code manipulation, such as the structured result from the interactive
 *       {@code suggestChange} tool.</li>
 *
 *   <li><b>{@link uno.anahata.ai.nb.model.ide}</b>: Data models for general IDE
 *       components, like representing a tab in the NetBeans Output Window.</li>
 *
 *   <li><b>{@link uno.anahata.ai.nb.model.java}</b>: A rich set of models for
 *       representing Java code elements (types, members, source files) and the
 *       paginated results from introspection and search tools.</li>
 *
 *   <li><b>{@link uno.anahata.ai.nb.model.maven}</b>: A comprehensive and highly
 *       token-efficient set of models for representing Maven artifacts, dependencies
 *       (both declared and fully resolved), and build results.</li>
 *
 *   <li><b>{@link uno.anahata.ai.nb.model.projects}</b>: Models for representing
 *       the high-level structure of a NetBeans project, including its files,
 *       source folders, and overall configuration.</li>
 *
 *   <li><b>{@link uno.anahata.ai.nb.model.windows}</b>: Contains models for
 *       representing open windows and tabs ({@code TopComponent}s) within the IDE.</li>
 * </ul>
 *
 * By providing a clear and consistent data structure, this package enables
 * efficient and reliable communication throughout the entire plugin architecture.
 */
package uno.anahata.ai.nb.model;
