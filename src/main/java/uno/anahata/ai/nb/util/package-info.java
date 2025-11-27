/**
 * This package contains utility classes that provide helper functions and bridge
 * various NetBeans Platform APIs for use by the plugin's core components and tools.
 * These classes handle low-level, cross-cutting concerns, simplifying the logic
 * in other parts of the plugin.
 *
 * <h2>Key Utilities:</h2>
 * <ul>
 *   <li>{@link uno.anahata.ai.nb.util.NetBeansModuleUtils}: A critical utility
 *       responsible for introspecting the NetBeans module system. Its primary role
 *       is to dynamically construct a complete classpath at runtime, including all
 *       plugin dependencies, which is then injected into the {@code RunningJVM}
 *       tool to enable in-process compilation and execution.</li>
 *
 *   <li>{@link uno.anahata.ai.nb.util.NetBeansJavaQueryUtils}: Provides helper
 *       methods that leverage the NetBeans Java Queries API
 *       ({@code org.netbeans.api.java.queries}) to find the source file
 *       ({@code FileObject}) for a given fully qualified class name, searching
 *       across all registered classpaths (project source, dependencies, and JDK).</li>
 *
 *   <li>{@link uno.anahata.ai.nb.util.ClassPathBuilder}: A simple utility for
 *       converting a collection of {@code File} objects into a properly formatted
 *       classpath string.</li>
 *
 *   <li>{@link uno.anahata.ai.nb.util.TeeInputOutput}: An implementation of the
 *       NetBeans {@code InputOutput} interface that acts as a "tee," duplicating
 *       all output sent to it to both the original delegate (e.g., the Output Window)
 *       and an in-memory buffer. This allows the AI to capture and analyze the
 *       output of processes it initiates.</li>
 * </ul>
 */
package uno.anahata.ai.nb.util;
