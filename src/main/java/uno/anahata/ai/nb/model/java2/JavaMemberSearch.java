/* Licensed under the Apache License, Version 2.0 */
package uno.anahata.ai.nb.model.java2;

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.NewClassTree;
import com.sun.source.util.TreePath;
import com.sun.source.util.TreePathScanner;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import lombok.Getter;
import lombok.SneakyThrows;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.JavaSource;
import org.openide.filesystems.FileObject;

/**
 * A "Finder" command object that searches for all members of a given JavaType
 * upon instantiation. It uses a context-aware ClasspathInfo derived from the
 * type's own class file.
 *
 * @author anahata
 */
@Getter
public class JavaMemberSearch {

    private final JavaType type;
    private final List<JavaMember> members;

    /**
     * Constructs a new JavaMemberSearch and performs the search for members of the given JavaType.
     * @param javaType the type to search members for.
     */
    @SneakyThrows
    public JavaMemberSearch(JavaType javaType) {
        this.type = javaType;

        // 1. Build a context-aware ClasspathInfo from the type's own class file.
        FileObject classFile = javaType.getClassFileObject();
        ClasspathInfo cpInfo = ClasspathInfo.create(classFile);

        // 2. Use a JavaSource to resolve the handle and inspect the structure.
        JavaSource javaSource = JavaSource.create(cpInfo);
        if (javaSource == null) {
            throw new Exception("Could not create JavaSource for: " + classFile.getPath());
        }

        final List<JavaMember> foundMembers = new ArrayList<>();
        javaSource.runUserActionTask(controller -> {
            controller.toPhase(JavaSource.Phase.RESOLVED);
            Element resolvedElement = javaType.getHandle().resolve(controller);

            if (resolvedElement == null) {
                throw new Exception("Failed to resolve ElementHandle for " + javaType.getHandle());
            }

            if (!(resolvedElement instanceof TypeElement)) {
                throw new IllegalStateException("Resolved element is not a TypeElement, but a " + resolvedElement.getKind());
            }

            TypeElement typeElement = (TypeElement) resolvedElement;
            URL url = javaType.getUrl();
            
            // Named members (fields, methods, constructors, named inner classes)
            for (Element element : typeElement.getEnclosedElements()) {
                ElementKind kind = element.getKind();
                ElementHandle<? extends Element> handle = ElementHandle.create(element);
                String name = element.getSimpleName().toString();
                String details = createMemberDetails(element);
                Set<String> modifiers = element.getModifiers().stream()
                        .map(m -> m.name().toLowerCase())
                        .collect(Collectors.toSet());
                
                foundMembers.add(new JavaMember(handle, name, kind, details, url, modifiers));
            }
            
            // Anonymous inner classes
            ClassTree classTree = controller.getTrees().getTree(typeElement);
            if (classTree != null) {
                TreePath classPath = controller.getTrees().getPath(controller.getCompilationUnit(), classTree);
                new TreePathScanner<Void, Void>() {
                    private int anonCount = 0;
                    @Override
                    public Void visitNewClass(NewClassTree node, Void p) {
                        if (node.getClassBody() != null) {
                            anonCount++;
                            TreePath bodyPath = new TreePath(getCurrentPath(), node.getClassBody());
                            Element anonElement = controller.getTrees().getElement(bodyPath);
                            if (anonElement instanceof TypeElement anonTe) {
                                ElementHandle<TypeElement> handle = ElementHandle.create(anonTe);
                                String name = "Anonymous #" + anonCount + " (extends " + node.getIdentifier() + ")";
                                Set<String> modifiers = anonTe.getModifiers().stream()
                                        .map(m -> m.name().toLowerCase())
                                        .collect(Collectors.toSet());
                                foundMembers.add(new JavaMember(handle, name, anonTe.getKind(), anonTe.toString(), url, modifiers));
                            }
                        }
                        return super.visitNewClass(node, p);
                    }
                }.scan(classPath, null);
            }
        }, true);

        this.members = Collections.unmodifiableList(foundMembers);
    }

    private static String createMemberDetails(Element element) {
        if (element instanceof ExecutableElement ee) {
            String params = ee.getParameters().stream()
                    .map(p -> p.asType().toString())
                    .collect(Collectors.joining(", "));
            return "(" + params + ") : " + ee.getReturnType().toString();
        } else if (element instanceof VariableElement ve) {
            return ": " + ve.asType().toString();
        }
        return element.toString();
    }
}
