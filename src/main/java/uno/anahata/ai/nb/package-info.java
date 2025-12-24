/* Licensed under the Apache License, Version 2.0 */
/**
 * This package contains the core integration classes that bridge the Anahata AI framework
 * with the Apache NetBeans IDE. It is the central hub of the `anahata-netbeans-ai` plugin.
 *
 * <h2>Key Components:</h2>
 * <ul>
 *   <li>{@link uno.anahata.ai.nb.AnahataTopComponent}: The primary UI component, a NetBeans
 *       {@code TopComponent}, that hosts the main chat interface (`ChatPanel`). It manages
 *       the lifecycle of a single chat session within the IDE.</li>
 *
 *   <li>{@link uno.anahata.ai.nb.LiveSessionsTopComponent}: A session manager window that
 *       displays a live, sortable table of all active Anahata AI sessions. It allows users
 *       to monitor status, context usage, and switch between different chat instances.</li>
 *
 *   <li>{@link uno.anahata.ai.nb.NetBeansChatConfig}: A crucial configuration class that
 *       extends the framework's {@code SwingChatConfig}. It registers all the NetBeans-specific
 *       tools (from the {@code uno.anahata.ai.nb.tools} package) and context providers,
 *       making the AI aware of the IDE's state.</li>
 *
 *   <li>{@link uno.anahata.ai.nb.AnahataInstaller}: The NetBeans module installer class
 *       ({@code ModuleInstall}) responsible for setup and teardown logic, including the
 *       innovative session handoff mechanism that preserves chat windows across plugin
 *       reloads.</li>
 *
 *   <li>{@link uno.anahata.ai.nb.AddProjectAction} and {@link uno.anahata.ai.nb.ShowDefaultCompilerClassPathAction}:
 *       Example actions that demonstrate how to integrate AI functionality into the NetBeans
 *       menus and UI.</li>
 * </ul>
 *
 * This package serves as the foundation for the AI's deep integration into the NetBeans
 * development workflow.
 */
package uno.anahata.ai.nb;