package uno.anahata.nb.ai.mime;

import java.util.HashSet;
import java.util.Set;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

public class LanguageSupport {
    /**
     * Retrieves all supported languages in NetBeans by querying MIME types from the Editors folder.
     * @return Set of human-readable language names.
     */
    public static Set<String> getSupportedLanguages() {
        Set<String> languages = new HashSet<>();

        // Access the Editors folder in the system filesystem
        FileObject editorsRoot = FileUtil.getConfigFile("Editors");
        if (editorsRoot == null) {
            languages.add("Error: Editors folder not found in system filesystem");
            return languages;
        }

        // Iterate through subfolders, each representing a MIME type
        for (FileObject mimeFolder : editorsRoot.getChildren()) {
            String mimeType = mimeFolder.getPath().replaceFirst("^Editors/", ""); // e.g., "text/x-java"
            String language = mimeToLanguageName(mimeType);
            languages.add(language);
        }

        return languages;
    }

    /**
     * Converts a MIME type to a human-readable language name using heuristics.
     * @param mimeType The MIME type (e.g., "text/x-java").
     * @return A human-readable language name (e.g., "Java").
     */
    private static String mimeToLanguageName(String mimeType) {
        if (mimeType == null || mimeType.isEmpty()) {
            return "Unknown";
        }

        // Split MIME type by '/' and take the last part
        String[] parts = mimeType.split("/");
        String lastPart = parts[parts.length - 1];

        // Handle common prefixes like "text/x-" or "application/"
        if (lastPart.startsWith("x-")) {
            lastPart = lastPart.substring(2); // Remove "x-"
        }

        // Clean up and capitalize
        String language = lastPart.replace("-", " ");
        language = language.replace("+", " and ");
        language = capitalizeWords(language);

        // Handle special cases for clarity
        switch (language) {
            case "Java":
                return "Java";
            case "Jsp":
                return "JSP";
            case "Php5":
                return "PHP";
            case "C++":
                return "C++";
            case "Shellscript":
                return "Shell Script";
            case "Ant and Xml":
                return "Ant";
            case "El":
                return "Expression Language (EL)";
            case "Javadoc":
                return "Javadoc";
            case "Properties":
                return "Java Properties";
            case "Ini":
                return "INI";
            case "Json":
                return "JSON";
            case "Css":
                return "CSS";
            case "Javascript":
                return "JavaScript";
            case "Html":
                return "HTML";
            case "Xml":
                return "XML";
            case "Python":
                return "Python";
            case "Ruby":
                return "Ruby";
            case "C":
                return "C";
            case "Sql":
                return "SQL";
            case "Freemarker":
                return "Freemarker";
            case "Groovy":
                return "Groovy";
            case "Yaml":
                return "YAML";
            case "Dockerfile":
                return "Dockerfile";
            case "Markdown":
                return "Markdown";
            case "Plain":
                return "Plain Text";
            default:
                return language;
        }
    }

    /**
     * Capitalizes the first letter of each word in a string.
     * @param str The input string.
     * @return The capitalized string.
     */
    private static String capitalizeWords(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        String[] words = str.split(" ");
        StringBuilder result = new StringBuilder();
        for (String word : words) {
            if (!word.isEmpty()) {
                result.append(Character.toUpperCase(word.charAt(0)))
                      .append(word.substring(1).toLowerCase())
                      .append(" ");
            }
        }
        return result.toString().trim();
    }

    // Example usage
    public static void main(String[] args) {
        Set<String> languages = getSupportedLanguages();
        System.out.println("Supported Languages in NetBeans 27:");
        for (String language : languages) {
            System.out.println("- " + language);
        }
        System.out.println("Total: " + languages.size() + " languages");
    }
}