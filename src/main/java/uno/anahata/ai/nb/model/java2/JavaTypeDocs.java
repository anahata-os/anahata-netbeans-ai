/* Licensed under the Apache License, Version 2.0 */
package uno.anahata.ai.nb.model.java2;

import java.util.Enumeration;
import java.util.concurrent.atomic.AtomicReference;
import javax.lang.model.element.Element;
import lombok.Getter;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.ui.ElementJavadoc;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.api.project.ui.OpenProjects;
import org.openide.filesystems.FileObject;

/**
 * A rich result object that represents the outcome of a Javadoc retrieval operation for a JavaType.
 * It leverages the polymorphic nature of JavaType to retrieve docs for classes, inner classes, and members.
 */
@Getter
public class JavaTypeDocs {

    protected final JavaType javaType;
    protected String javadoc;

    /**
     * Constructs a new JavaTypeDocs and attempts to retrieve the Javadoc for the given JavaType.
     * @param javaType the type or member to retrieve Javadoc for.
     * @throws Exception if the Javadoc cannot be retrieved.
     */
    public JavaTypeDocs(JavaType javaType) throws Exception {
        this.javaType = javaType;
        
        // 1. Determine the best file to use for creating a JavaSource.
        // ElementJavadoc requires a CompilationInfo with a CompilationUnit, which is only 
        // available for .java files. For binary classes, we must find an "anchor" .java file 
        // in an open project that has the class on its classpath.
        FileObject fileToUse = null;
        try {
            fileToUse = javaType.getSource().getSourceFile();
        } catch (Exception e) {
            // Sources not found, look for an anchor file in open projects.
            FileObject classFile = javaType.getClassFileObject();
            fileToUse = findAnchorFile(classFile);
            if (fileToUse == null) {
                fileToUse = classFile; // Last resort, ElementJavadoc will likely fail.
            }
        }
        
        // 2. Create a JavaSource for the file.
        JavaSource js = JavaSource.forFileObject(fileToUse);
        if (js == null) {
            throw new Exception("Could not create JavaSource for: " + fileToUse.getPath());
        }

        final AtomicReference<String> docRef = new AtomicReference<>();
        final Exception[] taskException = new Exception[1];

        js.runUserActionTask(controller -> {
            try {
                controller.toPhase(JavaSource.Phase.RESOLVED);
                // 3. Resolve the handle to an Element
                Element element = javaType.getHandle().resolve(controller);
                if (element != null) {
                    // 4. Try using ElementJavadoc (rich HTML, handles binaries via anchor).
                    try {
                        ElementJavadoc ej = ElementJavadoc.create(controller, element);
                        if (ej != null) {
                            docRef.set(ej.getText());
                        }
                    } catch (NullPointerException npe) {
                        // Fallback to basic doc comment if ElementJavadoc fails (e.g. no anchor found).
                        String comment = controller.getElements().getDocComment(element);
                        docRef.set(comment);
                    }
                }
            } catch (Exception e) {
                taskException[0] = e;
            }
        }, true);
        
        if (taskException[0] != null) {
            throw taskException[0];
        }

        this.javadoc = cleanJavadoc(docRef.get());
        
        if (this.javadoc == null || this.javadoc.isEmpty()) {
             throw new Exception("Javadoc not found for: " + javaType.getHandle() + ". "
                    + "If this is a Maven dependency, try using 'MavenTools.downloadProjectDependencies' or 'MavenTools.downloadDependencyArtifact' to retrieve the 'javadoc' or 'sources' classifier.");
        }
    }

    private FileObject findAnchorFile(FileObject binaryFile) {
        for (Project p : OpenProjects.getDefault().getOpenProjects()) {
            Sources s = ProjectUtils.getSources(p);
            SourceGroup[] groups = s.getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
            for (SourceGroup sg : groups) {
                FileObject root = sg.getRootFolder();
                ClassPath cp = ClassPath.getClassPath(root, ClassPath.COMPILE);
                if (cp != null && cp.contains(binaryFile)) {
                    FileObject anchor = findFirstJavaFile(root);
                    if (anchor != null) return anchor;
                }
                // Also check boot classpath (for JDK classes)
                ClassPath boot = ClassPath.getClassPath(root, ClassPath.BOOT);
                if (boot != null && boot.contains(binaryFile)) {
                    FileObject anchor = findFirstJavaFile(root);
                    if (anchor != null) return anchor;
                }
            }
        }
        return null;
    }

    private FileObject findFirstJavaFile(FileObject root) {
        Enumeration<? extends FileObject> children = root.getChildren(true);
        while (children.hasMoreElements()) {
            FileObject child = children.nextElement();
            if (child.isData() && "java".equalsIgnoreCase(child.getExt())) {
                return child;
            }
        }
        return null;
    }

    protected static String cleanJavadoc(String rawDoc) {
        if (rawDoc == null) {
            return "";
        }
        return rawDoc.trim();
    }
}
