package uno.anahata.nb.ai.systeminstructions;

import com.google.genai.types.Part;
import java.util.Collections;
import java.util.List;
import lombok.SneakyThrows;
import uno.anahata.gemini.GeminiChat;
import uno.anahata.gemini.systeminstructions.SystemInstructionProvider;
import uno.anahata.nb.ai.tools.TopComponents;

public class OpenTopComponentsInstructionsProvider extends SystemInstructionProvider {

    @Override
    public String getId() {
        return "netbeans-open-topcomponents";
    }

    @Override
    public String getDisplayName() {
        return "TopComponents.getOpenTopComponentsMarkdown()";
    }

    @Override
    @SneakyThrows
    public List<Part> getInstructionParts(GeminiChat chat) {
        String openFiles = TopComponents.getOpenTopComponentsMarkdown();
        return Collections.singletonList(Part.fromText(openFiles));
    }
}
