/* Licensed under the Apache License, Version 2.0 */
package uno.anahata.ai.nb;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Image;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.DecimalFormat;
import java.util.Comparator;
import java.util.List;
import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JToolBar;
import javax.swing.ListSelectionModel;
import javax.swing.Timer;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableRowSorter;
import lombok.extern.slf4j.Slf4j;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.util.ImageUtilities;
import org.openide.windows.TopComponent;
import uno.anahata.ai.status.ChatStatus;
import uno.anahata.ai.swing.SwingChatConfig;

/**
 * A TopComponent that displays a list of all active Anahata AI sessions.
 * It allows users to create new sessions, focus existing ones, or dispose of them.
 */
@TopComponent.Description(
        preferredID = "LiveSessionsTopComponent",
        iconBase = "icons/anahata_16.png",
        persistenceType = TopComponent.PERSISTENCE_ONLY_OPENED)
@TopComponent.Registration(mode = "explorer", openAtStartup = true, position = 100)
@ActionID(category = "Window", id = "uno.anahata.nb.ai.LiveSessionsTopComponent")
@ActionReference(path = "Menu/Window", position = 100)
@TopComponent.OpenActionRegistration(
        displayName = "Anahata Sessions",
        preferredID = "LiveSessionsTopComponent"
)
@Slf4j
public class LiveSessionsTopComponent extends TopComponent {

    private final JTable table;
    private final LiveSessionsTableModel model;
    private final Timer refreshTimer;
    private final JButton closeButton;
    private final JButton disposeButton;

