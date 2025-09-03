/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package uno.anahata.nb.ai;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.logging.Logger;
import javax.swing.JEditorPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.api.editor.EditorRegistry;
import org.openide.loaders.DataObject;
import org.openide.filesystems.FileAttributeEvent;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileRenameEvent;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.Utilities;
import org.openide.windows.TopComponent;
import org.openide.windows.TopComponent.Registry;
import org.openide.windows.WindowManager;

/**
 *
 * @author pablo
 */
public class NetBeansListener implements LookupListener, FileChangeListener, PropertyChangeListener {

    Logger log = Logger.getLogger(NetBeansListener.class.getName());

    private final Lookup.Result<Object> result;

    public NetBeansListener() {
        log.info("NetBeansListener() init() entry");
        result = Utilities.actionsGlobalContext().lookupResult(Object.class);
        result.addLookupListener(this);

        Registry registry = WindowManager.getDefault().getRegistry();
        registry.addPropertyChangeListener(this);

        FileUtil.addFileChangeListener(this);
        log.info("NetBeansListener() init() exit");
    }

    @Override
    public void propertyChange(PropertyChangeEvent pce) {
        log.info("Property Change"
                + " \n\tSource: " + pce.getSource()
                + " \n\tProperty: '" + pce.getPropertyName() + "'"
                + "\n\tFrom: " + pce.getOldValue()
                + "\n\tTo:" + pce.getNewValue());
    }

    @Override
    public void resultChanged(LookupEvent ev) {
        // Get all objects in the current global context
        log.info("Lookup changed: " + result.allInstances().size());
        int idx = 0;
        for (Object obj : result.allInstances()) {
            log.info("[" + idx++ + "] " + obj + " \n\t Class:" + obj.getClass().getName());
        }
        
        
/*
        if (result.allInstances().isEmpty()) {
            log.info("---- No objects in context --- ");
        }
*/
    }

    public static String getActiveEditorContent() {
        // Get the currently activated TopComponent
        TopComponent activeTC = TopComponent.getRegistry().getActivated();
        if (activeTC != null) {
            // Look for a DataObject in the TopComponent's Lookup
            DataObject dataObject = activeTC.getLookup().lookup(DataObject.class);
            if (dataObject != null) {
                FileObject fileObject = dataObject.getPrimaryFile();
                if (fileObject != null && fileObject.isValid()) {
                    try (BufferedReader reader = new BufferedReader(
                            new InputStreamReader(fileObject.getInputStream()))) {
                        StringBuilder content = new StringBuilder();
                        String line;
                        while ((line = reader.readLine()) != null) {
                            content.append(line).append("\n");
                        }
                        return content.toString();
                    } catch (IOException ex) {
                        ex.printStackTrace();
                        return null;
                    }
                }
            }
        }
        return null; // No editor or file found
    }

    public static String getFocusedEditorContent() {
        // Get the most recently focused editor component
        JTextComponent editor = EditorRegistry.lastFocusedComponent();
        if (editor != null) {
            // Get the document associated with the editor
            Document doc = editor.getDocument();
            try {
                // Get the full text content of the document
                String content = doc.getText(0, doc.getLength());
                return content;
            } catch (BadLocationException ex) {
                ex.printStackTrace();
                return null;
            }
        }
        return null; // No editor found
    }

    public static FileObject getActiveEditorFile() {
        JTextComponent editor = EditorRegistry.lastFocusedComponent();
        if (editor != null) {
            DataObject dataObject = (DataObject) editor.getClientProperty(DataObject.class);
            if (dataObject != null) {
                return dataObject.getPrimaryFile();
            }
        }
        return null;
    }

    public static void printActiveEditorInfo() {
        JTextComponent editor = EditorRegistry.lastFocusedComponent();
        if (editor != null) {
            // Get the document content
            Document doc = editor.getDocument();
            String content = null;
            try {
                content = doc.getText(0, doc.getLength());
            } catch (BadLocationException ex) {
                ex.printStackTrace();
            }

            // Get the file path
            Object dataObject = editor.getClientProperty(DataObject.class);
            if (dataObject instanceof DataObject) {
                DataObject dao = (DataObject) dataObject;
                String filePath = dataObject != null ? dao.getPrimaryFile().getPath() : "Unknown file";

                
                System.out.println("Content:\n" + (content != null ? content : "Error reading content"));
                System.out.println("End content of File: " + filePath);
            }

        } else {
            System.out.println("No editor open");
        }
    }

    @Override
    public void fileChanged(FileEvent fe) {
        log.info("File changed: " + fe);
        // Update UI or logic here
    }

    @Override
    public void fileFolderCreated(FileEvent fe) {
        log.info("File folder created: " + fe);
    }

    @Override
    public void fileDataCreated(FileEvent fe) {
        log.info("File data created: " + fe);
    }

    @Override
    public void fileDeleted(FileEvent fe) {
        log.info("File deleted: " + fe);
    }

    @Override
    public void fileRenamed(FileRenameEvent fe) {
        log.info("File renamed: " + fe);
    }

    @Override
    public void fileAttributeChanged(FileAttributeEvent fe) {
        log.info("File attribute changed: " + fe);
    }

    public void cleanup() {
        log.info("cleanup(): ");
        result.removeLookupListener(this);
        FileUtil.removeFileChangeListener(this);
        Registry registry = WindowManager.getDefault().getRegistry();
        registry.removePropertyChangeListener(this);
        log.info("cleanup() finished: ");
    }
}
