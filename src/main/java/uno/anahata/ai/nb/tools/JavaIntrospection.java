/* Licensed under the Apache License, Version 2.0 */
package uno.anahata.ai.nb.tools;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashSet;
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
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.jumpto.type.TypeProviderAccessor;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.netbeans.spi.jumpto.type.SearchType;
import org.netbeans.spi.jumpto.type.TypeDescriptor;
import org.netbeans.spi.jumpto.type.TypeProvider;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;
import uno.anahata.ai.nb.model.java.MemberInfo;
import uno.anahata.ai.nb.model.java.MemberSearchResultPage;
import uno.anahata.ai.nb.model.java.PackageSearchResultPage;
import uno.anahata.ai.nb.model.java.TypeInfo;
import uno.anahata.ai.nb.model.java.TypeKind;
import uno.anahata.ai.nb.model.java.TypeSearchResultPage;
import uno.anahata.ai.nb.util.NetBeansJavaQueryUtils;
import uno.anahata.ai.tools.AIToolMethod;
import uno.anahata.ai.tools.AIToolParam;
import org.netbeans.api.project.ui.OpenProjects;
import org.openide.filesystems.FileUtil;

/**
 * Provides robust, API-driven tools for Java type introspection, similar to the NetBeans Navigator.
 * This tool uses the proper NetBeans Java Source APIs to avoid brittle text parsing.
 * @author Anahata
 */
public class JavaIntrospection {

    /**
     * Gets detailed information about a single, specific Java type.
     * @param fqn The fully qualified name of the type to inspect.
     * @return a TypeInfo object containing the type's details.
     */
    @AIToolMethod("Gets detailed information about a single, specific Java type.")
    public static TypeInfo getTypeInfo(@AIToolParam("The fully qualified name of the type to inspect.") String fqn) {
        ClasspathInfo cpInfo = getClasspathInfo();
        // The kind doesn't matter for the lookup, so we can use a common one.
        ElementHandle<TypeElement> handle = ElementHandle.createTypeElementHandle(ElementKind.CLASS, fqn);
        if (handle == null) {
            return null;
        }
        int lastDot = fqn.lastIndexOf('.');
        String simpleName = lastDot > -1 ? fqn.substring(lastDot + 1) : fqn;
        String packageName = lastDot > -1 ? fqn.substring(0, lastDot) : "";
        String origin = findOrigin(cpInfo, handle);
        return new TypeInfo(fqn, simpleName, packageName, origin);
    }

    /**
     * Gets a paginated list of all members (fields, constructors, methods, inner classes) for a given type.
     * @param fqn The fully qualified name of the type to inspect.
     * @param startIndex The starting index (0-based) for pagination.
     * @param pageSize The maximum number of results to return per page.
     * @return a MemberSearchResultPage containing the members.
     * @throws Exception if an error occurs.
     */
    @AIToolMethod(value = "Gets a paginated list of all members (fields, constructors, methods, inner classes) for a given type.", requiresApproval = false)
    public static MemberSearchResultPage getMembers(
            @AIToolParam("The fully qualified name of the type to inspect.") String fqn,
            @AIToolParam("The starting index (0-based) for pagination.") Integer startIndex,
            @AIToolParam("The maximum number of results to return per page.") Integer pageSize) throws Exception {

        int start = startIndex != null ? startIndex : 0;
        int size = pageSize != null ? pageSize : 100;

        FileObject sourceFile = NetBeansJavaQueryUtils.findSourceFile(fqn);
        if (sourceFile == null) {
            return getMembersByReflection(fqn, start, size);
        }

        JavaSource javaSource = JavaSource.forFileObject(sourceFile);
        if (javaSource == null) {
            // Fallback to reflection if JavaSource fails
            return getMembersByReflection(fqn, start, size);
        }

        final List<MemberInfo> allMembers = new ArrayList<>();
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
                        name = typeElement.getSimpleName().toString();
                        ExecutableElement ee = (ExecutableElement) element;
                        type = "void"; // Constructors don't have a return type in this context
                        for (VariableElement p : ee.getParameters()) {
                            parameters.add(p.asType().toString());
                        }
                    } else if (element.getKind() == ElementKind.METHOD) {
                        ExecutableElement ee = (ExecutableElement) element;
                        type = ee.getReturnType().toString();
                        for (VariableElement p : ee.getParameters()) {
                            parameters.add(p.asType().toString());
                        }
                    } else {
                        type = element.asType().toString();
                    }
                    