    /**
     * Default constructor for LiveSessionsTopComponent.
     * Initializes the UI components, toolbar, and table.
     */
    public LiveSessionsTopComponent() {
        setName("Anahata Sessions");
        setToolTipText("Manage active Anahata AI sessions");
        setLayout(new BorderLayout());

        // Toolbar for Actions
        JToolBar toolBar = new JToolBar();
        toolBar.setFloatable(false);

        // Create icons dynamically at runtime
        Image anahataIcon = ImageUtilities.loadImage("icons/anahata_16.png", true);
        Image addIcon = ImageUtilities.loadImage("org/netbeans/modules/autoupdate/ui/resources/add.png", true);
        Image newSessionIcon = null;
        if (anahataIcon != null && addIcon != null) {
            // Overlay the 'add' icon at the bottom-right corner
            newSessionIcon = ImageUtilities.mergeImages(anahataIcon, addIcon, 8, 8);
        }

        JButton newButton = new JButton("New");
        newButton.setToolTipText("Create a new AI session");
        if (newSessionIcon != null) {
            newButton.setIcon(new ImageIcon(newSessionIcon));
        }
        newButton.addActionListener(e -> createNewSession());
        toolBar.add(newButton);

        closeButton = new JButton("Close");
        closeButton.setToolTipText("Close the selected AI session window");
        closeButton.setIcon(ImageUtilities.loadImageIcon("org/openide/actions/close.gif", true));
        closeButton.addActionListener(e -> closeSelectedSession());
        closeButton.setEnabled(false);
        toolBar.add(closeButton);
        
        toolBar.add(Box.createHorizontalGlue()); // Pushes subsequent components to the right

        disposeButton = new JButton("Dispose");
        disposeButton.setToolTipText("Permanently dispose of the selected session");
        disposeButton.setIcon(ImageUtilities.loadImageIcon("org/openide/actions/delete.gif", true));
        disposeButton.addActionListener(e -> disposeSelectedSession());
        disposeButton.setEnabled(false);
        toolBar.add(disposeButton);

        add(toolBar, BorderLayout.NORTH);

        model = new LiveSessionsTableModel();
        table = new JTable(model);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setFillsViewportHeight(true);
        
        // Selection listener to enable/disable buttons
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                updateButtonState();
            }
        });

        // Custom Renderers
        table.setDefaultRenderer(ChatStatus.class, new StatusCellRenderer());
        table.getColumnModel().getColumn(LiveSessionsTableModel.CONTEXT_COL).setCellRenderer(new ContextUsageCellRenderer());

        // Sorting
        TableRowSorter<LiveSessionsTableModel> sorter = new TableRowSorter<>(model);
        table.setRowSorter(sorter);
        sorter.setComparator(LiveSessionsTableModel.CONTEXT_COL, Comparator.comparingDouble(d -> (Double) d));
        sorter.setSortKeys(List.of(new javax.swing.RowSorter.SortKey(LiveSessionsTableModel.SESSION_COL, javax.swing.SortOrder.ASCENDING)));

        // Double-click listener to focus a session
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int viewRow = table.getSelectedRow();
                    if (viewRow >= 0) {
                        int modelRow = table.convertRowIndexToModel(viewRow);
                        AnahataTopComponent tc = model.getTopComponentAt(modelRow);
                        tc.open();
                        tc.requestActive();
                    }
                }
            }
        });

        add(new JScrollPane(table), BorderLayout.CENTER);

        refreshTimer = new Timer(1000, e -> model.refresh());

        addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentResized(java.awt.event.ComponentEvent evt) {
                setColumnWidths();
            }
        });
    }

    private void setColumnWidths() {
        TableColumn statusColumn = table.getColumnModel().getColumn(LiveSessionsTableModel.STATUS_COL);
        statusColumn.setMinWidth(120);
        statusColumn.setMaxWidth(150);

        TableColumn msgColumn = table.getColumnModel().getColumn(LiveSessionsTableModel.MESSAGES_COL);
        msgColumn.setMinWidth(60);
        msgColumn.setMaxWidth(80);

        TableColumn ctxColumn = table.getColumnModel().getColumn(LiveSessionsTableModel.CONTEXT_COL);
        ctxColumn.setMinWidth(80);
        ctxColumn.setMaxWidth(100);
    }

    @Override
    public void componentOpened() {
        refreshTimer.start();
    }

    @Override
    public void componentClosed() {
        refreshTimer.stop();
    }

    // -- Session Tracking Logic --
    
    private void createNewSession() {
        AnahataTopComponent tc = new AnahataTopComponent();
        tc.open();
        tc.requestActive();
    }

    private void closeSelectedSession() {
        AnahataTopComponent tc = getSelectedTopComponent();
        if (tc != null) {
            tc.close();
        }
    }
    
    private void disposeSelectedSession() {
        AnahataTopComponent tc = getSelectedTopComponent();
        if (tc != null) {
            AnahataTopComponent.disposeSession(tc);
        }
    }
    
    private AnahataTopComponent getSelectedTopComponent() {
        int viewRow = table.getSelectedRow();
        if (viewRow >= 0) {
            int modelRow = table.convertRowIndexToModel(viewRow);
            return model.getTopComponentAt(modelRow);
        }
        return null;
    }
    
    private void updateButtonState() {
        AnahataTopComponent selected = getSelectedTopComponent();
        boolean isSelected = selected != null;
        
        disposeButton.setEnabled(isSelected);
        
        if (isSelected && selected.isOpened()) {
            closeButton.setEnabled(true);
        } else {
            closeButton.setEnabled(false);
        }
    }

    // -- Custom Cell Renderers --
    private static class StatusCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            if (value instanceof ChatStatus) {
                ChatStatus status = (ChatStatus) value;
                c.setForeground(SwingChatConfig.getColor(status));
                setText(status.toString());
            }
            return c;
        }
    }

    private static class ContextUsageCellRenderer extends DefaultTableCellRenderer {
        private final DecimalFormat PERCENT_FORMAT = new DecimalFormat("0.0%");

        public ContextUsageCellRenderer() {
            setHorizontalAlignment(JLabel.RIGHT);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

            if (value instanceof Double) {
                double percentage = (Double) value;
                setText(PERCENT_FORMAT.format(percentage));
                setForeground(SwingChatConfig.getColorForContextUsage(percentage));
            }
            return this;
        }
    }
}