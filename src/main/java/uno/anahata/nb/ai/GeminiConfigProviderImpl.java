
package uno.anahata.nb.ai;

import com.google.genai.types.Content;
import com.google.genai.types.Part;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;
import uno.anahata.gemini.GeminiConfigProvider;
import uno.anahata.gemini.functions.spi.ExecuteJavaCode;

public class GeminiConfigProviderImpl implements GeminiConfigProvider {

    private static final String PER_REQUEST_MANUAL;
    private static final String STARTUP_MANUAL;
    private static volatile String dynamicEnvSummary;

    static {
        PER_REQUEST_MANUAL = loadManual("per-request-manual.md");
        STARTUP_MANUAL = loadManual("startup-manual.md");
        dynamicEnvSummary = computeDynamicEnvSummary();
    }

    private static String loadManual(String resourceName) {
        try (InputStream is = GeminiConfigProviderImpl.class.getResourceAsStream(resourceName)) {
            if (is == null) {
                return "Error: Could not find resource " + resourceName;
            }
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
                return reader.lines().collect(Collectors.joining("\n"));
            }
        } catch (Exception e) {
            return "Error loading " + resourceName + ": " + e.getMessage();
        }
    }

    @Override
    public Content getSystemInstruction() {
        String processedManual = PER_REQUEST_MANUAL.replace("${netbeans.productversion}", System.getProperty("netbeans.productversion", "an unknown version of NetBeans"));
        
        return Content.fromParts(
                Part.fromText(processedManual),
                Part.fromText(dynamicEnvSummary)
        );
    }
    
    //@Override
    public Content getStartupContent() {
        return Content.fromParts(Part.fromText("Hi, i just launched started netbeans"), Part.fromText(STARTUP_MANUAL));
    }

    private static String computeDynamicEnvSummary() {
        StringBuilder sb = new StringBuilder();
        sb.append("\n\n# Dynamic Environment Details\n");
        sb.append("- System Properties: ").append(System.getProperties().toString()).append("\n");
        sb.append("- Environment variables: ").append(System.getenv().toString()).append("\n");
        sb.append("- ExecuteJavaCode.chatTemp keys: ").append(ExecuteJavaCode.chatTemp.keySet()).append("\n");
        sb.append("- Default Compiler classpath: ").append(ExecuteJavaCode.getDefaultCompilerClasspath()).append("\n");
        return sb.toString();
    }
}
