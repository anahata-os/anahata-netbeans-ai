/* Licensed under the Apache License, Version 2.0 */
package uno.anahata.ai.nb.model.java2;

import java.net.URL;
import java.util.List;
import lombok.Getter;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.modules.java.source.ui.JavaTypeDescription;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.URLMapper;

@Getter
public class JavaType {

    private final ElementHandle handle;
    private final URL url;

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

    public FileObject getClassFileObject() throws Exception {
        FileObject classFile = URLMapper.findFileObject(getUrl());
        if (classFile == null) {
            throw new Exception("Could not resolve URL back to FileObject: " + getUrl());
        }
        return classFile;
    }

    public JavaTypeSource getSource() throws Exception {
        return new JavaTypeSource(this);
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