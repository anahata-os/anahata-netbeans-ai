/* Licensed under the Apache License, Version 2.0 */
package uno.anahata.ai.nb.model.java2;

import com.sun.source.doctree.DocCommentTree;
import com.sun.source.util.DocTrees;
import com.sun.source.util.TreePath;
import java.util.concurrent.atomic.AtomicReference;
import javax.lang.model.element.Element;
import lombok.Getter;
import lombok.SneakyThrows;
import org.netbeans.api.java.source.JavaSource;
import org.openide.filesystems.FileObject;

/**
 * A rich result object that represents the outcome of a Javadoc retrieval operation for a JavaType.
 * It leverages the polymorphic nature of JavaType to retrieve docs for classes, inner classes, and members.
 */
@Getter
public class JavaTypeDocs {

    private final JavaType javaType;
    private final String javadoc;

    /**
     * Constructs a new JavaTypeDocs and attempts to retrieve the Javadoc for the given JavaType.
     * @param javaType the type or member to retrieve Javadoc for.
     */
    @SneakyThrows
    public JavaTypeDocs(JavaType javaType) {
        this.javaType = javaType;
        
        // 1. Get the source file using the existing logic in JavaTypeSource
        FileObject sourceFile = javaType.getSource().getSourceFile();
        
        // 2. Create a JavaSource for the file
        JavaSource js = JavaSource.forFileObject(sourceFile);
        if (js == null) {
            this.javadoc = null;
            return;
        }

        final AtomicReference<String> docRef = new AtomicReference<>();
        js.runUserActionTask(controller -> {
            controller.toPhase(JavaSource.Phase.RESOLVED);
            // 3. Resolve the handle to an Element
            Element element = javaType.getHandle().resolve(controller);
            if (element != null) {
                DocTrees docTrees = controller.getDocTrees();
                // 4. Get the TreePath for the element
                TreePath path = docTrees.getPath(element);
                if (path != null) {
                    // 5. Extract the DocCommentTree
                    DocCommentTree docCommentTree = docTrees.getDocCommentTree(path);
                    if (docCommentTree != null) {
                        docRef.set(cleanJavadoc(docCommentTree.toString()));
                    }
                }
            }
        }, true);
        
        this.javadoc = docRef.get();
    }

    private static String cleanJavadoc(String rawDoc) {
        if (rawDoc == null) {
            return "";
        }
        return rawDoc
                .replaceAll("^/\\*\\*|\\*/$", "") // Remove /** and */
                .replaceAll("\n[ \t]*\\* ?", "\n") // Remove leading * from each line
                .trim();
    }
}
