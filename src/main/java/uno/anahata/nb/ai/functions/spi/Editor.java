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
import org.openide.cookies.LineCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.text.Line;
import org.openide.text.Line.ShowOpenType;
import org.openide.text.Line.ShowVisibilityType;
import uno.anahata.gemini.functions.AIToolMethod;
import uno.anahata.gemini.functions.AIToolParam;

/**
 *
 * @author pablo
 */
public class Editor {

    @AIToolMethod("Opens a specified file in the NetBeans editor and optionally scrolls to a specific line.")
    public static String openFile(
            @AIToolParam("The absolute path of the file to open.") String filePath,
            @AIToolParam("The line number to scroll to (1-based).") Integer scrollToLine) throws Exception {
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
            SwingUtilities.invokeLater(() -> {
                try {
                    editorCookie.open();
                    if (scrollToLine != null && scrollToLine > 0) {
                        LineCookie lineCookie = dataObject.getLookup().lookup(LineCookie.class);
                        if (lineCookie != null) {
                            int lineIndex = scrollToLine - 1;
                            Line.Set lineSet = lineCookie.getLineSet();
                            if (lineIndex < lineSet.getLines().size()) {
                                Line line = lineSet.getLines().get(lineIndex);
                                line.show(ShowOpenType.OPEN, ShowVisibilityType.FOCUS);
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
            String scrollMessage = (scrollToLine != null) ? " and scroll to line " + scrollToLine : "";
            return "Successfully requested to open file: " + filePath + scrollMessage;
        } else {
            return "Error: The specified file is not an editable text file.";
        }
    }

    @AIToolMethod("Gets a list of all files open in the editor")
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
                  .append(" [unsavedChanges=").append(modified).append("]")
                  .append("\n");
            } else {
                sb.append("\n-No dataObjet for" + comp);
            }
        }
        return sb.toString();
    }
}
