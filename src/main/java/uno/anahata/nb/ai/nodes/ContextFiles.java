package uno.anahata.nb.ai.nodes;

import com.google.genai.types.FunctionResponse;
import com.google.genai.types.Part;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import uno.anahata.gemini.ChatMessage;
import uno.anahata.gemini.Chat;
import uno.anahata.gemini.context.ContextListener;
import uno.anahata.gemini.context.stateful.ResourceTracker;
import uno.anahata.gemini.functions.FunctionManager;


public class ContextFiles implements ContextListener {

    private static final Logger log = Logger.getLogger(ContextFiles.class.getName());
    private static final ContextFiles INSTANCE = new ContextFiles();

    public static final String CONTEXT_FILES_PROPERTY = "contextFiles";
    public static final String ATTR_IN_AI_CONTEXT = "inAiContext"; // NOI18N

    private final Set<String> contextFiles = new HashSet<>();
    private FunctionManager functionManager;
    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);

    private ContextFiles() {
        log.info("ENTRY ContextFiles()");
        log.info("EXIT ContextFiles()");
    }

    public static ContextFiles getInstance() {
        // Static, no entry/exit needed as it's trivial
        return INSTANCE;
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        log.log(Level.INFO, "ENTRY addPropertyChangeListener(listener={0})", listener);
        pcs.addPropertyChangeListener(listener);
        log.log(Level.INFO, "EXIT addPropertyChangeListener()");
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        log.log(Level.INFO, "ENTRY removePropertyChangeListener(listener={0})", listener);
        pcs.removePropertyChangeListener(listener);
        log.log(Level.INFO, "EXIT removePropertyChangeListener()");
    }

    public void setFunctionManager(FunctionManager functionManager) {
        log.log(Level.INFO, "ENTRY setFunctionManager(functionManager={0})", functionManager);
        this.functionManager = functionManager;
        log.log(Level.INFO, "EXIT setFunctionManager()");
    }

    public boolean contains(File file) {
        log.log(Level.INFO, "ENTRY contains(file={0})", file.getName());
        try {
            String canonicalPath = file.getCanonicalPath();
            boolean result = contextFiles.contains(canonicalPath);
            log.log(Level.INFO, "EXIT contains(): returning {0} for canonical path {1}", new Object[]{result, canonicalPath});
            return result;
        } catch (IOException e) {
            log.log(Level.WARNING, "Could not get canonical path for " + file, e);
            boolean result = contextFiles.contains(file.getAbsolutePath());
            log.log(Level.INFO, "EXIT contains(): returning {0} for absolute path {1}", new Object[]{result, file.getAbsolutePath()});
            return result;
        }
    }

    private void rescanContext(Chat source) {
        log.info("ENTRY rescanContext()");
        Set<String> oldFiles = new HashSet<>(contextFiles);
        Set<String> newFiles = new HashSet<>();

        if (functionManager != null) {
            for (ChatMessage message : source.getContext()) {
                if (message.getContent() != null && message.getContent().parts().isPresent()) {
                    for (Part part : message.getContent().parts().get()) {
                        if (part.functionResponse().isPresent()) {
                            FunctionResponse fr = part.functionResponse().get();
                            Optional<String> resourceIdOpt = ResourceTracker.getResourceIdIfStateful(fr, functionManager);
                            if (resourceIdOpt.isPresent()) {
                                String resourcePath = resourceIdOpt.get();
                                try {
                                    String canonicalPath = new File(resourcePath).getCanonicalPath();
                                    newFiles.add(canonicalPath);
                                } catch (IOException e) {
                                    log.log(Level.WARNING, "Could not get canonical path for " + resourcePath, e);
                                    newFiles.add(resourcePath);
                                }
                            }
                        }
                    }
                }
            }
        } else {
            log.warning("FunctionManager is null, cannot scan for context files.");
        }

        if (!oldFiles.equals(newFiles)) {
            log.log(Level.INFO, "Context files changed. Old: {0}, New: {1}. Firing property change.", new Object[]{oldFiles.size(), newFiles.size()});
            synchronized (this) {
                contextFiles.clear();
                contextFiles.addAll(newFiles);
            }
            pcs.firePropertyChange(CONTEXT_FILES_PROPERTY,
                                   Collections.unmodifiableSet(oldFiles),
                                   Collections.unmodifiableSet(newFiles));
        } else {
            log.info("No change in context files.");
        }
        log.info("EXIT rescanContext()");
    }

    @Override
    public void contextChanged(Chat source) {
        log.info("ENTRY contextChanged()");
        rescanContext(source);
        log.info("EXIT contextChanged()");
    }

    @Override
    public void contextCleared(Chat source) {
        log.info("ENTRY contextCleared()");
        Set<String> oldFiles = new HashSet<>(contextFiles);
        synchronized (this) {
            contextFiles.clear();
        }
        if (!oldFiles.isEmpty()) {
            log.log(Level.INFO, "Context files cleared. Triggering UI refresh for {0} files.", oldFiles.size());
            pcs.firePropertyChange(CONTEXT_FILES_PROPERTY,
                                   Collections.unmodifiableSet(oldFiles),
                                   Collections.emptySet());
        }
        log.info("EXIT contextCleared()");
    }
}
