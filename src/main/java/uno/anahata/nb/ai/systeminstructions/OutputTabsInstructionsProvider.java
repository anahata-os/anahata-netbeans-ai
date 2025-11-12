package uno.anahata.nb.ai.systeminstructions;

import com.google.genai.types.Part;
import java.util.Collections;
import java.util.List;
import lombok.SneakyThrows;
import uno.anahata.gemini.GeminiChat;
import uno.anahata.gemini.internal.GsonUtils;
import uno.anahata.gemini.config.systeminstructions.SystemInstructionProvider;
import uno.anahata.nb.ai.model.ide.OutputTabInfo;
import uno.anahata.nb.ai.tools.Output;
import uno.anahata.nb.ai.tools.TopComponents;

public class OutputTabsInstructionsProvider extends SystemInstructionProvider {

    @Override
    public String getId() {
        return "netbeans-open-topcomponents";
    }

    @Override
    public String getDisplayName() {
        return "Output.listOutputTabs() (The output tabs open in the ide)";
    }

    @Override
    @SneakyThrows
    public List<Part> getInstructionParts(GeminiChat chat) {
        List<OutputTabInfo> outputTabs = Output.listOutputTabs();
        return Collections.singletonList(Part.fromText(GsonUtils.prettyPrint(outputTabs)));
    }
}
