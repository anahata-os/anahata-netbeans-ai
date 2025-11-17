package uno.anahata.ai.nb.model.java;

import java.util.List;
import java.util.Set;

/**
 * A record to hold structured information about a class member (field, constructor, or method).
 *
 * @param name The name of the member.
 * @param kind The kind of member (e.g., "METHOD", "FIELD", "CONSTRUCTOR").
 * @param type The return type for a method or the type of a field.
 * @param modifiers The set of modifiers (e.g., "public", "static", "final").
 * @param parameters A list of parameter types for a method or constructor.
 */
public record MemberInfo(
    String name,
    String kind,
    String type,
    Set<String> modifiers,
    List<String> parameters
) {}
