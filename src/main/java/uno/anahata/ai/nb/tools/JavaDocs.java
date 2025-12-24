/* Licensed under the Apache License, Version 2.0 */
package uno.anahata.ai.nb.tools;

import com.sun.source.doctree.DocCommentTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.util.DocTrees;
import com.sun.source.util.TreePath;
import com.sun.source.util.TreePathScanner;
import java.net.URL;
import java.util.concurrent.atomic.AtomicReference;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import org.netbeans.api.java.queries.JavadocForBinaryQuery;
import org.netbeans.api.java.source.JavaSource;
import org.openide.filesystems.FileObject;
import uno.anahata.ai.tools.AIToolMethod;
import uno.anahata.ai.tools.AIToolParam;
import uno.anahata.ai.nb.model.java.ClassSearchResult;
import uno.anahata.ai.tools.spi.pojos.TextChunk;
import uno.anahata.ai.nb.util.NetBeansJavaQueryUtils;
import uno.anahata.ai.internal.TextUtils;

/**
 * Provides tools for retrieving Javadoc and source information for classes and methods.
 * @author Anahata
 */
public class JavaDocs {

    /**
     * Gets the Javadoc URL for a given fully qualified class name by searching all open projects and their dependencies.
     * @param fqn The fully qualified class name (e.g., 'java.lang.String')
     * @return A string containing the found Javadoc roots or a message if none were found.
     * @throws Exception if an error occurs during the search.
     */
    @AIToolMethod(value = "Gets the Javadoc URL for a given fully qualified class name by searching all open projects and their dependencies.", requiresApproval = false)
    public static String getJavadocUrlForClass(@AIToolParam("The fully qualified class name (e.g., 'java.lang.String')") String fqn) throws Exception {
        String classAsPath = fqn.replace('.', '/') + ".class";
        ClassSearchResult searchResult = NetBeansJavaQueryUtils.findClassFile(classAsPath);

        if (searchResult == null) {
            throw new Exception("Could not find " + classAsPath + " in any registered project or JDK classpath.");
        }

        FileObject root = searchResult.ownerCp.findOwnerRoot(searchResult.classFile);
        if (root == null) {
            throw new Exception("Could not find the owner root for " + searchResult.classFile.getPath());
        }

        URL rootUrl = root.toURL();
        JavadocForBinaryQuery.Result result = JavadocForBinaryQuery.findJavadoc(rootUrl);
        URL[] javadocRoots = result.getRoots();

        if (javadocRoots.length > 0) {
            StringBuilder sb = new StringBuilder("Found Javadoc root(s) for " + fqn + ":\n");
            for (URL javadocRoot : javadocRoots) {
                sb.append("- ").append(javadocRoot.toExternalForm()).append("\n");
            }
            return sb.toString();
        } else {
            return "No Javadoc URL found for " + fqn + ". Searched with root: " + rootUrl.toExternalForm();
        }
    }

    @AIToolMethod(value = "Gets the Javadoc comment for a specific type (class, interface, enum, inner class) from its source file.", requiresApproval = false)
    public static TextChunk getJavadocForType(
            @AIToolParam("The fully qualified name of the type.") String fqn,
            @AIToolParam("The starting line number (0-based) for pagination.") Integer startIndex,
            @AIToolParam("The number of lines to return.") Integer pageSize,
            @AIToolParam("A regex pattern to filter lines. Can be null or empty to return all lines.") String grepPattern,
            @AIToolParam("The maximum length of each line. Lines longer than this will be truncated. Set to 0 for no limit.") Integer maxLineLength) throws Exception {

        String rawJavadoc = getRawJavadocForType(fqn);
        if (rawJavadoc == null) {
            throw new IllegalStateException("No Javadoc found for type '" + fqn + "'");
        }
        
        return TextUtils.processText(rawJavadoc, startIndex, pageSize, grepPattern, maxLineLength);
    }

