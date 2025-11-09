package uno.anahata.nb.ai.util;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.classpath.GlobalPathRegistry;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.platform.JavaPlatformManager;
import org.netbeans.api.java.queries.JavadocForBinaryQuery;
import org.netbeans.api.java.queries.SourceForBinaryQuery;
import org.openide.filesystems.FileObject;
import uno.anahata.nb.ai.model.java.ClassSearchResult;

public class NetBeansJavaQueryUtils {

    // Regex to find the method signature and its preceding Javadoc block
    private static final Pattern METHOD_PATTERN = Pattern.compile(
            "(/\\*\\*(?:.|\\n)*?\\*/)?\\s*.*\\b(public|protected|private|static|final|abstract|synchronized|transient|volatile)\\s+.*\\b%s\\b\\s*\\([^)]*\\)\\s*(\\{|;)",
            Pattern.CASE_INSENSITIVE | Pattern.DOTALL
    );

    /**
     * Gets the Javadoc URL for a given fully qualified class name by searching all open projects and their dependencies.
     * @param fqn The fully qualified class name (e.g., 'java.lang.String')
     * @return A string containing the found Javadoc roots or an error message.
     */
    public static String getJavadocForClass(String fqn) {
        try {
            String classAsPath = fqn.replace('.', '/') + ".class";
            ClassSearchResult searchResult = findClassFile(classAsPath);

            if (searchResult == null) {
                return "Error: Could not find " + classAsPath + " in any registered project or JDK classpath.";
            }

            FileObject root = searchResult.ownerCp.findOwnerRoot(searchResult.classFile);
            if (root == null) {
                return "Error: Could not find the owner root for " + searchResult.classFile.getPath();
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
                return "No Javadoc found for " + fqn + ". Searched with root: " + rootUrl.toExternalForm();
            }
        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            return "An exception occurred while searching for Javadoc for " + fqn + ":\n" + sw.toString();
        }
    }

    /**
     * Gets the source file URL for a given fully qualified class name by searching all open projects and their dependencies.
     * @param fqn The fully qualified class name (e.g., 'java.lang.String')
     * @return A string containing the found source roots or an error message.
     */
    public static String getSourcesForClass(String fqn) {
        try {
            String classAsPath = fqn.replace('.', '/') + ".class";
            ClassSearchResult searchResult = findClassFile(classAsPath);

            if (searchResult == null) {
                return "Error: Could not find " + classAsPath + " in any registered project or JDK classpath.";
            }

            FileObject root = searchResult.ownerCp.findOwnerRoot(searchResult.classFile);
            if (root == null) {
                return "Error: Could not find the owner root for " + searchResult.classFile.getPath();
            }

            URL rootUrl = root.toURL();
            SourceForBinaryQuery.Result result = SourceForBinaryQuery.findSourceRoots(rootUrl);
            FileObject[] sourceRoots = result.getRoots();

            if (sourceRoots.length > 0) {
                StringBuilder sb = new StringBuilder("Found Source root(s) for " + fqn + ":\n");
                for (FileObject sourceRoot : sourceRoots) {
                    sb.append("- ").append(sourceRoot.toURL().toExternalForm()).append("\n");
                }
                return sb.toString();
            } else {
                return "No Sources found for " + fqn + ". Searched with root: " + rootUrl.toExternalForm();
            }
        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            return "An exception occurred while searching for sources for " + fqn + ":\n" + sw.toString();
        }
    }

    /**
     * Gets the Javadoc for a specific method within a class using regex.
     * @param fqn The fully qualified class name.
     * @param methodName The name of the method.
     * @return The Javadoc content or a message indicating it was not found.
     */
    public static String getJavadocForMethod(
            String fqn,
            String methodName) {
        try {
            String sourceContent = getSourceContent(fqn);
            if (sourceContent == null) {
                return "Error: Could not find source content for " + fqn;
            }

            Pattern pattern = Pattern.compile(String.format(METHOD_PATTERN.pattern(), Pattern.quote(methodName)), METHOD_PATTERN.flags());
            Matcher matcher = pattern.matcher(sourceContent);

            if (matcher.find()) {
                String javadoc = matcher.group(1); // Group 1 is the optional Javadoc block
                if (javadoc != null) {
                    return javadoc.replaceAll("^/\\*\\*|\\*/$", "").replaceAll("^[ \\t]*\\*", "").trim();
                }
            }

            return "No Javadoc found for method '" + methodName + "' in class " + fqn;
        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            return "An exception occurred: " + sw.toString();
        }
    }

    /**
     * Reads the entire content of a source file.
     * @param fqn The fully qualified class name.
     * @return The file content as a string, or null if the source file is not found.
     * @throws Exception if an I/O error occurs.
     */
    public static String getSourceContent(String fqn) throws Exception {
        FileObject sourceFile = findSourceFile(fqn);
        if (sourceFile == null) {
            return null;
        }
        // Use getInputStream() which works for both regular files and files inside JARs.
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(sourceFile.getInputStream(), StandardCharsets.UTF_8))) {
            return reader.lines().collect(Collectors.joining("\n"));
        }
    }

    /**
     * Finds the FileObject corresponding to the source file for a given FQN.
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
     * @param classAsPath The path to the class file (e.g., java/lang/String.class).
     * @return A ClassSearchResult containing the FileObject and its ClassPath, or null if not found.
     */
    public static ClassSearchResult findClassFile(String classAsPath) {
        for (ClassPath cp : GlobalPathRegistry.getDefault().getPaths(ClassPath.SOURCE)) {
            FileObject classFile = cp.findResource(classAsPath);
            if (classFile != null) {
                return new ClassSearchResult(classFile, cp);
            }
        }
        for (ClassPath cp : GlobalPathRegistry.getDefault().getPaths(ClassPath.EXECUTE)) {
            FileObject classFile = cp.findResource(classAsPath);
            if (classFile != null) {
                return new ClassSearchResult(classFile, cp);
            }
        }

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
