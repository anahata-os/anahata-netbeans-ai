package uno.anahata.ai.nb.model.java;

/**
 * Describes the origin of a source file, which determines if it's modifiable.
 * @author Anahata
 */
public enum SourceOrigin {
    /**
     * The source file is part of an open project and is modifiable.
     */
    PROJECT,
    /**
     * The source file is located inside a JAR file (e.g., a dependency) and is read-only.
     */
    JAR,
    /**
     * The source file is part of the JDK sources and is read-only.
     */
    JDK,
    /**
     * The origin of the source file could not be determined.
     */
    UNKNOWN
}
