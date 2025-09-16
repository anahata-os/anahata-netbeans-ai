package uno.anahata.nb.ai;

import java.awt.BorderLayout;
import java.awt.Color;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
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
                
                // This is the critical fix: Associate the language with the document
                // to ensure the lexer is activated reliably.
                Language<?> lang = Language.find(mimeType);
                if (lang != null) {
                    logger.log(Level.INFO, "Found Language for mime type: {0}:" + lang, mimeType);
                    doc.putProperty(Language.class, lang);
                    doc.putProperty("mimeType", mimeType);
                    // This forces the TokenHierarchy to be created, activating highlighting
                    Object thd = TokenHierarchy.get(doc); 
                    //logger.log(Level.INFO, "TokenHierarchy.get(doc) for mime type: {0}:" + thd, mimeType);
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
        
        // [@pablo-ai] START: Diagnostic Wrapper
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBorder(BorderFactory.createLineBorder(Color.RED, 1));
        wrapper.add(codeEditor, BorderLayout.CENTER);

        // Log the sizes to the console
        System.out.println("--- NetBeansCodeBlockRenderer ---");
        System.out.println("Language: " + language);
        System.out.println("Code Editor Preferred Size: " + codeEditor.getPreferredSize());
        System.out.println("Wrapper Preferred Size: " + wrapper.getPreferredSize());
        System.out.println("---------------------------------");
        
        return wrapper;
        // [@pablo-ai] END: Diagnostic Wrapper
    }
}
