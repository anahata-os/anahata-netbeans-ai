package uno.anahata.nb.ai.tools;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.classpath.GlobalPathRegistry;
import org.netbeans.api.java.source.ClassIndex;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.openide.filesystems.FileObject;
import uno.anahata.gemini.functions.AIToolMethod;
import uno.anahata.gemini.functions.AIToolParam;
import uno.anahata.nb.ai.model.java.MemberInfo;
import uno.anahata.nb.ai.model.java.TypeInfo;
import uno.anahata.nb.ai.util.NetBeansJavaQueryUtils;

/**
 * Provides robust, API-driven tools for Java type introspection, similar to the NetBeans Navigator.
 * This tool uses the proper NetBeans Java Source APIs to avoid brittle text parsing.
 * @author Anahata
 */
public class JavaIntrospection {

    @AIToolMethod(value = "Gets a list of types that match the given prefix, ordered by relevance (project source, dependencies, JDK).", requiresApproval = false)
    public static List<TypeInfo> getTypes(@AIToolParam("The prefix of the type name to search for (e.g., 'Strin')") String text) {
        Set<TypeInfo> results = new LinkedHashSet<>();
        Set<ClassPath> sourcePaths = GlobalPathRegistry.getDefault().getPaths(ClassPath.SOURCE);
        Set<ClassPath> compilePaths = GlobalPathRegistry.getDefault().getPaths(ClassPath.COMPILE);
        Set<ClassPath> bootPaths = GlobalPathRegistry.getDefault().getPaths(ClassPath.BOOT);

        ClassPath sourceCp = ClassPathSupport.createProxyClassPath(sourcePaths.toArray(new ClassPath[0]));
        ClassPath compileCp = ClassPathSupport.createProxyClassPath(compilePaths.toArray(new ClassPath[0]));
        ClassPath bootCp = ClassPathSupport.createProxyClassPath(bootPaths.toArray(new ClassPath[0]));

        ClasspathInfo cpInfo = ClasspathInfo.create(bootCp, compileCp, sourceCp);

        Set<ElementHandle<TypeElement>> types = cpInfo.getClassIndex().getDeclaredTypes(
                text,
                ClassIndex.NameKind.PREFIX,
                EnumSet.allOf(ClassIndex.SearchScope.class)
        );

        for (ElementHandle<TypeElement> handle : types) {
            String fqn = handle.getQualifiedName();
            String resourceName = handle.getBinaryName().replace('.', '/') + ".class";
            String origin = "Unknown";
            if (sourceCp.findResource(resourceName) != null) {
                origin = "Project Source";
            } else if (compileCp.findResource(resourceName) != null) {
                origin = "Project Dependency";
            } else if (bootCp.findResource(resourceName) != null) {
                origin = "JDK / Platform";
            }
            int lastDot = fqn.lastIndexOf('.');
            String simpleName = lastDot > -1 ? fqn.substring(lastDot + 1) : fqn;
            String packageName = lastDot > -1 ? fqn.substring(0, lastDot) : "";
            results.add(new TypeInfo(fqn, simpleName, packageName, origin));
        }

        return new ArrayList<>(results);
    }

