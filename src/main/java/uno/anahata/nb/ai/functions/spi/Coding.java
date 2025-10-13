package uno.anahata.nb.ai.functions.spi;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GraphicsConfiguration;
import java.awt.Rectangle;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.border.TitledBorder;
import org.netbeans.api.diff.Diff;
import org.netbeans.api.diff.DiffView;
import org.netbeans.api.diff.StreamSource;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.windows.WindowManager;
import uno.anahata.gemini.functions.AIToolMethod;
import uno.anahata.gemini.functions.AIToolParam;
import uno.anahata.gemini.functions.ContextBehavior;
import uno.anahata.gemini.functions.pojos.FileInfo;

/**
 * Tools related to coding tasks and modifying source files.
 */
public class Coding {

    @AIToolMethod(value = "Proposes a change to a file by showing an editable, modal diff dialog to the user. Writes the changes to disk and returns the output of LocalFiles.readFile if the change is accepted, or null if cancelled.", behavior = ContextBehavior.STATEFUL_REPLACE)
    public static FileInfo proposeChange(
            @AIToolParam("The absolute path of the file to modify.") String filePath,
            @AIToolParam("The full, new proposed content for the file.") String proposedContent,
            @AIToolParam("A clear and concise explanation of the proposed change.") String explanation) throws Exception {

        final File originalFile = new File(filePath);
        if (!originalFile.exists()) {
            throw new IOException("The source file does not exist: " + filePath);
        }

        final AtomicReference<FileInfo> resultHolder = new AtomicReference<>();
        final CountDownLatch dialogLatch = new CountDownLatch(1);
        final AtomicReference<String> userComment = new AtomicReference<>("");

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
                    dialog.setLayout(new BorderLayout(10, 10));
                    
                    JTextArea explanationArea = new JTextArea(explanation);
                    explanationArea.setEditable(false);
                    explanationArea.setWrapStyleWord(true);
                    explanationArea.setLineWrap(true);
                    explanationArea.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
                    dialog.add(explanationArea, BorderLayout.NORTH);

                    dialog.add(view.getComponent(), BorderLayout.CENTER);

                    JTextArea commentTextArea = new JTextArea(3, 60);
                    JPanel commentPanel = new JPanel(new BorderLayout());
                    commentPanel.setBorder(new TitledBorder("Add Comment (Optional)"));
                    commentPanel.add(new JScrollPane(commentTextArea), BorderLayout.CENTER);
                    
                    JButton acceptButton = new JButton("Accept & Save");
                    JButton cancelButton = new JButton("Cancel");
                    JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
                    buttonPanel.add(cancelButton);
                    buttonPanel.add(acceptButton);
                    
                    JPanel bottomPanel = new JPanel(new BorderLayout(0, 5));
                    bottomPanel.add(commentPanel, BorderLayout.NORTH);
                    bottomPanel.add(buttonPanel, BorderLayout.SOUTH);
                    
                    dialog.add(bottomPanel, BorderLayout.SOUTH);

                    acceptButton.addActionListener(e -> {
                        try {
                            String finalText = new String(proposedFileObject.asBytes());
                            Files.writeString(Paths.get(filePath), finalText);
                            File updatedFile = new File(filePath);
                            
                            FileInfo fileInfo = new FileInfo(
                                filePath,
                                finalText,
                                updatedFile.lastModified(),
                                updatedFile.length()
                            );
                            resultHolder.set(fileInfo);
                            userComment.set(commentTextArea.getText());
                            
                        } catch (IOException ex) {
                            // In a real app, show an error dialog
                            ex.printStackTrace();
                            resultHolder.set(null); 
                        }
                        dialog.dispose();
                    });
                    
                    cancelButton.addActionListener(e -> {
                        resultHolder.set(null);
                        userComment.set(commentTextArea.getText());
                        dialog.dispose();
                    });
                    
                    dialog.addWindowListener(new WindowAdapter() {
                        @Override
                        public void windowClosed(WindowEvent e) {
                            dialogLatch.countDown();
                        }
                    });

                    dialog.pack();
                    
                    // Calculate 90% of the screen size
                    GraphicsConfiguration gc = mainWindow.getGraphicsConfiguration();
                    Rectangle screenBounds = gc.getBounds();
                    int dialogWidth = (int) (screenBounds.width * 0.9);
                    int dialogHeight = (int) (screenBounds.height * 0.9);
                    dialog.setSize(new Dimension(dialogWidth, dialogHeight));
                    
                    dialog.setLocationRelativeTo(mainWindow);
                    dialog.setVisible(true);
                }

            } catch (Exception e) {
                e.printStackTrace();
                resultHolder.set(null);
                dialogLatch.countDown();
            }
        });

        dialogLatch.await();
        
        // Here you could potentially use the userComment, e.g., by returning a more complex object.
        // For now, we just return the FileInfo or null.
        
        return resultHolder.get();
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
