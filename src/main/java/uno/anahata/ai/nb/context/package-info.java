/**
 * This package contains the sensory system of the Anahata AI Assistant for NetBeans.
 * It is composed of {@link uno.anahata.ai.context.provider.ContextProvider}
 * implementations that are responsible for feeding the AI with a continuous,
 * just-in-time stream of information about the live state of the IDE.
 *
 * <h2>Key Context Providers:</h2>
 * <ul>
 *   <li>{@link uno.anahata.ai.nb.context.CoreNetBeansInstructionsProvider}: Injects
 *       fundamental, high-level instructions about the NetBeans environment, such
 *       as the location of project folders and core principles for interacting with
 *       the IDE's APIs.</li>
 *
 *   <li>{@link uno.anahata.ai.nb.context.IdeAlertsContextProvider}: Provides a
 *       real-time feed of all compilation errors and warnings detected by the IDE's
 *       live parser across all open projects. This is a critical sense that allows
 *       the AI to be "aware" of code health.</li>
 *
 *   <li>{@link uno.anahata.ai.nb.context.ProjectOverviewContextProvider} and
 *       {@link uno.anahata.ai.nb.context.OpenProjectsOverviewContextProvider}:
 *       These providers work together to give the AI a detailed understanding of
 *       the workspace, including project structure, source files, dependencies,
 *       and build configurations.</li>
 *
 *   <li>{@link uno.anahata.ai.nb.context.OpenTopComponentsContextProvider}: Allows
 *       the AI to "see" which windows, tabs, and editors are currently open,
 *       providing crucial UI context.</li>
 *
 *   <li>{@link uno.anahata.ai.nb.context.OutputTabsContextProvider}: Gives the AI
 *       the ability to list and read from the various tabs in the NetBeans
 *       Output Window, enabling it to monitor build processes and other tasks.</li>
 * </ul>
 *
 * Together, these providers form the "augmented workspace," a rich, dynamic
 * layer of information that is injected into the AI's context before every
 * request, making it a deeply integrated and aware development partner.
 */
package uno.anahata.ai.nb.context;
