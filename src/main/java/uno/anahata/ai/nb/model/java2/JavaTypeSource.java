/* Licensed under the Apache License, Version 2.0 */
package uno.anahata.ai.nb.model.java2;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.stream.Collectors;
import lombok.Getter;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.queries.SourceForBinaryQuery;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.URLMapper;

/**
 * A rich result object that represents the outcome of a source-finding operation for a JavaType.
 */
@Getter
public class JavaTypeSource {

    private final JavaType javaType;
    private final FileObject sourceFile;
    private final String content;

    /**
     * Constructs a new JavaTypeSource and attempts to find the source file for the given JavaType.
     * @param javaType the type to find source for.
     * @throws Exception if the source cannot be found or read.
     */
    public JavaTypeSource(JavaType javaType) throws Exception {
        this.javaType = javaType;
        
        FileObject classFile = javaType.getClassFileObject();

        String protocol = javaType.getUrl().getProtocol();
        FileObject foundSourceFile = null;
        String resourceName = javaType.getHandle().getQualifiedName().replace('.', '/') + ".java";

        if ("file".equals(protocol)) {
            ClassPath sourcePath = ClassPath.getClassPath(classFile, ClassPath.SOURCE);
            if (sourcePath != null) {
                foundSourceFile = sourcePath.findResource(resourceName);
            }
        } else if ("jar".equals(protocol)) {
            URL binaryRootUrl = URLMapper.findURL(classFile.getFileSystem().getRoot(), URLMapper.EXTERNAL);
            if (binaryRootUrl != null) {
                SourceForBinaryQuery.Result result = SourceForBinaryQuery.findSourceRoots(binaryRootUrl);
                for (FileObject root : result.getRoots()) {
                    foundSourceFile = root.getFileObject(resourceName);
                    if (foundSourceFile != null) break;
                }
            }
        } else if ("nbjrt".equals(protocol)) {
            URL binaryRootUrl = URLMapper.findURL(classFile.getFileSystem().getRoot(), URLMapper.EXTERNAL);
            if (binaryRootUrl != null) {
                SourceForBinaryQuery.Result result = SourceForBinaryQuery.findSourceRoots(binaryRootUrl);
                for (FileObject root : result.getRoots()) {
                    String modularResourcePath = classFile.getPath();
                    if (modularResourcePath.startsWith("modules/")) {
                        modularResourcePath = modularResourcePath.substring("modules/".length());
                    }
                    foundSourceFile = root.getFileObject(modularResourcePath.replace(".class", ".java"));
                    if (foundSourceFile != null) break;
                }
            }
        }

        if (foundSourceFile == null) {
            throw new Exception("Source not found for: " + resourceName);
        }
        
        this.sourceFile = foundSourceFile;

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(this.sourceFile.getInputStream()))) {
            this.content = reader.lines().collect(Collectors.joining("\n"));
        }
    }
}