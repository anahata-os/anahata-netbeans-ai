package uno.anahata.nb.ai;

import java.awt.Dimension;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JScrollPane;
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
        logger.log(Level.INFO, "Creating code block for: {0}:\n" + code, language);
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
                logger.log(Level.INFO, "Found EditorKit for mime type: {0}:" + kit, mimeType);
                codeEditor.setEditorKit(kit);
                Document doc = codeEditor.getDocument();
                
                Language<?> lang = Language.find(mimeType);
                if (lang != null) {
                    logger.log(Level.INFO, "Found Language for mime type: {0}:" + lang, mimeType);
                    doc.putProperty(Language.class, lang);
                    doc.putProperty("mimeType", mimeType);
                    TokenHierarchy.get(doc); 
                } else {
                    logger.log(Level.WARNING, "Could not find a registered Language for mime type: {0}. Highlighting may not be applied.", mimeType);
                }
                
                doc.putProperty("hyperlink-activation-enabled", false);
                codeEditor.setText(code);
                
            } else {
                 logger.log(Level.WARNING, "Could not find an EditorKit for mime type: {0}", mimeType);
                 codeEditor.setText(code);
            }

        } catch (Throwable e) {
            logger.log(Level.SEVERE, "An unexpected error occurred while setting up the EditorKit for mime type: " + mimeType, e);
            codeEditor.setText(code);
        }
        
        // Prime the component to calculate its size, but do NOT call setPreferredSize.
        codeEditor.setSize(new Dimension(600, Integer.MAX_VALUE));

        // Return the component wrapped in a JScrollPane.
        return new JScrollPane(codeEditor);
    }
}
