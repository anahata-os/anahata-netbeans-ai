/*
 * Licensed under the Anahata Software License (ASL) v 108. See the LICENSE file for details. Fora Bara!
 */
package uno.anahata.ai.nb.model.java2;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import lombok.Getter;
import org.netbeans.api.java.source.ElementHandle;

/**
 * A lightweight, serializable "keychain" DTO that uniquely identifies a Java
 * class member (field, method, constructor, etc.). It is designed to be the
 * result of a discovery tool and the input to an action tool.
 *
 * @author pablo
 */
@Getter
public class JavaMember {

    /**
     * The serializable handle to the actual code element. This may be null if
     * the member was discovered via a method that doesn't produce a handle.
     */
    private final ElementHandle<? extends Element> handle;

    /**
     * The simple name of the member (e.g., "myField", "myMethod").
     */
    private final String name;

    /**
     * The kind of the member (e.g., FIELD, METHOD, CONSTRUCTOR).
     */
    private final ElementKind kind;

    /**
     * A human-readable representation of the member's signature or type.
     */
    private final String details;

    public JavaMember(ElementHandle<? extends Element> handle, String name, ElementKind kind, String details) {
        this.handle = handle;
        this.name = name;
        this.kind = kind;
        this.details = details;
    }

    @Override
    public String toString() {
        return kind + ": " + name + " (" + details + ")";
    }
}
