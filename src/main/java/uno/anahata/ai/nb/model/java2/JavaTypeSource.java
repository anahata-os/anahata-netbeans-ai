/* Licensed under the Apache License, Version 2.0 */
package uno.anahata.ai.nb.model.java2;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.stream.Collectors;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import lombok.Getter;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.queries.SourceForBinaryQuery;
import org.netbeans.api.java.source.JavaSource;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.URLMapper;

/**
 * A rich result object that represents the outcome of a source-finding operation for a JavaType.
 * It handles both top-level and nested types by resolving the outermost type element.
 */
@Getter
public class JavaTypeSource {

    protected final JavaType javaType;
    protected final FileObject sourceFile;
    protected String content;

    /**
     * Constructs a new JavaTypeSource and attempts to find the source file for the given JavaType.
     * @param javaType the type to find source for.
     * @throws Exception if the source cannot be found or read.
     */
    public JavaTypeSource(JavaType javaType) throws Exception {
        this.javaType = javaType;
        
        // 1. Get the FileObject (could be .java or .class)
        FileObject classFile = javaType.getClassFileObject();
        
        // 2. Create a JavaSource for the file.
        JavaSource js = JavaSource.forFileObject(classFile);
        if (js == null) {
            throw new Exception("Could not create JavaSource for: " + classFile.getPath());
        }

        final String[] resourceName = new String[1];
        final Exception[] taskException = new Exception[1];
        
        js.runUserActionTask(controller -> {
            try {
                controller.toPhase(JavaSource.Phase.RESOLVED);
                Element resolved = javaType.getHandle().resolve(controller);
                if (resolved != null) {
                    // Find the enclosing type element (or the element itself if it's already a type)
                    Element current = resolved;
                    while (current != null && !(current instanceof TypeElement)) {
                        current = current.getEnclosingElement();
                    }
                    
                    if (current instanceof TypeElement te) {
                        // Java 17 compatible way to find the outermost type:
                        // Traverse up until the enclosing element is a package.
                        Element outermost = te;
                        while (outermost.getEnclosingElement() != null && 
                               outermost.getEnclosingElement().getKind() != ElementKind.PACKAGE) {
                            outermost = outermost.getEnclosingElement();
                        }
                        
                        if (outermost instanceof TypeElement outermostTe) {
                            resourceName[0] = outermostTe.getQualifiedName().toString().replace('.', '/') + ".java";
                        }
                    }
                }
            } catch (Exception e) {
                taskException[0] = e;
            }
        }, true);

        if (taskException[0] != null) {
            throw taskException[0];
        }

        if (resourceName[0] == null) {
            throw new Exception("Could not resolve type element for: " + javaType.getHandle());
        }

        // 3. Locate the source file using the resolved resource name.
        URL url = javaType.getUrl();
        String protocol = url.getProtocol();
        FileObject foundSourceFile = null;

        if ("file".equals(protocol)) {
            ClassPath sp = ClassPath.getClassPath(classFile, ClassPath.SOURCE);
            if (sp != null) {
                foundSourceFile = sp.findResource(resourceName[0]);
            }
        } else if ("jar".equals(protocol)) {
            URL binaryRootUrl = URLMapper.findURL(classFile.getFileSystem().getRoot(), URLMapper.EXTERNAL);
            if (binaryRootUrl != null) {
                SourceForBinaryQuery.Result result = SourceForBinaryQuery.findSourceRoots(binaryRootUrl);
                for (FileObject root : result.getRoots()) {
                    foundSourceFile = root.getFileObject(resourceName[0]);
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
            throw new Exception("Source code not found for: " + resourceName[0] + ". "
                    + "If this is a Maven dependency, try using 'MavenTools.downloadProjectDependencies' or 'MavenTools.downloadDependencyArtifact' to retrieve the 'sources' classifier.");
        }
        
        this.sourceFile = foundSourceFile;

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(this.sourceFile.getInputStream()))) {
            this.content = reader.lines().collect(Collectors.joining("\n"));
        }
    }
}
