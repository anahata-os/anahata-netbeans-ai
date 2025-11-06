package uno.anahata.nb.ai.tools;

import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.SourcePositions;
import com.sun.source.util.TreePathScanner;
import com.sun.source.util.Trees;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import javax.lang.model.element.Element;
import javax.lang.model.type.TypeMirror;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.Task;
import org.openide.filesystems.FileObject;
import uno.anahata.gemini.functions.AIToolMethod;
import uno.anahata.gemini.functions.AIToolParam;
import uno.anahata.nb.ai.util.NetBeansJavaQueryUtils;

/**
 * Provides tools for retrieving Java source code.
 * @author Anahata
 */
public class JavaSources {

    /**
     * Gets the full source code of a specific method from a Java file.
     *
     * @param fqn The fully qualified name of the class.
     * @param methodName The simple name of the method.
     * @param parameterTypes A list of fully qualified type names for the method's parameters, in order.
     *                       Use an empty list for methods with no parameters.
     * @return The source code of the method, including Javadoc and annotations, or null if not found.
     * @throws Exception if an error occurs.
     */
    @AIToolMethod(value = "Gets the full source code of a specific method from a Java file, including its Javadoc and annotations, using the IDE's Abstract Syntax Tree.", requiresApproval = false)
    public static String getSource(
            @AIToolParam("The fully qualified name of the class.") String fqn,
            @AIToolParam("The simple name of the method.") String methodName,
            @AIToolParam("A list of fully qualified type names for the method's parameters, in order. Use an empty list for methods with no parameters.") List<String> parameterTypes) throws Exception {
        FileObject sourceFile = NetBeansJavaQueryUtils.findSourceFile(fqn);
        if (sourceFile == null) {
            return "Error: Source file not found for " + fqn;
        }

        JavaSource javaSource = JavaSource.forFileObject(sourceFile);
        if (javaSource == null) {
            return "Error: Could not create JavaSource for " + sourceFile.getPath();
        }

        final AtomicReference<String> sourceCodeRef = new AtomicReference<>();
        
        javaSource.runUserActionTask(new Task<CompilationController>() {
            @Override
            public void run(CompilationController controller) throws Exception {
                controller.toPhase(JavaSource.Phase.RESOLVED);
                CompilationUnitTree cu = controller.getCompilationUnit();
                Trees trees = controller.getTrees();
                SourcePositions sourcePositions = trees.getSourcePositions();
                String fileContent = controller.getText();

                new TreePathScanner<Void, Void>() {
                    @Override
                    public Void visitMethod(MethodTree methodTree, Void p) {
                        if (methodTree.getName().toString().equals(methodName)) {
                            Element methodElement = trees.getElement(getCurrentPath());
                            if (methodElement != null) {
                                List<? extends VariableTree> params = methodTree.getParameters();
                                if (params.size() == parameterTypes.size()) {
                                    boolean match = true;
                                    for (int i = 0; i < params.size(); i++) {
                                        VariableTree param = params.get(i);
                                        Element paramElement = trees.getElement(new com.sun.source.util.TreePath(getCurrentPath(), param));
                                        if (paramElement != null) {
                                            TypeMirror paramType = paramElement.asType();
                                            // Diagnostic confirmed this returns FQN, so we match against the input list
                                            if (!paramType.toString().equals(parameterTypes.get(i))) {
                                                match = false;
                                                break;
                                            }
                                        }
                                    }

                                    if (match) {
                                        long start = sourcePositions.getStartPosition(cu, methodTree);
                                        long end = sourcePositions.getEndPosition(cu, methodTree);
                                        if (start != -1 && end != -1) {
                                            sourceCodeRef.set(fileContent.substring((int) start, (int) end));
                                        }
                                    }
                                }
                            }
                        }
                        return super.visitMethod(methodTree, p);
                    }
                }.scan(cu, null);
            }
        }, true);

        return sourceCodeRef.get();
    }
     
    /**
     * Gets the entire source code of a Java file, limited by a maximum size to prevent context overflow.
     *
     * @param fqn The fully qualified name of the class.
     * @param maxSize The maximum number of characters to return. The content will be truncated if exceeded.
     * @return The full or truncated source code of the file.
     * @throws Exception if an error occurs.
     */
    @AIToolMethod(value = "Gets the entire source code of a Java file, limited by a maximum size to prevent context overflow.", requiresApproval = false)
    public static String getSourceFileContent(
            @AIToolParam("The fully qualified name of the class.") String fqn,
            @AIToolParam("The maximum number of characters to return. The content will be truncated if exceeded.") int maxSize) throws Exception {
        
        String content = NetBeansJavaQueryUtils.getSourceContent(fqn);
        
        if (content == null) {
            return "Error: Source file not found for " + fqn;
        }
        
        if (content.length() > maxSize) {
            String truncatedContent = content.substring(0, maxSize);
            return String.format("--- WARNING: Source content for %s was truncated to %d characters (original size: %d) to prevent context overflow. ---\n%s",
                    fqn, maxSize, content.length(), truncatedContent);
        }
        
        return content;
    }
    
    public static void main(String[] args) throws Exception {
        // Simple test case
        String fqn = "uno.anahata.nb.ai.functions.spi.JavaIntrospection";
        String methodName = "getMembers";
        List<String> params = Collections.singletonList("java.lang.String");
        String source = getSource(fqn, methodName, params);
        System.out.println("SOURCE FOUND:\n" + source);
    }
}
