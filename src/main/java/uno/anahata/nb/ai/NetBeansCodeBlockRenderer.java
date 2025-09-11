package uno.anahata.nb.ai;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.text.Document;
import javax.swing.text.EditorKit;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.TokenHierarchy;
import uno.anahata.gemini.ui.CodeBlockRenderer;

public class NetBeansCodeBlockRenderer implements CodeBlockRenderer {

    private static final Logger logger = Logger.getLogger(NetBeansCodeBlockRenderer.class.getName());

    @Override
    public JComponent createCodeBlock(String code, String language) {
        JEditorPane codeEditor = new JEditorPane();
        codeEditor.setEditable(false);

        String mimeType = "text/plain"; // Default
        if ("java".equalsIgnoreCase(language)) {
            mimeType = "text/x-java";
        } else if ("xml".equalsIgnoreCase(language)) {
            mimeType = "text/xml";
        } else if ("html".equalsIgnoreCase(language)) {
            mimeType = "text/html";
        }
        // Add other language-to-mimetype mappings here

        try {
            EditorKit kit = MimeLookup.getLookup(mimeType).lookup(EditorKit.class);
            if (kit != null) {
                codeEditor.setEditorKit(kit);
                Document doc = codeEditor.getDocument();
                
                // This is the critical fix: Associate the language with the document
                // to ensure the lexer is activated reliably.
                Language<?> lang = Language.find(mimeType);
                if (lang != null) {
                    doc.putProperty(Language.class, lang);
                    doc.putProperty("mimeType", mimeType);
                    // This forces the TokenHierarchy to be created, activating highlighting
                    TokenHierarchy.get(doc); 
                } else {
                    logger.log(Level.WARNING, "Could not find a registered Language for mime type: {0}. Highlighting may not be applied.", mimeType);
                }
                
                // Disable hyperlinks
                doc.putProperty("hyperlink-activation-enabled", false);
                
                codeEditor.setText(code);
                
            } else {
                 logger.log(Level.WARNING, "Could not find an EditorKit for mime type: {0}", mimeType);
                 codeEditor.setText(code); // Set text even if kit fails
            }

        } catch (Throwable e) {
            logger.log(Level.SEVERE, "An unexpected error occurred while setting up the EditorKit for mime type: " + mimeType, e);
            codeEditor.setText(code); // Ensure text is set on error
        }
        
        return codeEditor;
    }
}
