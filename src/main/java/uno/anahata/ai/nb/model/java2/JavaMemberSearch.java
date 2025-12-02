/*
 * Licensed under the Anahata Software License (ASL) v 108. See the LICENSE file for details. Fora Bara!
 */
package uno.anahata.ai.nb.model.java2;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import lombok.Getter;
import lombok.SneakyThrows;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.classpath.GlobalPathRegistry;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;

/**
 * A "Finder" command object that searches for all members of a given JavaType
 * upon instantiation. It uses a universal, ClassIndex-based approach that works
 * for all types of classes (project source, JDK, and Maven dependencies).
 *
 * @author pablo
 */
@Getter
public class JavaMemberSearch {

    private final JavaType type;
    private final List<JavaMember> members;

    @SneakyThrows
    public JavaMemberSearch(JavaType javaType) {
        this.type = javaType;

        // 1. Build a complete, global ClasspathInfo from the IDE's state.
        Set<ClassPath> bootPaths = GlobalPathRegistry.getDefault().getPaths(ClassPath.BOOT);
        Set<ClassPath> compilePaths = GlobalPathRegistry.getDefault().getPaths(ClassPath.COMPILE);
        Set<ClassPath> sourcePaths = GlobalPathRegistry.getDefault().getPaths(ClassPath.SOURCE);

        ClassPath globalBootCp = ClassPathSupport.createProxyClassPath(bootPaths.toArray(new ClassPath[0]));
        ClassPath globalCompileCp = ClassPathSupport.createProxyClassPath(compilePaths.toArray(new ClassPath[0]));
        ClassPath globalSourceCp = ClassPathSupport.createProxyClassPath(sourcePaths.toArray(new ClassPath[0]));

        ClasspathInfo cpInfo = ClasspathInfo.create(globalBootCp, globalCompileCp, globalSourceCp);

        // 2. Use a "virtual" JavaSource representing the entire classpath to resolve the handle.
        JavaSource javaSource = JavaSource.create(cpInfo);
        if (javaSource == null) {
            throw new Exception("Could not create a global JavaSource");
        }

        final List<JavaMember> foundMembers = new ArrayList<>();
        javaSource.runUserActionTask(controller -> {
            controller.toPhase(JavaSource.Phase.RESOLVED);
            Element resolvedElement = javaType.getHandle().resolve(controller);

            if (resolvedElement == null) {
                throw new Exception("Failed to resolve ElementHandle for " + javaType.getHandle().getQualifiedName() + ". The class might not be on the global classpath.");
            }

            if (!(resolvedElement instanceof TypeElement)) {
                throw new IllegalStateException("Resolved element is not a TypeElement, but a " + resolvedElement.getKind() + ". This indicates a logic error.");
            }

            TypeElement typeElement = (TypeElement) resolvedElement;
            for (Element element : typeElement.getEnclosedElements()) {
                ElementKind kind = element.getKind();
                ElementHandle<? extends Element> handle = ElementHandle.create(element);
                String name = element.getSimpleName().toString();
                String details = createMemberDetails(element);
                foundMembers.add(new JavaMember(handle, name, kind, details));
            }
        }, true);

        this.members = Collections.unmodifiableList(foundMembers);
    }

    private static String createMemberDetails(Element element) {
        // This logic can be expanded to provide more detailed signatures.
        return element.toString();
    }
}
