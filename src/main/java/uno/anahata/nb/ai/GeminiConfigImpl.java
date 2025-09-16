package uno.anahata.nb.ai;

import com.google.genai.types.Content;
import com.google.genai.types.Part;
import com.google.gson.Gson;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import uno.anahata.gemini.GeminiAPI;
import uno.anahata.gemini.functions.spi.LocalFiles;
import uno.anahata.gemini.functions.spi.RunningJVM;
import uno.anahata.gemini.functions.GeminiFunctionPrompter;
import uno.anahata.gemini.GeminiConfig;
import uno.anahata.nb.ai.functions.spi.Workspace;

public class GeminiConfigImpl extends GeminiConfig {
    
    private GeminiAPI api = new GeminiAPI();

    private static final String PER_REQUEST_MANUAL;
    private static final String STARTUP_MANUAL;
    private static volatile String dynamicEnvSummary;
    private static final Logger log = Logger.getLogger(GeminiConfigImpl.class.getName());
    private static final String PLUGINW_WORK_DIR_PATH = System.getProperty("user.home") + File.separator + ".anahata" + File.separator + "netbeans-ai";
    private static File workDir;

    static {
        workDir = new File(PLUGINW_WORK_DIR_PATH);
        if (!workDir.exists()) {
            workDir.mkdirs();
            log.info("work.dir created " + workDir);
        } else if (!workDir.isDirectory()) { 
            throw new RuntimeException("work.dir is not a directory: " + workDir);
        } else {
            log.info("work.dir initialized " + workDir);
        }
        
        STARTUP_MANUAL = loadManual("startup-manual.md");
        
        PER_REQUEST_MANUAL = loadManual("per-request-manual.md");
        
        dynamicEnvSummary = computeDynamicEnvSummary();
    }
    
    
    /**
     * 
     * @param resourceName
     * @return 
     */
    private static String loadManual(String resourceName) {
        try (InputStream is = GeminiConfigImpl.class.getResourceAsStream(resourceName)) {
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
    public GeminiAPI getApi() {
        return api;
    }

    
    @Override
    public Content getSystemInstruction() {
        String processedManual = PER_REQUEST_MANUAL
                .replace("${netbeans.productversion}", System.getProperty("netbeans.productversion", "an unknown version of NetBeans"))
                .replace("${work.dir}", PLUGINW_WORK_DIR_PATH);

        return Content.fromParts(
                Part.fromText(processedManual),
                Part.fromText(dynamicEnvSummary)
        );
    }

    
    @Override
    public File getWorkingFolder() {
        return workDir;
    }

    @Override
    public List<Part> getStartupParts() {
        List<Part> startupParts = new ArrayList<>();

        // Part 1: Startup Manual
        startupParts.add(Part.fromText(STARTUP_MANUAL));
        

        // Part 2: Workspace Overview
        try {
            log.info("Attempting to get workspace overview ...");
            String overviewResult = (String) Workspace.getOverview();
            startupParts.add(Part.fromText(overviewResult));
            log.info("Successfully retrieved workspace overview.");
        } catch (Exception e) {
            log.log(Level.SEVERE, "Error executing Workspace.getOverview()", e);
            String errorMsg = "Error getting workspace overview: " + e.getMessage();
            startupParts.add(Part.fromText(errorMsg));
        }
        
        startupParts.addAll(super.getStartupParts());
        
        
        return startupParts;
        
    }
    
    

    private static String computeDynamicEnvSummary() {
        StringBuilder sb = new StringBuilder();
        sb.append("\n\n# Dynamic Environment Details\n");
        sb.append("\n\n# ---------------------------\n");
        sb.append("- **System Properties**: ").append(System.getProperties().toString()).append("\n");
        sb.append("- **Environment variables**: ").append(System.getenv().toString()).append("\n");
        //sb.append("- RunningJVM.chatTemp keys: ").append(RunningJVM.chatTemp.keySet()).append("\n");
        //sb.append("- Gem Ids: ").append(RunningJVM.getGemIds()).append("\n");
        return sb.toString();
    }
}
