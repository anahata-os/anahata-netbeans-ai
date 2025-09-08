package uno.anahata.nb.ai;

import com.google.genai.types.Content;
import com.google.genai.types.Part;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import uno.anahata.gemini.GeminiConfigProvider;
import uno.anahata.gemini.functions.spi.ExecuteJavaCode;

public class GeminiConfigProviderImpl implements GeminiConfigProvider {

    private static final String PER_REQUEST_MANUAL;
    private static final String STARTUP_MANUAL;
    private static volatile String dynamicEnvSummary;
    private static final Logger log = Logger.getLogger(GeminiConfigProviderImpl.class.getName());
    private static final String GEMS_DIR_PATH = System.getProperty("user.home") + File.separator + ".netbeans" + File.separator + "Gems";

    static {
        ExecuteJavaCode.setGemsDirPath(GEMS_DIR_PATH);
        STARTUP_MANUAL = loadManual("startup-manual.md");
        PER_REQUEST_MANUAL = loadManual("per-request-manual.md");        
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

    @Override
    public Content getStartupContent() {
        try {
            log.info("Attempting to perform startup awareness via runGem...");
            String awarenessResult = (String) ExecuteJavaCode.runGem("performStartupAwareness.java");
            log.info("Successfully performed startup awareness.");
            
            // CORRECTED: Send the manual first for better UI and model context.
            return Content.fromParts(
                Part.fromText(STARTUP_MANUAL),
                Part.fromText(awarenessResult) 
            );
            
        } catch (Exception e) {
            log.log(Level.SEVERE, "Error performing startup awareness via runGem", e);
            String errorMsg = "Error executing startup awareness Gem: " + e.getMessage();
            // Send the manual first even in case of an error.
            return Content.fromParts(
                Part.fromText(STARTUP_MANUAL),
                Part.fromText(errorMsg)
            );
        }
    }

    private static String computeDynamicEnvSummary() {
        StringBuilder sb = new StringBuilder();
        sb.append("\n\n# Dynamic Environment Details\n");
        sb.append("- System Properties: ").append(System.getProperties().toString()).append("\n");
        sb.append("- Environment variables: ").append(System.getenv().toString()).append("\n");
        sb.append("- ExecuteJavaCode.chatTemp keys: ").append(ExecuteJavaCode.chatTemp.keySet()).append("\n");
        sb.append("- Gem Ids: ").append(ExecuteJavaCode.getGemIds()).append("\n");
        sb.append("- Default Compiler classpath: ").append(ExecuteJavaCode.getDefaultCompilerClasspath()).append("\n");
        return sb.toString();
    }
}