    @AIToolMethod(value = "Gets a list of all members (fields, constructors, methods, inner classes) for a given type.", requiresApproval = false)
    public static List<MemberInfo> getMembers(@AIToolParam("The fully qualified name of the type to inspect.") String fqn) throws Exception {
        FileObject sourceFile = NetBeansJavaQueryUtils.findSourceFile(fqn);
        if (sourceFile == null) {
            // Fallback to reflection if source not found, for JDK classes etc.
            return getMembersByReflection(fqn);
        }

        JavaSource javaSource = JavaSource.forFileObject(sourceFile);
        if (javaSource == null) {
            throw new IllegalStateException("Could not create JavaSource for " + sourceFile.getPath());
        }

        final List<MemberInfo> members = new ArrayList<>();
        javaSource.runUserActionTask(controller -> {
            controller.toPhase(JavaSource.Phase.RESOLVED);
            TypeElement typeElement = controller.getElements().getTypeElement(fqn);
            if (typeElement != null) {
                for (Element element : typeElement.getEnclosedElements()) {
                    String name = element.getSimpleName().toString();
                    String kind = element.getKind().toString();
                    String type;
                    List<String> parameters = new ArrayList<>();

                    if (element.getKind() == ElementKind.CONSTRUCTOR) {
                        name = typeElement.getSimpleName().toString(); // Use class name for constructor
                        ExecutableElement executableElement = (ExecutableElement) element;
                        type = "void"; // Constructors effectively return void
                        for (VariableElement parameter : executableElement.getParameters()) {
                            parameters.add(parameter.asType().toString());
                        }
                    } else if (element.getKind() == ElementKind.METHOD) {
                        ExecutableElement executableElement = (ExecutableElement) element;
                        type = executableElement.getReturnType().toString(); // Get only the return type
                        for (VariableElement parameter : executableElement.getParameters()) {
                            parameters.add(parameter.asType().toString());
                        }
                    } else {
                        type = element.asType().toString(); // For fields, etc.
                    }

                    Set<String> modifiers = element.getModifiers().stream()
                            .map(javax.lang.model.element.Modifier::toString)
                            .collect(Collectors.toSet());

                    members.add(new MemberInfo(name, kind, type, modifiers, parameters));
                }
            }
        }, true);

        if (members.isEmpty()) {
            // If for some reason the JavaSource API failed, try with reflection as a last resort.
            return getMembersByReflection(fqn);
        }

        return members;
    }

    private static List<MemberInfo> getMembersByReflection(String fqn) throws ClassNotFoundException {
        List<MemberInfo> members = new ArrayList<>();
        // Use standard reflection to avoid NetBeans classloader issues (LinkageError)
        Class<?> clazz = Class.forName(fqn);

        // Get Fields
        for (Field field : clazz.getDeclaredFields()) {
            String name = field.getName();
            String kind = "FIELD";
            String type = field.getType().getName();
            Set<String> modifiers = getModifiersAsSet(field.getModifiers());
            members.add(new MemberInfo(name, kind, type, modifiers, new ArrayList<>()));
        }

        // Get Constructors
        for (Constructor<?> constructor : clazz.getDeclaredConstructors()) {
            String name = constructor.getName();
            String kind = "CONSTRUCTOR";
            String type = "void"; // Constructors don't have a return type
            Set<String> modifiers = getModifiersAsSet(constructor.getModifiers());
            List<String> parameters = new ArrayList<>();
            for (Class<?> paramType : constructor.getParameterTypes()) {
                parameters.add(paramType.getName());
            }
            members.add(new MemberInfo(name, kind, type, modifiers, parameters));
        }

        // Get Methods
        for (Method method : clazz.getDeclaredMethods()) {
            String name = method.getName();
            String kind = "METHOD";
            String type = method.getReturnType().getName();
            Set<String> modifiers = getModifiersAsSet(method.getModifiers());
            List<String> parameters = new ArrayList<>();
            for (Class<?> paramType : method.getParameterTypes()) {
                parameters.add(paramType.getName());
            }
            members.add(new MemberInfo(name, kind, type, modifiers, parameters));
        }
        
        // Get Inner Classes
        for (Class<?> innerClass : clazz.getDeclaredClasses()) {
            String name = innerClass.getSimpleName();
            String kind = "CLASS";
            String type = innerClass.getName();
            Set<String> modifiers = getModifiersAsSet(innerClass.getModifiers());
            members.add(new MemberInfo(name, kind, type, modifiers, new ArrayList<>()));
        }

        return members;
    }
    
    private static Set<String> getModifiersAsSet(int mod) {
        return Arrays.stream(Modifier.toString(mod).split(" "))
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toSet());
    }
}
