package uno.anahata.nb.ai.tools;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.netbeans.api.java.queries.JavadocForBinaryQuery;
import org.openide.filesystems.FileObject;
import uno.anahata.gemini.functions.AIToolMethod;
import uno.anahata.gemini.functions.AIToolParam;
import uno.anahata.nb.ai.util.NetBeansJavaQueryUtils;

/**
 * Provides tools for retrieving Javadoc and source information for classes and methods.
 * @author Anahata
 */
public class JavaDocs {

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
    @AIToolMethod(value = "Gets the Javadoc URL for a given fully qualified class name by searching all open projects and their dependencies.", requiresApproval = false)
    public static String getJavadocForClass(@AIToolParam("The fully qualified class name (e.g., 'java.lang.String')") String fqn) {
        try {
            String classAsPath = fqn.replace('.', '/') + ".class";
            NetBeansJavaQueryUtils.ClassSearchResult searchResult = NetBeansJavaQueryUtils.findClassFile(classAsPath);

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
     * Gets the Javadoc for a specific method within a class using regex.
     * @param fqn The fully qualified class name.
     * @param methodName The name of the method.
     * @return The Javadoc content or a message indicating it was not found.
     */
    @AIToolMethod(value = "Gets the Javadoc for a specific method within a class using regex.", requiresApproval = false)
    public static String getJavadocForMethod(
            @AIToolParam("The fully qualified class name.") String fqn,
            @AIToolParam("The name of the method.") String methodName) {
        try {
            String sourceContent = NetBeansJavaQueryUtils.getSourceContent(fqn);
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
}
