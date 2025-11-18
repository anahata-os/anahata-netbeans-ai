package uno.anahata.ai.nb.tools;

import java.io.File;
import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.refactoring.api.RefactoringSession;
import org.netbeans.modules.refactoring.api.RenameRefactoring;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.lookup.Lookups;
import uno.anahata.ai.tools.AIToolMethod;

/**
 * A tool for performing programmatic refactoring operations within the NetBeans IDE.
 */
public class Refactor {

    /**
     * Performs a programmatic rename refactoring of a file within the IDE, providing detailed feedback.
     *
     * @param filePath The absolute path of the file to rename.
     * @param newName  The new name for the file (without the extension).
     * @return A detailed log of the refactoring process.
     * @throws Exception if there is an error invoking the operation.
     */
    @AIToolMethod("Performs a programmatic rename refactoring of a file within the IDE, providing detailed feedback.")
    public static String renameFile(String filePath, String newName) throws Exception {
        StringBuilder feedback = new StringBuilder();
        File f = new File(filePath);
        if (!f.exists()) {
            return "Error: Source file does not exist at path: " + filePath;
        }

        FileObject fileObject = FileUtil.toFileObject(f);
        if (fileObject == null) {
            return "Error: Could not get FileObject for path: " + filePath;
        }
        feedback.append("Successfully found FileObject for ").append(filePath).append("\n");

        RenameRefactoring refactoring = new RenameRefactoring(Lookups.singleton(fileObject));
        refactoring.setNewName(newName);
        feedback.append("Created RenameRefactoring with new name: '").append(newName).append("'\n");

        feedback.append("1. Calling refactoring.preCheck()...\n");
        Problem preCheckProblem = refactoring.preCheck();
        if (preCheckProblem != null && preCheckProblem.isFatal()) {
            return feedback.append("FATAL ERROR during preCheck: ").append(preCheckProblem.getMessage()).toString();
        }
        feedback.append("   preCheck OK. Problem: ").append(preCheckProblem).append("\n");

        feedback.append("2. Calling refactoring.checkParameters()...\n");
        Problem paramsProblem = refactoring.checkParameters();
        if (paramsProblem != null && paramsProblem.isFatal()) {
            return feedback.append("FATAL ERROR during checkParameters: ").append(paramsProblem.getMessage()).toString();
        }
        feedback.append("   checkParameters OK. Problem: ").append(paramsProblem).append("\n");

        RefactoringSession session = RefactoringSession.create("Anahata AI Rename: " + fileObject.getName());
        feedback.append("3. Created RefactoringSession.\n");

        feedback.append("4. Calling refactoring.prepare(session)...\n");
        Problem prepareProblem = refactoring.prepare(session);
        if (prepareProblem != null && prepareProblem.isFatal()) {
            return feedback.append("FATAL ERROR during prepare: ").append(prepareProblem.getMessage()).toString();
        }
        feedback.append("   prepare OK. Problem: ").append(prepareProblem).append("\n");

        feedback.append("5. Calling session.doRefactoring(true)...\n");
        Problem doRefactoringProblem = session.doRefactoring(true);
        if (doRefactoringProblem != null && doRefactoringProblem.isFatal()) {
            return feedback.append("FATAL ERROR during doRefactoring: ").append(doRefactoringProblem.getMessage()).toString();
        }
        feedback.append("   doRefactoring OK. Problem: ").append(doRefactoringProblem).append("\n");

        // Post-check: Verify if the file was actually renamed.
        String extension = com.google.common.io.Files.getFileExtension(filePath);
        File newFile = new File(f.getParent(), newName + "." + extension);
        
        if (newFile.exists()) {
            feedback.append("SUCCESS: Verified that new file '").append(newFile.getName()).append("' exists on disk.");
        } else {
            feedback.append("FAILURE: The new file '").append(newFile.getName()).append("' was not found on disk after the operation. The refactoring might have failed silently or is still in progress.");
        }

        return feedback.toString();
    }
}
