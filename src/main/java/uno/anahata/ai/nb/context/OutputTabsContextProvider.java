package uno.anahata.ai.nb.context;

import com.google.genai.types.Part;
import java.util.Collections;
import java.util.List;
import lombok.SneakyThrows;
import uno.anahata.gemini.Chat;
import uno.anahata.gemini.content.ContextPosition;
import uno.anahata.gemini.content.ContextProvider;
import uno.anahata.gemini.internal.GsonUtils;
import uno.anahata.ai.nb.model.ide.OutputTabInfo;
import uno.anahata.ai.nb.tools.Output;

public class OutputTabsContextProvider extends ContextProvider {

    public OutputTabsContextProvider() {
        super(ContextPosition.AUGMENTED_WORKSPACE);
    }

    
    @Override
    public String getId() {
        return "netbeans-open-output-tabs";
    }

    @Override
    public String getDisplayName() {
        return "Output.listOutputTabs() (The output tabs open in the ide with a unique id for content retrieval via Output.getOutputTabContent)";
    }

    @Override
    @SneakyThrows
    public List<Part> getParts(Chat chat) {
        List<OutputTabInfo> outputTabs = Output.listOutputTabs();        
        return Collections.singletonList(Part.fromText(GsonUtils.prettyPrint(outputTabs)));
    }
}
