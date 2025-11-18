package uno.anahata.ai.nb.context;

import com.google.genai.types.Part;
import java.util.Collections;
import java.util.List;
import uno.anahata.ai.Chat;
import uno.anahata.ai.context.provider.ContextPosition;
import uno.anahata.ai.context.provider.ContextProvider;
import uno.anahata.ai.nb.tools.IDE;

public class IdeAlertsContextProvider extends ContextProvider {

    public IdeAlertsContextProvider() {
        super(ContextPosition.AUGMENTED_WORKSPACE);
    }
    

    @Override
    public String getId() {
        return "netbeans-ide-alerts";
    }

    @Override
    public String getDisplayName() {
        return "IDE Alerts";
    }

    @Override
    public List<Part> getParts(Chat chat) {
        try {
            String alerts = IDE.getCachedIDEAlerts();
            return Collections.singletonList(Part.fromText(alerts));
        } catch (Exception e) {
            return Collections.singletonList(Part.fromText("Error getting IDE alerts: " + e.getMessage()));
        }
    }
}