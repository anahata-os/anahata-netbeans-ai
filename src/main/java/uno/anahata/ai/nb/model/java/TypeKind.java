package uno.anahata.ai.nb.model.java;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Defines the kind of Java type to search for.
 */
@Schema(description = "Defines the kind of Java type to search for.")
public enum TypeKind {
    @Schema(description = "Search for classes only.")
    CLASS,
    @Schema(description = "Search for interfaces only.")
    INTERFACE,
    @Schema(description = "Search for enums only.")
    ENUM,
    @Schema(description = "Search for records only.")
    RECORD,
    @Schema(description = "Search for annotation types only.")
    ANNOTATION_TYPE,
    @Schema(description = "Search for all types (classes, interfaces, enums, records, and annotation types).")
    ALL
}
