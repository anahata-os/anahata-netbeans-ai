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
import javax.lang.model.element.TypeElement;
import lombok.Getter;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.JavaSource;
import org.openide.filesystems.FileObject;

/**
 * A "Finder" command object that searches for all members of a given JavaType
 * upon instantiation. It uses a context-aware JavaSource derived from the
 * type's own class or source file.
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
     * @throws Exception if the search fails.
     */
    public JavaMemberSearch(JavaType javaType) throws Exception {
        this.type = javaType;

        // 1. Get the FileObject (could be .java or .class)
        FileObject classFile = javaType.getClassFileObject();
        
        // 2. Create a JavaSource for the file.
        JavaSource javaSource = JavaSource.forFileObject(classFile);
        if (javaSource == null) {
            throw new Exception("Could not create JavaSource for: " + classFile.getPath());
        }

        final List<JavaMember> foundMembers = new ArrayList<>();
        final Exception[] taskException = new Exception[1];

        javaSource.runUserActionTask(controller -> {
            try {
                controller.toPhase(JavaSource.Phase.RESOLVED);
                Element resolvedElement = javaType.getHandle().resolve(controller);

                if (resolvedElement == null) {
                    throw new Exception("Failed to resolve ElementHandle for: " + javaType.getHandle());
                }

                if (!(resolvedElement instanceof TypeElement)) {
                    throw new Exception("Resolved element is not a TypeElement, but a " + resolvedElement.getKind() + " for: " + javaType.getHandle());
                }

                TypeElement typeElement = (TypeElement) resolvedElement;
                URL url = javaType.getUrl();
                
                // Named members (fields, methods, constructors, named inner classes)
                for (Element element : typeElement.getEnclosedElements()) {
                    ElementKind kind = element.getKind();
                    ElementHandle<? extends Element> handle = ElementHandle.create(element);
                    String name = element.getSimpleName().toString();
                    Set<String> modifiers = element.getModifiers().stream()
                            .map(m -> m.name().toLowerCase())
                            .collect(Collectors.toSet());
                    
                    foundMembers.add(new JavaMember(handle, name, kind, url, modifiers));
                }
                
                // Anonymous inner classes - Scan the AST (only available for source files)
                TreePath typePath = controller.getTrees().getPath(typeElement);
                if (typePath != null) {
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
                                    String baseType = node.getIdentifier().toString();
                                    String name = "Anonymous #" + anonCount + " (" + baseType + ")";
                                    Set<String> modifiers = anonTe.getModifiers().stream()
                                            .map(m -> m.name().toLowerCase())
                                            .collect(Collectors.toSet());
                                    foundMembers.add(new JavaMember(handle, name, anonTe.getKind(), url, modifiers));
                                }
                            }
                            return super.visitNewClass(node, p);
                        }
                        
                        @Override
                        public Void visitClass(ClassTree node, Void p) {
                            if (getCurrentPath().equals(typePath)) {
                                return super.visitClass(node, p);
                            }
                            return null;
                        }
                    }.scan(typePath, null);
                }
            } catch (Exception e) {
                taskException[0] = e;
            }
        }, true);

        if (taskException[0] != null) {
            throw taskException[0];
        }

        this.members = Collections.unmodifiableList(foundMembers);
    }
}
