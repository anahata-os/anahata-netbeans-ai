package uno.anahata.nb.ai.gemini.spi;

import com.google.genai.types.Part;
import java.util.Collections;
import java.util.List;
import uno.anahata.gemini.GeminiChat;
import uno.anahata.gemini.systeminstructions.SystemInstructionProvider;
import uno.anahata.nb.ai.functions.spi.Editor;

public class OpenFilesInEditorInstructionsProvider extends SystemInstructionProvider {

    @Override
    public String getId() {
        return "netbeans-open-files";
    }

    @Override
    public String getDisplayName() {
        return "Editor.getOpenFiles()";
    }

    @Override
    public List<Part> getInstructionParts(GeminiChat chat) {
        String openFiles = Editor.getOpenFiles();
        return Collections.singletonList(Part.fromText(openFiles));
    }
}