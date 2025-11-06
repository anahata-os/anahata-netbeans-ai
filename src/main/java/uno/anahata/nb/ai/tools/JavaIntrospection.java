package uno.anahata.nb.ai.tools;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import javax.lang.model.element.TypeElement;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.classpath.GlobalPathRegistry;
import org.netbeans.api.java.source.ClassIndex;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import uno.anahata.gemini.functions.AIToolMethod;
import uno.anahata.gemini.functions.AIToolParam;
import uno.anahata.nb.ai.model.java.MemberInfo;
import uno.anahata.nb.ai.model.java.TypeInfo;

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
        List<MemberInfo> members = new ArrayList<>();
        // Use standard reflection to avoid NetBeans classloader issues (LinkageError)
        Class<?> clazz = Class.forName(fqn);

        // Get Fields
        for (Field field : clazz.getDeclaredFields()) {
            String name = field.getName();
            String kind = "FIELD";
            String type = field.getType().getName();
            Set<String> modifiers = Arrays.stream(java.lang.reflect.Modifier.toString(field.getModifiers()).split(" ")).collect(Collectors.toSet());
            members.add(new MemberInfo(name, kind, type, modifiers, new ArrayList<>()));
        }

        // Get Constructors
        for (Constructor<?> constructor : clazz.getDeclaredConstructors()) {
            String name = constructor.getName();
            String kind = "CONSTRUCTOR";
            String type = "void"; // Constructors don't have a return type
            Set<String> modifiers = Arrays.stream(java.lang.reflect.Modifier.toString(constructor.getModifiers()).split(" ")).collect(Collectors.toSet());
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
            Set<String> modifiers = Arrays.stream(java.lang.reflect.Modifier.toString(method.getModifiers()).split(" ")).collect(Collectors.toSet());
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
            Set<String> modifiers = Arrays.stream(java.lang.reflect.Modifier.toString(innerClass.getModifiers()).split(" ")).collect(Collectors.toSet());
            members.add(new MemberInfo(name, kind, type, modifiers, new ArrayList<>()));
        }

        return members;
    }
}
