/* Licensed under the Apache License, Version 2.0 */
package uno.anahata.ai.nb.model.java2;

import java.net.URL;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.modules.java.source.ui.JavaTypeDescription;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.URLMapper;
import javax.lang.model.element.Element;

/**
 * A lightweight, serializable "keychain" DTO that uniquely identifies a Java type.
 * It holds an ElementHandle and a URL to the class file.
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class JavaType {

    private ElementHandle<? extends Element> handle;
    private URL url;

    /**
     * Constructs a new JavaType from a JavaTypeDescription.
     * @param descriptor the type descriptor.
     */
    public JavaType(JavaTypeDescription descriptor) {
        if (descriptor == null) {
            throw new IllegalArgumentException("TypeDescriptor cannot be null.");
        }
        this.handle = descriptor.getHandle();
        FileObject fo = descriptor.getFileObject();
        if (fo == null) {
            throw new IllegalStateException("Could not get FileObject from TypeDescriptor");
        }
        this.url = fo.toURL();
    }

    /**
     * Resolves the URL back to a FileObject representing the class file.
     * @return the FileObject for the class file.
     * @throws Exception if the URL cannot be resolved.
     */
    public FileObject getClassFileObject() throws Exception {
        FileObject fo = URLMapper.findFileObject(url);
        if (fo == null) {
            throw new Exception("Could not resolve URL back to FileObject: " + url);
        }
        return fo;
    }

    /**
     * Gets the source information for this type.
     * @return a JavaTypeSource object.
     * @throws Exception if the source cannot be found.
     */
    public JavaTypeSource getSource() throws Exception {
        return new JavaTypeSource(this);
    }

    /**
     * Gets the Javadoc for this type.
     * @return a JavaTypeDocs object.
     * @throws Exception if the Javadoc cannot be retrieved.
     */
    public JavaTypeDocs getJavadoc() throws Exception {
        return new JavaTypeDocs(this);
    }

    /**
     * Gets the members of this type using a universal, ClassIndex-based approach.
     *
     * @return An unmodifiable list of JavaMembers.
     * @throws Exception if the members cannot be retrieved.
     */
    public List<JavaMember> getMembers() throws Exception {
        return new JavaMemberSearch(this).getMembers();
    }
}
