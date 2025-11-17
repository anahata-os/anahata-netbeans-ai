package uno.anahata.ai.nb.context;

import com.google.genai.types.Part;
import java.util.Collections;
import java.util.List;
import lombok.SneakyThrows;
import uno.anahata.gemini.Chat;
import uno.anahata.gemini.content.ContextPosition;
import uno.anahata.gemini.content.ContextProvider;
import uno.anahata.gemini.internal.GsonUtils;
import uno.anahata.ai.nb.tools.TopComponents;

public class OpenTopComponentsContextProvider extends ContextProvider {

    public OpenTopComponentsContextProvider() {
        super(ContextPosition.AUGMENTED_WORKSPACE);
    }
    
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
    public List<Part> getParts(Chat chat) {
        String openFiles = TopComponents.getOpenTopComponentsMarkdown();
        return Collections.singletonList(Part.fromText(openFiles));
    }
}
