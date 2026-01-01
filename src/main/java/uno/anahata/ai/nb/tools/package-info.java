/* Licensed under the Apache License, Version 2.0 */
/**
 * This package contains the heart of the Anahata NetBeans AI Plugin's unique
 * capabilities. It provides a suite of AI-callable tools that grant the model
 * the ability to programmatically "see" and interact with the Apache NetBeans
 * IDE in a deep and context-aware manner.
 *
 * <h2>Core Tool Categories:</h2>
 * <ul>
 *   <li><b>Project &amp; IDE Interaction:</b>
 *     <ul>
 *       <li>{@link uno.anahata.ai.nb.tools.Projects}: Tools for discovering,
 *           querying, and getting detailed overviews of open NetBeans projects.</li>
 *       <li>{@link uno.anahata.ai.nb.tools.IDE}: General tools for interacting
 *           with the IDE itself, most notably providing real-time access to all
 *           compilation errors and warnings via {@code getAllIDEAlerts}.</li>
 *       <li>{@link uno.anahata.ai.nb.tools.TopComponents}: Allows the AI to list
 *           and get detailed information about all open windows and tabs in the IDE.</li>
 *       <li>{@link uno.anahata.ai.nb.tools.Output}: Provides tools to read and
 *           interact with the content of the NetBeans Output Window.</li>
 *     </ul>
 *   </li>
 *
 *   <li><b>Code Analysis &amp; Manipulation:</b>
 *     <ul>
 *       <li>{@link uno.anahata.ai.nb.tools.Coding}: Provides the critical
 *           {@code suggestChange} tool, which presents a modal diff view to the
 *           user for approving code modifications.</li>
 *       <li>{@link uno.anahata.ai.nb.tools.Editor}: Allows the AI to open files
 *           in the editor and navigate to specific lines.</li>
 *       <li>{@link uno.anahata.ai.nb.tools.Refactor}: Exposes the NetBeans
 *           refactoring engine, allowing the AI to perform safe, programmatic
 *           renames and other refactoring operations.</li>
 *       <li>{@link uno.anahata.ai.nb.tools.JavaIntrospection},
 *           {@link uno.anahata.ai.nb.tools.JavaSources},
 *           {@link uno.anahata.ai.nb.tools.JavaDocs}, and
 *           {@link uno.anahata.ai.nb.tools.JavaAnalysis}: A powerful suite for
 *           deep code analysis. They allow the AI to inspect class structures,
 *           find types, retrieve Javadoc, and read source code from projects,
 *           dependency JARs, or the JDK itself.</li>
 *     </ul>
 *   </li>
 *
 *   <li><b>Build &amp; Execution:</b>
 *     <ul>
 *       <li>{@link uno.anahata.ai.nb.tools.deprecated.MavenTools}: A consolidated "super-tool"
 *           for all Maven-related operations, including searching for dependencies,
 *           modifying the {@code pom.xml}, and executing build goals. This class
 *           replaces the functionality of the now-deprecated {@code Maven},
 *           {@code MavenPom}, and {@code MavenSearch} tools.</li>
 *       <li>{@link uno.anahata.ai.nb.tools.NetBeansProjectJVM}: A powerful tool
 *           that enables a "hot-reload" workflow by compiling and executing code
 *           within the specific classpath and context of a NetBeans project.</li>
 *       <li>{@link uno.anahata.ai.nb.tools.Git}: Integrates with the IDE's Git
 *           support to perform actions like opening the commit dialog.</li>
 *     </ul>
 *   </li>
 * </ul>
 *
 * These tools collectively transform the AI from a simple text generator into a
 * true development partner integrated into the NetBeans environment.
 */
package uno.anahata.ai.nb.tools;