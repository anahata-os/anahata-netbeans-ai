package uno.anahata.nb.ai.context;

import com.google.genai.types.FunctionResponse;
import com.google.genai.types.Part;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import uno.anahata.gemini.ChatMessage;
import uno.anahata.gemini.ContextListener;
import uno.anahata.gemini.GeminiChat;
import uno.anahata.gemini.functions.FunctionManager;
import uno.anahata.gemini.internal.FunctionUtils;

public class ContextFiles implements ContextListener {

    public static final String PROP_CONTEXT_FILES_CHANGED = "contextFilesChanged";
    private static final ContextFiles INSTANCE = new ContextFiles();

    private final Set<String> contextFiles = new HashSet<>();
    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
    private FunctionManager functionManager;

    private ContextFiles() {
    }

    public static ContextFiles getInstance() {
        return INSTANCE;
    }

    public void setFunctionManager(FunctionManager functionManager) {
        this.functionManager = functionManager;
    }

    public boolean contains(File file) {
        return contextFiles.contains(file.getAbsolutePath());
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        pcs.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        pcs.removePropertyChangeListener(listener);
    }

    private void rescanContext(GeminiChat source) {
        Set<String> oldFiles = new HashSet<>(contextFiles);
        contextFiles.clear();

        if (functionManager != null) {
            for (ChatMessage message : source.getContext()) {
                if (message.getContent() != null && message.getContent().parts().isPresent()) {
                    for (Part part : message.getContent().parts().get()) {
                        if (part.functionResponse().isPresent()) {
                            FunctionResponse fr = part.functionResponse().get();
                            Optional<String> resourceIdOpt = FunctionUtils.getResourceIdIfStateful(fr, functionManager);
                            resourceIdOpt.ifPresent(contextFiles::add);
                        }
                    }
                }
            }
        }

        if (!oldFiles.equals(contextFiles)) {
            pcs.firePropertyChange(PROP_CONTEXT_FILES_CHANGED, oldFiles, new HashSet<>(contextFiles));
        }
    }

    @Override
    public void contextChanged(GeminiChat source) {
        rescanContext(source);
    }

    @Override
    public void contextCleared(GeminiChat source) {
        Set<String> oldFiles = new HashSet<>(contextFiles);
        contextFiles.clear();
        if (!oldFiles.isEmpty()) {
            pcs.firePropertyChange(PROP_CONTEXT_FILES_CHANGED, oldFiles, new HashSet<>(contextFiles));
        }
    }
}
