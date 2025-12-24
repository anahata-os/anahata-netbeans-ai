/* Licensed under the Apache License, Version 2.0 */
package uno.anahata.ai.nb.util;

import java.net.URL;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.classpath.GlobalPathRegistry;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.platform.JavaPlatformManager;
import org.netbeans.api.java.queries.SourceForBinaryQuery;
import org.openide.filesystems.FileObject;
import uno.anahata.ai.nb.model.java.ClassSearchResult;

public class NetBeansJavaQueryUtils {

    /**
     * Finds the FileObject corresponding to the source file for a given FQN.
     * This is a low-level utility for bridging a class name to its source file.
     * @param fqn The fully qualified class name.
     * @return The FileObject of the source file, or null if not found.
     * @throws Exception if an error occurs.
     */
    public static FileObject findSourceFile(String fqn) throws Exception {
        String classAsPath = fqn.replace('.', '/') + ".class";
        ClassSearchResult searchResult = findClassFile(classAsPath);
        if (searchResult == null) {
            return null;
        }

        FileObject root = searchResult.ownerCp.findOwnerRoot(searchResult.classFile);
        if (root == null) {
            return null;
        }

        URL rootUrl = root.toURL();
        SourceForBinaryQuery.Result sfbqResult = SourceForBinaryQuery.findSourceRoots(rootUrl);
        FileObject[] sourceRoots = sfbqResult.getRoots();
        if (sourceRoots.length == 0) {
            return null;
        }

        String sourcePath = fqn.replace('.', '/') + ".java";
        for (FileObject sourceRoot : sourceRoots) {
            FileObject sourceFile = sourceRoot.getFileObject(sourcePath);
            if (sourceFile != null) {
                return sourceFile;
            }
        }
        return null;
    }

    /**
     * Finds the FileObject corresponding to the compiled class file for a given FQN.
     * This is a low-level utility that searches all registered classpaths.
     * @param classAsPath The path to the class file (e.g., java/lang/String.class).
     * @return A ClassSearchResult containing the FileObject and its ClassPath, or null if not found.
     */
    public static ClassSearchResult findClassFile(String classAsPath) {
        // Check project sources first (covers target/classes)
        for (ClassPath cp : GlobalPathRegistry.getDefault().getPaths(ClassPath.SOURCE)) {
            FileObject classFile = cp.findResource(classAsPath);
            if (classFile != null) {
                return new ClassSearchResult(classFile, cp);
            }
        }
        // Then check dependencies
        for (ClassPath cp : GlobalPathRegistry.getDefault().getPaths(ClassPath.EXECUTE)) {
            FileObject classFile = cp.findResource(classAsPath);
            if (classFile != null) {
                return new ClassSearchResult(classFile, cp);
            }
        }
        // Finally, check the JDK
        JavaPlatform defaultPlatform = JavaPlatformManager.getDefault().getDefaultPlatform();
        if (defaultPlatform != null) {
            ClassPath bootstrapLibraries = defaultPlatform.getBootstrapLibraries();
            FileObject classFile = bootstrapLibraries.findResource(classAsPath);
            if (classFile != null) {
                return new ClassSearchResult(classFile, bootstrapLibraries);
            }
        }

        return null;
    }
}
