/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package uno.anahata.nb.ai;

import com.google.genai.types.Content;
import com.google.genai.types.GenerateContentConfig;
import com.google.genai.types.GenerateContentResponse;
import com.google.genai.types.Part;
import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.openide.modules.ModuleInfo;
import org.openide.modules.Modules;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import uno.anahata.gemini.GeminiConfigProvider;

/**
 *
 * @author pablo
 */
public class GeminiConfigProviderImpl implements GeminiConfigProvider {
    private static String staticEnvSummary;  // Cached static info
    private static volatile String dynamicEnvSummary;  // Updated on demand

    // Static system instruction parts
    

    // Initialize static info once
    static {
        staticEnvSummary = computeStaticEnvSummary();
        dynamicEnvSummary = computeDynamicEnvSummary();        
    }

    @Override
    public Content getSystemInstruction() {
        return Content.fromParts(
        Part.fromText("You are an AI software developer assistant embedded in " + System.getProperty("netbeans.productversion") + " as a NBM plugin."),
        //Part.fromText("Your role is to write Java code that the plugin can compile it and run in the current JVM via declared functions to interact with the IDE (e.g., open files using org.netbeans.api.actions.OpenAction)."),
        //Part.fromText("You can also use the runShell or read and write file functions if that is easier"),
        Part.fromText("Use the provided environment details to ensure compatibility."),
        //Part.fromText(staticEnvSummary),
        Part.fromText(dynamicEnvSummary));
    }
    

    // Compute static environment details (called once)
    private static String computeStaticEnvSummary() {
        StringBuilder sb = new StringBuilder();
        sb.append("Static environment details:\n");
        //sb.append("- IDE: Apache NetBeans ").append(Modules.getDefault().ownerOf(ModuleInfo.class).getSpecificationVersion()).append("\n");
        return sb.toString();
    }

    // Compute dynamic environment details (called per request or on event)
    private static String computeDynamicEnvSummary() {
        StringBuilder sb = new StringBuilder();
        
        sb.append("- System Properties: ").append(System.getProperties().toString()).append("\n");
        sb.append("- Environment veriables: ").append(System.getenv().toString()).append("\n");
        
        // Add project-specific info if applicable, e.g., current project's dependencies
        return sb.toString();
    }

    // Summarize classpath (shorten for brevity)
    private static String summarizeClasspath(String cp) {
        return Arrays.stream(cp.split(File.pathSeparator))
            .filter(p -> p.contains("netbeans") || p.contains("java"))
            .limit(5)  // Limit for token efficiency
            .collect(Collectors.joining(", "));
    }
    
    
}

