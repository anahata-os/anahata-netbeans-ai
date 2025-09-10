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
import uno.anahata.gemini.GeminiConfigProvider;
import uno.anahata.gemini.functions.spi.LocalFiles;
import uno.anahata.gemini.functions.spi.RunningJVM;

public class GeminiConfigProviderImpl implements GeminiConfigProvider {

    private static final String PER_REQUEST_MANUAL;
    private static final String STARTUP_MANUAL;
    private static volatile String dynamicEnvSummary;
    private static final Logger log = Logger.getLogger(GeminiConfigProviderImpl.class.getName());
    private static final String GEMS_DIR_PATH = System.getProperty("user.home") + File.separator + ".netbeans" + File.separator + "Gems";

    static {
        File f = new File(GEMS_DIR_PATH);
        if (!f.exists()) {
            f.mkdirs();
        }
        RunningJVM.setGemsDirPath(GEMS_DIR_PATH);
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
        String processedManual = PER_REQUEST_MANUAL
                .replace("${netbeans.productversion}", System.getProperty("netbeans.productversion", "an unknown version of NetBeans"))
                .replace("${gems.dir}", GEMS_DIR_PATH);

        return Content.fromParts(
                Part.fromText(processedManual),
                Part.fromText(dynamicEnvSummary)
        );
    }

    @Override
    public Content getStartupContent() {
        List<Part> startupParts = new ArrayList<>();

        // Part 1: Startup Manual
        startupParts.add(Part.fromText(STARTUP_MANUAL));

        // Part 2: Workspace Overview
        try {
            log.info("Attempting to get workspace overview via runGem...");
            String overviewResult = (String) RunningJVM.runGem("getWorkspaceOverview.java");
            startupParts.add(Part.fromText(overviewResult));
            log.info("Successfully retrieved workspace overview.");
        } catch (Exception e) {
            log.log(Level.SEVERE, "Error executing getWorkspaceOverview.java gem", e);
            String errorMsg = "Error getting workspace overview: " + e.getMessage();
            startupParts.add(Part.fromText(errorMsg));
        }

        //File gemsDir = new File(G);
        // Part 3: Gems, Notes, and History (Consolidated)
        try {
            log.info("Attempting to read all files from Gems directory...");
            Map<String, String> fileContentMap = LocalFiles.findAndReadFiles(GEMS_DIR_PATH, "**/*");

            for (String path : fileContentMap.keySet()) {
                String content = fileContentMap.get(path);
                startupParts.add(Part.fromText(path));
                startupParts.add(Part.fromText(content));
            }
            /*
            if (fileContentMap != null && !fileContentMap.isEmpty()) {
                 Map<String, String> files = fileContentMap.entrySet().stream()
                    .collect(Collectors.toMap(
                        entry -> Paths.get(entry.getKey()).getFileName().toString(),
                        Map.Entry::getValue
                    ));

                String filesJson = new Gson().toJson(files);
                startupParts.add(Part.fromJson(filesJson));
                log.info("Successfully read and added " + files.size() + " files from Gems directory.");
            } else {
                String notFoundMsg = "NOTE: No files found in Gems directory: " + GEMS_DIR_PATH;
                startupParts.add(Part.fromText("{\"gemsDirDump\": {}}")); // Send empty JSON
                log.warning(notFoundMsg);
            }
             */
        } catch (Exception e) {
            log.log(Level.SEVERE, "Error reading files from Gems directory", e);
            String errorMsg = "Error reading gems and notes: " + e.getMessage();
            startupParts.add(Part.fromText(errorMsg));
        }

        return Content.fromParts(startupParts.toArray(new Part[startupParts.size()]));
    }

    private static String computeDynamicEnvSummary() {
        StringBuilder sb = new StringBuilder();
        sb.append("\n\n# Dynamic Environment Details\n");
        sb.append("- System Properties: ").append(System.getProperties().toString()).append("\n");
        sb.append("- Environment variables: ").append(System.getenv().toString()).append("\n");
        sb.append("- RunningJVM.chatTemp keys: ").append(RunningJVM.chatTemp.keySet()).append("\n");
        sb.append("- Gem Ids: ").append(RunningJVM.getGemIds()).append("\n");
        return sb.toString();
    }
}
