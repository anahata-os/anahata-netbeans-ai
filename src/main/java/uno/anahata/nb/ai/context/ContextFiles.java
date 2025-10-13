package uno.anahata.nb.ai.context;

import com.google.genai.types.FunctionResponse;
import com.google.genai.types.Part;
import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import uno.anahata.gemini.ChatMessage;
import uno.anahata.gemini.ContextListener;
import uno.anahata.gemini.GeminiChat;
import uno.anahata.gemini.functions.FunctionManager;
import uno.anahata.gemini.internal.FunctionUtils;

public class ContextFiles implements ContextListener {

    private static final Logger log = Logger.getLogger(ContextFiles.class.getName());
    private static final ContextFiles INSTANCE = new ContextFiles();

    /**
     * The key for the FileObject attribute used to mark a file as being in the
     * AI context. The UI decorators listen for changes to this attribute.
     */
    public static final String ATTR_IN_AI_CONTEXT = "inAiContext"; // NOI18N

    private final Set<String> contextFiles = new HashSet<>();
    private FunctionManager functionManager;

    private ContextFiles() {
    }

    public static ContextFiles getInstance() {
        return INSTANCE;
    }

    public void setFunctionManager(FunctionManager functionManager) {
        this.functionManager = functionManager;
    }

    /**
     * This method is now for internal state tracking only. Decorators should
     * NOT use this, they should check the FileObject attribute directly.
     */
    public boolean contains(File file) {
        try {
            String canonicalPath = file.getCanonicalPath();
            return contextFiles.contains(canonicalPath);
        } catch (IOException e) {
            log.log(Level.WARNING, "Could not get canonical path for " + file, e);
            return contextFiles.contains(file.getAbsolutePath());
        }
    }

    private void rescanContext(GeminiChat source) {
        log.info("Rescanning context for files...");
        Set<String> oldFiles = new HashSet<>(contextFiles);
        contextFiles.clear();

        if (functionManager != null) {
            for (ChatMessage message : source.getContext()) {
                if (message.getContent() != null && message.getContent().parts().isPresent()) {
                    for (Part part : message.getContent().parts().get()) {
                        if (part.functionResponse().isPresent()) {
                            FunctionResponse fr = part.functionResponse().get();
                            Optional<String> resourceIdOpt = FunctionUtils.getResourceIdIfStateful(fr, functionManager);
                            if (resourceIdOpt.isPresent()) {
                                String resourcePath = resourceIdOpt.get();
                                try {
                                    String canonicalPath = new File(resourcePath).getCanonicalPath();
                                    contextFiles.add(canonicalPath);
                                } catch (IOException e) {
                                    log.log(Level.WARNING, "Could not get canonical path for " + resourcePath, e);
                                    contextFiles.add(resourcePath);
                                }
                            }
                        }
                    }
                }
            }
        } else {
            log.warning("FunctionManager is null, cannot scan for context files.");
        }

        if (!oldFiles.equals(contextFiles)) {
            log.info("Context files changed. Old: " + oldFiles.size() + ", New: " + contextFiles.size());

            Set<String> addedFiles = new HashSet<>(contextFiles);
            addedFiles.removeAll(oldFiles);

            Set<String> removedFiles = new HashSet<>(oldFiles);
            removedFiles.removeAll(contextFiles);

            // Set attribute for newly added files to trigger UI update
            for (String path : addedFiles) {
                setFileObjectAttribute(path, true);
            }

            // Clear attribute for removed files to trigger UI update
            for (String path : removedFiles) {
                setFileObjectAttribute(path, false);
            }
        } else {
            log.info("No change in context files.");
        }
    }

    private void setFileObjectAttribute(String path, boolean isInContext) {
        File file = new File(path);
        FileObject fo = FileUtil.toFileObject(FileUtil.normalizeFile(file));
        if (fo == null) {
            log.log(Level.WARNING, "Could not find FileObject for path: {0}", path);
            return;
        }
        try {
            log.log(Level.INFO, "Setting {0} attribute to {1} for: {2}", new Object[]{ATTR_IN_AI_CONTEXT, isInContext, path});
            // This is the official way to trigger a UI refresh for decorators.
            // Set to Boolean.TRUE or null (to remove it).
            fo.setAttribute(ATTR_IN_AI_CONTEXT, isInContext ? Boolean.TRUE : null);
        } catch (IOException e) {
            log.log(Level.SEVERE, "Failed to set attribute on FileObject: " + path, e);
        }
    }

    @Override
    public void contextChanged(GeminiChat source) {
        log.info("Context changed event received.");
        rescanContext(source);
    }

    @Override
    public void contextCleared(GeminiChat source) {
        log.info("Context cleared event received.");
        Set<String> oldFiles = new HashSet<>(contextFiles);
        contextFiles.clear();
        if (!oldFiles.isEmpty()) {
            log.info("Context files cleared. Triggering UI refresh for " + oldFiles.size() + " files.");
            for (String path : oldFiles) {
                setFileObjectAttribute(path, false);
            }
        }
    }
}