    @AIToolMethod(value = "Gets the Javadoc comment for a specific method from its source file.", requiresApproval = false)
    public static TextChunk getJavadocForMethod(
            @AIToolParam("The fully qualified class name.") String fqn,
            @AIToolParam("The name of the method.") String methodName,
            @AIToolParam("The starting line number (0-based) for pagination.") Integer startIndex,
            @AIToolParam("The number of lines to return.") Integer pageSize,
            @AIToolParam("A regex pattern to filter lines. Can be null or empty to return all lines.") String grepPattern,
            @AIToolParam("The maximum length of each line. Lines longer than this will be truncated. Set to 0 for no limit.") Integer maxLineLength) throws Exception {

        String rawJavadoc = getRawJavadocForMethod(fqn, methodName);
        if (rawJavadoc == null) {
            throw new IllegalStateException("No Javadoc found for method '" + methodName + "' in class " + fqn);
        }
        
        return TextUtils.processText(rawJavadoc, startIndex, pageSize, grepPattern, maxLineLength);
    }

    private static String getRawJavadocForType(String fqn) throws Exception {
        FileObject sourceFile = NetBeansJavaQueryUtils.findSourceFile(fqn);
        if (sourceFile == null) {
            throw new Exception("Source file not found for " + fqn);
        }

        JavaSource javaSource = JavaSource.forFileObject(sourceFile);
        if (javaSource == null) {
            throw new Exception("Could not create JavaSource for " + sourceFile.getPath());
        }

        final AtomicReference<String> javadocRef = new AtomicReference<>();
        final AtomicReference<Exception> exceptionRef = new AtomicReference<>();

        javaSource.runUserActionTask(controller -> {
            try {
                controller.toPhase(JavaSource.Phase.PARSED);
                DocTrees docTrees = controller.getDocTrees();

                new TreePathScanner<Void, Void>() {
                    @Override
                    public Void visitClass(ClassTree classTree, Void p) {
                        TreePath path = getCurrentPath();
                        Element element = controller.getTrees().getElement(path);
                        if (element instanceof TypeElement) {
                            TypeElement typeElement = (TypeElement) element;
                            if (typeElement.getQualifiedName().toString().equals(fqn)) {
                                DocCommentTree docCommentTree = docTrees.getDocCommentTree(path);
                                if (docCommentTree != null) {
                                    javadocRef.set(cleanJavadoc(docCommentTree.toString()));
                                }
                            }
                        }
                        return super.visitClass(classTree, p);
                    }
                }.scan(controller.getCompilationUnit(), null);
            } catch (Exception e) {
                exceptionRef.set(e);
            }
        }, true);

        if (exceptionRef.get() != null) {
            throw exceptionRef.get();
        }
        return javadocRef.get();
    }

    private static String getRawJavadocForMethod(String fqn, String methodName) throws Exception {
        FileObject sourceFile = NetBeansJavaQueryUtils.findSourceFile(fqn);
        if (sourceFile == null) {
            throw new Exception("Source file not found for " + fqn);
        }

        JavaSource javaSource = JavaSource.forFileObject(sourceFile);
        if (javaSource == null) {
            throw new Exception("Could not create JavaSource for " + sourceFile.getPath());
        }

        final AtomicReference<String> javadocRef = new AtomicReference<>();
        final AtomicReference<Exception> exceptionRef = new AtomicReference<>();

        javaSource.runUserActionTask(controller -> {
            try {
                controller.toPhase(JavaSource.Phase.PARSED);
                DocTrees docTrees = controller.getDocTrees();

                new TreePathScanner<Void, Void>() {
                    @Override
                    public Void visitMethod(MethodTree methodTree, Void p) {
                        if (methodTree.getName().toString().equals(methodName)) {
                            TreePath path = getCurrentPath();
                            DocCommentTree docCommentTree = docTrees.getDocCommentTree(path);
                            if (docCommentTree != null) {
                                javadocRef.set(cleanJavadoc(docCommentTree.toString()));
                            }
                        }
                        return super.visitMethod(methodTree, p);
                    }
                }.scan(controller.getCompilationUnit(), null);
            } catch (Exception e) {
                exceptionRef.set(e);
            }
        }, true);

        if (exceptionRef.get() != null) {
            throw exceptionRef.get();
        }
        return javadocRef.get();
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