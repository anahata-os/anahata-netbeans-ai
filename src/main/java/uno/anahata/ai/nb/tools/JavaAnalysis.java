package uno.anahata.ai.nb.tools;

import com.sun.source.tree.Tree;
import com.sun.source.util.SourcePositions;
import java.util.ArrayList;
import java.util.List;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.ElementFilter;
import javax.lang.model.util.Elements;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.Task;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import uno.anahata.ai.tools.AIToolMethod;
import uno.anahata.ai.tools.AIToolParam;

/**
 * Provides advanced Java code analysis tools that inspect relationships and usages within the source code.
 * @author Anahata
 */
public class JavaAnalysis {

    @AIToolMethod("Finds all method body usages of a given field within a specific Java class.")
    public static List<String> findFieldUsagesInClass(
            @AIToolParam("The absolute path to the Java source file.") String filePath,
            @AIToolParam("The fully qualified name of the class to inspect.") String fqn,
            @AIToolParam("The name of the field to search for.") String fieldName) throws Exception {

        FileObject fileObject = FileUtil.toFileObject(new java.io.File(filePath));
        if (fileObject == null) {
            return List.of("Error: Could not find file at path: " + filePath);
        }
        
        JavaSource javaSource = JavaSource.forFileObject(fileObject);
        if (javaSource == null) {
            return List.of("Error: Could not create JavaSource for: " + filePath);
        }

        List<String> usages = new ArrayList<>();
        javaSource.runUserActionTask((Task<CompilationController>) controller -> {
            controller.toPhase(JavaSource.Phase.RESOLVED);
            Elements elements = controller.getElements();
            
            TypeElement classElement = elements.getTypeElement(fqn);
            if (classElement == null) {
                usages.add("Error: Could not find TypeElement for " + fqn);
                return;
            }

            SourcePositions pos = controller.getTrees().getSourcePositions();
            
            for (ExecutableElement method : ElementFilter.methodsIn(classElement.getEnclosedElements())) {
                Tree tree = controller.getTrees().getTree(method);
                if (tree != null) {
                    long start = pos.getStartPosition(controller.getCompilationUnit(), tree);
                    long end = pos.getEndPosition(controller.getCompilationUnit(), tree);
                    
                    if (start != -1 && end != -1) {
                        String methodSource = controller.getText().substring((int) start, (int) end);
                        if (methodSource.contains(fieldName)) {
                            String methodName = method.getSimpleName().contentEquals("<init>") ? "Constructor" : method.getSimpleName().toString();
                            usages.add(methodName);
                        }
                    }
                }
            }
        }, true);

        return usages;
    }
}