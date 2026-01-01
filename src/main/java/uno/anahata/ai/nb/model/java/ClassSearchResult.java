/* Licensed under the Apache License, Version 2.0 */
package uno.anahata.ai.nb.model.java;

import org.netbeans.api.java.classpath.ClassPath;
import org.openide.filesystems.FileObject;

/**
 * A simple data object to hold the result of a class file search,
 * containing the FileObject of the class and the ClassPath it belongs to.
 * This is used to bridge between finding a .class file and then finding its
 * corresponding .java source file or Javadoc.
 * @author Anahata
 */
public class ClassSearchResult {

    /**
     * The FileObject representing the found class file.
     */
    public final FileObject classFile;
    
    /**
     * The ClassPath that owns the found class file.
     */
    public final ClassPath ownerCp;

    /**
     * Constructs a new ClassSearchResult.
     * @param classFile the class file found.
     * @param ownerCp the classpath owning the class file.
     */
    public ClassSearchResult(FileObject classFile, ClassPath ownerCp) {
        this.classFile = classFile;
        this.ownerCp = ownerCp;
    }
}