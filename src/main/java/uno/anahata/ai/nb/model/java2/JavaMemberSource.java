/* Licensed under the Apache License, Version 2.0 */
package uno.anahata.ai.nb.model.java2;

import com.sun.source.tree.Tree;
import com.sun.source.util.SourcePositions;
import java.util.concurrent.atomic.AtomicReference;
import javax.lang.model.element.Element;
import lombok.Getter;
import org.netbeans.api.java.source.JavaSource;

/**
 * A rich result object that represents the outcome of a source-finding operation for a specific JavaMember.
 * It extracts only the source code of the member itself from the containing file.
 */
@Getter
public class JavaMemberSource extends JavaTypeSource {

    /**
     * Constructs a new JavaMemberSource and attempts to extract the source code for the given JavaMember.
     * @param member the member to find source for.
     * @throws Exception if the source cannot be found or read.
     */
    public JavaMemberSource(JavaMember member) throws Exception {
        super(member);
        
        // 1. Create a JavaSource for the file (sourceFile is inherited from JavaTypeSource)
        JavaSource js = JavaSource.forFileObject(this.sourceFile);
        if (js == null) {
            throw new Exception("Could not create JavaSource for: " + this.sourceFile.getPath());
        }

        final AtomicReference<String> sourceRef = new AtomicReference<>();
        final Exception[] taskException = new Exception[1];

        js.runUserActionTask(controller -> {
            try {
                controller.toPhase(JavaSource.Phase.RESOLVED);
                // 2. Resolve the handle to an Element
                Element element = member.getHandle().resolve(controller);
                if (element != null) {
                    // 3. Get the Tree for the element
                    Tree tree = controller.getTrees().getTree(element);
                    if (tree != null) {
                        // 4. Get the source positions
                        SourcePositions sp = controller.getTrees().getSourcePositions();
                        int start = (int) sp.getStartPosition(controller.getCompilationUnit(), tree);
                        int end = (int) sp.getEndPosition(controller.getCompilationUnit(), tree);
                        
                        if (start != -1 && end != -1) {
                            // 5. Extract the substring
                            sourceRef.set(controller.getText().substring(start, end));
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

        this.content = sourceRef.get();

        if (this.content == null) {
            throw new Exception("Source code not found for member: " + member.getHandle() + ". "
                    + "If this is a Maven dependency, try using 'MavenTools.downloadProjectDependencies' or 'MavenTools.downloadDependencyArtifact' to retrieve the 'sources' classifier.");
        }
    }
    
    public JavaMember getMember() {
        return (JavaMember) javaType;
    }
}
