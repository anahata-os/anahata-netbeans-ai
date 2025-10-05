package uno.anahata.nb.ai.functions.spi;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import org.netbeans.api.diff.Diff;
import org.netbeans.api.diff.DiffView;
import org.netbeans.api.diff.StreamSource;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.windows.WindowManager;
import uno.anahata.gemini.functions.AIToolMethod;
import uno.anahata.gemini.functions.AIToolParam;


/**
 * Tools related to coding tasks and modifying source files.
 */
public class Coding {

    @AIToolMethod("Proposes a change to a file by showing an editable, modal diff dialog to the user. Returns the user-approved content or null if cancelled.")
    public static String proposeChange(
            @AIToolParam("The absolute path of the file to modify.") String filePath,
            @AIToolParam("The full, new proposed content for the file.") String proposedContent) throws Exception {

        final File originalFile = new File(filePath);
        if (!originalFile.exists()) {
            throw new IOException("The source file does not exist: " + filePath);
        }

        final AtomicReference<String> resultHolder = new AtomicReference<>();
        final CountDownLatch dialogLatch = new CountDownLatch(1);

        SwingUtilities.invokeLater(() -> {
            try {
                FileObject memoryRoot = FileUtil.createMemoryFileSystem().getRoot();
                FileObject proposedFileObject = FileUtil.createData(memoryRoot, "proposed/" + originalFile.getName());
                try (Writer writer = new OutputStreamWriter(proposedFileObject.getOutputStream())) {
                    writer.write(proposedContent);
                }

                StreamSource source1 = StreamSource.createSource("Original", "Original", "text/x-java", originalFile);
                
                try (Reader reader = new InputStreamReader(proposedFileObject.getInputStream())) {
                    StreamSource source2 = StreamSource.createSource("Proposed Change", "Proposed", "text/x-java", reader);
                    DiffView view = Diff.getDefault().createDiff(source1, source2);

                    findAndSetEditable(view.getComponent(), 1, true);

                    JFrame mainWindow = (JFrame) WindowManager.getDefault().getMainWindow();
                    JDialog dialog = new JDialog(mainWindow, "Proposing Change for " + originalFile.getName(), true);
                    dialog.setLayout(new BorderLayout());
                    dialog.add(view.getComponent(), BorderLayout.CENTER);

                    JButton acceptButton = new JButton("Accept");
                    JButton cancelButton = new JButton("Cancel");
                    JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
                    buttonPanel.add(cancelButton);
                    buttonPanel.add(acceptButton);
                    dialog.add(buttonPanel, BorderLayout.SOUTH);

                    acceptButton.addActionListener(e -> {
                        try {
                            // Read the final content back from the in-memory object
                            String finalText = new String(proposedFileObject.asBytes());
                            resultHolder.set(finalText);
                        } catch (IOException ex) {
                            resultHolder.set("ERROR: " + ex.getMessage());
                        }
                        dialog.dispose();
                    });
                    
                    cancelButton.addActionListener(e -> {
                        resultHolder.set(null);
                        dialog.dispose();
                    });
                    
                    dialog.addWindowListener(new WindowAdapter() {
                        @Override
                        public void windowClosed(WindowEvent e) {
                            dialogLatch.countDown();
                        }
                    });

                    dialog.pack();
                    dialog.setSize(900, 700);
                    dialog.setLocationRelativeTo(mainWindow);
                    dialog.setVisible(true);
                }

            } catch (Exception e) {
                e.printStackTrace();
                resultHolder.set("ERROR: " + e.getMessage());
                dialogLatch.countDown();
            }
        });

        dialogLatch.await();
        String result = resultHolder.get();
        if (result != null && result.startsWith("ERROR:")) {
            throw new IOException(result);
        }
        return result;
    }

    private static void findAndSetEditable(Component comp, int targetIndex, boolean editable) {
        AtomicInteger currentIndex = new AtomicInteger(0);
        findAndSetEditableRecursive(comp, targetIndex, editable, currentIndex);
    }

    private static void findAndSetEditableRecursive(Component comp, int targetIndex, boolean editable, AtomicInteger currentIndex) {
        if (comp instanceof JEditorPane) {
            if (currentIndex.getAndIncrement() == targetIndex) {
                ((JEditorPane) comp).setEditable(editable);
                return;
            }
        }
        if (comp instanceof Container) {
            for (Component child : ((Container) comp).getComponents()) {
                findAndSetEditableRecursive(child, targetIndex, editable, currentIndex);
            }
        }
    }
}
