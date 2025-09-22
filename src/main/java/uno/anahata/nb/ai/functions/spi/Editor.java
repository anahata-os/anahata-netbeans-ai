/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package uno.anahata.nb.ai.functions.spi;

import java.io.File;
import javax.swing.SwingUtilities;
import javax.swing.text.JTextComponent;
import org.netbeans.api.editor.EditorRegistry;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.editor.NbEditorUtilities;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import uno.anahata.gemini.functions.AITool;

/**
 *
 * @author pablo
 */
public class Editor {
    
    @AITool("Opens a specified file in the NetBeans editor.")
    public static String openFile(@AITool("The absolute path of the file to open.") String filePath) throws Exception {
        if (filePath == null || filePath.trim().isEmpty()) {
            return "Error: The 'filePath' parameter was not set.";
        }
        File file = new File(filePath);
        if (!file.exists()) {
            return "Error: File does not exist at path: " + filePath;
        }
        FileObject fileObject = FileUtil.toFileObject(FileUtil.normalizeFile(file));
        if (fileObject == null) {
            return "Error: Could not find or create a FileObject for: " + filePath;
        }
        DataObject dataObject = DataObject.find(fileObject);
        EditorCookie editorCookie = dataObject.getLookup().lookup(EditorCookie.class);
        if (editorCookie != null) {
            SwingUtilities.invokeLater(editorCookie::open);
            return "Successfully requested to open file in editor: " + filePath + " with cookie " + editorCookie;
        } else {
            return "Error: The specified file is not an editable text file.";
        }
    }
    
     // --- Open Files (with dirty flag) ---
    @AITool("Gets a list of all files open in the editor")
    public static String getOpenFiles() {
        StringBuilder sb = new StringBuilder();
        int total = 0;
        int totalDocs = 0;
        for (JTextComponent comp : EditorRegistry.componentList()) {
            total++;
            DataObject dobj = NbEditorUtilities.getDataObject(comp.getDocument());
            if (dobj != null) {
                totalDocs++;
                boolean modified = dobj.isModified();
                FileObject fo = dobj.getPrimaryFile();
                Project owner = FileOwnerQuery.getOwner(fo);

                String projName = "(no project)";
                if (owner != null) {
                    ProjectInformation info = ProjectUtils.getInformation(owner);
                    String folderName = owner.getProjectDirectory().getNameExt();
                    projName = folderName + " (Display: " + info.getDisplayName() + ")";
                }

                sb.append("File: ").append(fo.getPath())
                  .append(" [lastModifiedOnDisk=").append(dobj.getPrimaryFile().lastModified()).append("]")
                  .append(" [unsavedChanges=").append(modified).append("]")
                  .append(" (Project: ").append(projName).append(")")
                  .append("\n");
            } else {
                sb.append("\n-No dataObjet for" + comp);
            }
        }
        return sb.toString();
    }
    /*
    public static FileObject getSelectedEditorFile() {
    JTextComponent editor = EditorRegistry.lastFocusedComponent();
    if (editor != null) {
        DataObject dobj = DataObject.find(editor.getDocument());
        return dobj.getPrimaryFile();
    }
    return null;
}*/
}