                    // Correctly format inner class names
                    if (element.getKind().isClass() || element.getKind().isInterface()) {
                        name = typeElement.getQualifiedName().toString().replace('$', '.') + "." + name;
                    }
                    
                    type = type.replace('$', '.');


                    Set<String> modifiers = element.getModifiers().stream()
                            .map(javax.lang.model.element.Modifier::toString)
                            .collect(Collectors.toSet());

                    allMembers.add(new MemberInfo(name, kind, type, modifiers, parameters));
                }
            }
        }, true);

        if (allMembers.isEmpty()) {
            // Fallback if no members were found via JavaSource (e.g., for binary classes)
            return getMembersByReflection(fqn, start, size);
        }
        
        int totalCount = allMembers.size();
        List<MemberInfo> page = allMembers.stream()
                .sorted((m1, m2) -> m1.name().compareTo(m2.name()))
                .skip(start)
                .limit(size)
                .collect(Collectors.toList());

        return new MemberSearchResultPage(start, totalCount, page);
    }

    /**
     * Finds all subpackages within a given parent package.
     * @param parentPackage The fully qualified name of the parent package.
     * @param recursive If true, the search will be recursive.
     * @param startIndex The starting index (0-based) for pagination.
     * @param pageSize The maximum number of results to return per page.
     * @return a PackageSearchResultPage containing the subpackages.
     */
    @AIToolMethod("Finds all subpackages within a given parent package.")
    public static PackageSearchResultPage findSubpackages(
            @AIToolParam("The fully qualified name of the parent package (e.g., 'java.util'). Use an empty string to find top-level packages.") String parentPackage,
            @AIToolParam("If true, the search will be recursive, finding all subpackages at any depth.") boolean recursive,
            @AIToolParam("The starting index (0-based) for pagination.") Integer startIndex,
            @AIToolParam("The maximum number of results to return per page.") Integer pageSize) {

        int start = startIndex != null ? startIndex : 0;
        int size = pageSize != null ? pageSize : 100;

        ClasspathInfo cpInfo = getClasspathInfo();
        Set<String> packages = cpInfo.getClassIndex().getPackageNames("", false, EnumSet.allOf(ClassIndex.SearchScope.class));

        String prefix = parentPackage.isEmpty() ? "" : parentPackage + ".";

        List<String> results = packages.stream()
                .filter(p -> p.startsWith(prefix) && !p.equals(parentPackage))
                .filter(p -> {
                    if (recursive) {
                        return true;
                    }
                    String remainder = p.substring(prefix.length());
                    return !remainder.contains(".");
                })
                .distinct()
                .sorted()
                .collect(Collectors.toList());

        int totalCount = results.size();
        List<String> page = results.stream().skip(start).limit(size).collect(Collectors.toList());

        return new PackageSearchResultPage(start, totalCount, page);
    }

    /**
     * Finds all types within a given package, with an option for recursive search.
     * @param packageName The fully qualified name of the package to search.
     * @param typeKind The kind of type to search for.
     * @param recursive If true, the search will include all subpackages.
     * @param startIndex The starting index (0-based) for pagination.
     * @param pageSize The maximum number of results to return per page.
     * @return a TypeSearchResultPage containing the found types.
     */
    @AIToolMethod("Finds all types within a given package, with an option for recursive search.")
    public static TypeSearchResultPage findTypesInPackage(
            @AIToolParam("The fully qualified name of the package to search (e.g., 'java.util').") String packageName,
            @AIToolParam("The kind of type to search for.") TypeKind typeKind,
            @AIToolParam("If true, the search will include all subpackages.") boolean recursive,
            @AIToolParam("The starting index (0-based) for pagination.") Integer startIndex,
            @AIToolParam("The maximum number of results to return per page.") Integer pageSize) {

        int start = startIndex != null ? startIndex : 0;
        int size = pageSize != null ? pageSize : 100;

        ClasspathInfo cpInfo = getClasspathInfo();

        Set<ElementHandle<TypeElement>> declaredTypes = cpInfo.getClassIndex().getDeclaredTypes(
                "", ClassIndex.NameKind.PREFIX, EnumSet.allOf(ClassIndex.SearchScope.class));

        List<TypeInfo> allMatchingTypes = declaredTypes.stream()
                .filter(handle -> {
                    String fqn = handle.getQualifiedName();
                    int lastDot = fqn.lastIndexOf('.');
                    String pkg = lastDot > -1 ? fqn.substring(0, lastDot) : "";
                    if (recursive) {
                        return pkg.startsWith(packageName);
                    } else {
                        return pkg.equals(packageName);
                    }
                })
                .filter(handle -> matchesTypeKind(handle.getKind(), typeKind))
                .map(handle -> {
                    String fqn = handle.getQualifiedName();
                    int lastDot = fqn.lastIndexOf('.');
                    String simpleName = lastDot > -1 ? fqn.substring(lastDot + 1) : fqn;
                    String pkg = lastDot > -1 ? fqn.substring(0, lastDot) : "";
                    String origin = findOrigin(cpInfo, handle);
                    return new TypeInfo(fqn, simpleName, pkg, origin);
                })
                .distinct()
                .sorted((t1, t2) -> t1.simpleName().compareTo(t2.simpleName()))
                .collect(Collectors.toList());

        int totalCount = allMatchingTypes.size();
        List<TypeInfo> page = allMatchingTypes.stream().skip(start).limit(size).collect(Collectors.toList());

        return new TypeSearchResultPage(start, totalCount, page);
    }

    /**
     * Searches for types across all classpaths by simple name.
     * @param simpleNameQuery The simple name query.
     * @param typeKind The kind of type to search for.
     * @param caseSensitive If true, the search will be case-sensitive.
     * @param preferOpenProjects If true, results from open projects will be listed first.
     * @param startIndex The starting index (0-based) for pagination.
     * @param pageSize The maximum number of results to return per page.
     * @return a TypeSearchResultPage containing the found types.
     */
    //@AIToolMethod("Searches for types across all classpaths by simple name.")
    public static TypeSearchResultPage searchTypesByName(
            @AIToolParam("The simple name query. Wildcards '*' and '?' are supported for REGEXP search. Otherwise, CAMEL_CASE is used.") String simpleNameQuery,
            @AIToolParam("The kind of type to search for.") TypeKind typeKind,
            @AIToolParam("If true, the search will be case-sensitive.") boolean caseSensitive,
            @AIToolParam("If true, results from open projects will be listed first.") boolean preferOpenProjects,
            @AIToolParam("The starting index (0-based) for pagination.") Integer startIndex,
            @AIToolParam("The maximum number of results to return per page.") Integer pageSize) {

        int start = startIndex != null ? startIndex : 0;
        int size = pageSize != null ? pageSize : 100;

        ClasspathInfo cpInfo = getClasspathInfo();

        Set<ElementHandle<TypeElement>> declaredTypes = cpInfo.getClassIndex().getDeclaredTypes(
                simpleNameQuery,
                ClassIndex.NameKind.CAMEL_CASE,
                EnumSet.allOf(ClassIndex.SearchScope.class)
        );

        List<TypeInfo> allMatchingTypes = declaredTypes.stream()
                .filter(handle -> matchesTypeKind(handle.getKind(), typeKind))
                .map(handle -> {
                    String fqn = handle.getQualifiedName();
                    int lastDot = fqn.lastIndexOf('.');
                    String simpleName = lastDot > -1 ? fqn.substring(lastDot + 1) : fqn;
                    String pkg = lastDot > -1 ? fqn.substring(0, lastDot) : "";
                    String origin = findOrigin(cpInfo, handle);
                    return new TypeInfo(fqn, simpleName, pkg, origin);
                })
                .distinct()
                .sorted((t1, t2) -> t1.simpleName().compareTo(t2.simpleName()))
                .collect(Collectors.toList());

        int totalCount = allMatchingTypes.size();
        List<TypeInfo> page = allMatchingTypes.stream().skip(start).limit(size).collect(Collectors.toList());

        return new TypeSearchResultPage(start, totalCount, page);
    }

    // --- Private Helper Methods ---
    private static boolean matchesTypeKind(ElementKind elementKind, TypeKind queryKind) {
        if (queryKind == TypeKind.ALL) {
            // isClass() is true for CLASS, ENUM, RECORD, and ANNOTATION_TYPE
            return elementKind.isClass() || elementKind.isInterface();
        }
        switch (queryKind) {
            case CLASS:         return elementKind == ElementKind.CLASS;
            case INTERFACE:     return elementKind == ElementKind.INTERFACE;
            case ENUM:          return elementKind == ElementKind.ENUM;
            case RECORD:        return elementKind == ElementKind.RECORD;
            case ANNOTATION_TYPE: return elementKind == ElementKind.ANNOTATION_TYPE;
            default:            return false;
        }
    }

    private static ClasspathInfo getClasspathInfo() {
        Set<ClassPath> sourcePaths = GlobalPathRegistry.getDefault().getPaths(ClassPath.SOURCE);
        Set<ClassPath> compilePaths = GlobalPathRegistry.getDefault().getPaths(ClassPath.COMPILE);
        Set<ClassPath> bootPaths = GlobalPathRegistry.getDefault().getPaths(ClassPath.BOOT);
        ClassPath sourceCp = ClassPathSupport.createProxyClassPath(sourcePaths.toArray(new ClassPath[0]));
        ClassPath compileCp = ClassPathSupport.createProxyClassPath(compilePaths.toArray(new ClassPath[0]));
        ClassPath bootCp = ClassPathSupport.createProxyClassPath(bootPaths.toArray(new ClassPath[0]));
        return ClasspathInfo.create(bootCp, compileCp, sourceCp);
    }

    private static String findOrigin(ClasspathInfo cpInfo, ElementHandle<TypeElement> handle) {
        String resourceName = handle.getBinaryName().replace('.', '/') + ".class";
        if (cpInfo.getClassPath(ClasspathInfo.PathKind.SOURCE).findResource(resourceName) != null) {
            return "Project Source";
        } else if (cpInfo.getClassPath(ClasspathInfo.PathKind.COMPILE).findResource(resourceName) != null) {
            return "Project Dependency";
        } else if (cpInfo.getClassPath(ClasspathInfo.PathKind.BOOT).findResource(resourceName) != null) {
            return "JDK / Platform";
        }
        return "Unknown";
    }

    private static MemberSearchResultPage getMembersByReflection(String fqn, int start, int size) throws ClassNotFoundException {
        List<MemberInfo> allMembers = new ArrayList<>();
        Class<?> clazz;
        try {
            clazz = Class.forName(fqn);
        } catch (ClassNotFoundException e) {
            // Return an empty page if the class cannot be found by reflection
            return new MemberSearchResultPage(start, 0, Collections.emptyList());
        }

        for (Field field : clazz.getDeclaredFields()) {
            allMembers.add(new MemberInfo(field.getName(), "FIELD", field.getType().getName(), getModifiersAsSet(field.getModifiers()), new ArrayList<>()));
        }
        for (Constructor<?> c : clazz.getDeclaredConstructors()) {
            List<String> params = Arrays.stream(c.getParameterTypes()).map(Class::getName).collect(Collectors.toList());
            allMembers.add(new MemberInfo(c.getName().replace('$', '.'), "CONSTRUCTOR", "void", getModifiersAsSet(c.getModifiers()), params));
        }
        for (Method method : clazz.getDeclaredMethods()) {
            List<String> params = Arrays.stream(method.getParameterTypes()).map(Class::getName).collect(Collectors.toList());
            allMembers.add(new MemberInfo(method.getName(), "METHOD", method.getReturnType().getName().replace('$', '.'), getModifiersAsSet(method.getModifiers()), params));
        }
        for (Class<?> innerClass : clazz.getDeclaredClasses()) {
            allMembers.add(new MemberInfo(innerClass.getName().replace('$', '.'), "CLASS", innerClass.getName().replace('$', '.'), getModifiersAsSet(innerClass.getModifiers()), new ArrayList<>()));
        }
        
        int totalCount = allMembers.size();
        List<MemberInfo> page = allMembers.stream()
                .sorted((m1, m2) -> m1.name().compareTo(m2.name()))
                .skip(start)
                .limit(size)
                .collect(Collectors.toList());

        return new MemberSearchResultPage(start, totalCount, page);
    }

    private static Set<String> getModifiersAsSet(int mod) {
        return Arrays.stream(Modifier.toString(mod).split(" ")).filter(s -> !s.isEmpty()).collect(Collectors.toSet());
    }
}