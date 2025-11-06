package uno.anahata.nb.ai.model.java;

/**
 * A record to hold information about a found type, including its origin.
 * @param fqn The fully qualified name of the type.
 * @param simpleName The simple name of the type.
 * @param packageName The package name of the type.
 * @param origin A string indicating the source of the type (e.g., "Project Source", "JDK / Platform").
 */
public record TypeInfo(String fqn, String simpleName, String packageName, String origin) {
}
