package uno.anahata.nb.ai.systeminstructions;

import com.google.genai.types.Part;
import java.util.Collections;
import java.util.List;
import uno.anahata.gemini.GeminiChat;
import uno.anahata.gemini.config.systeminstructions.SystemInstructionProvider;
import uno.anahata.nb.ai.tools.IDE;

public class IdeAlertsInstructionsProvider extends SystemInstructionProvider {

    @Override
    public String getId() {
        return "netbeans-ide-alerts";
    }

    @Override
    public String getDisplayName() {
        return "IDE Alerts";
    }

    @Override
    public List<Part> getInstructionParts(GeminiChat chat) {
        try {
            String alerts = IDE.getCachedIDEAlerts();
            return Collections.singletonList(Part.fromText(alerts));
        } catch (Exception e) {
            return Collections.singletonList(Part.fromText("Error getting IDE alerts: " + e.getMessage()));
        }
    }
}