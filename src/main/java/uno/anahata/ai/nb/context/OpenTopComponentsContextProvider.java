/* Licensed under the Apache License, Version 2.0 */
package uno.anahata.ai.nb.context;

import com.google.genai.types.Part;
import java.util.Collections;
import java.util.List;
import lombok.SneakyThrows;
import uno.anahata.ai.Chat;
import uno.anahata.ai.context.provider.ContextPosition;
import uno.anahata.ai.context.provider.ContextProvider;
import uno.anahata.ai.internal.GsonUtils;
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