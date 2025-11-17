package uno.anahata.ai.nb;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Image;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JToolBar;
import javax.swing.ListSelectionModel;
import javax.swing.Timer;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableRowSorter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.util.ImageUtilities;
import org.openide.windows.Mode;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;
import uno.anahata.gemini.Chat;
import uno.anahata.gemini.context.ContextManager;
import uno.anahata.gemini.status.ChatStatus;
import uno.anahata.gemini.ui.SwingChatConfig;

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
    private final DefaultTableModel model;
    private final Timer refreshTimer;
    private final JButton discardButton;

    private static final int SESSION_COL = 0;
    private static final int STATUS_COL = 1;
    private static final int MESSAGES_COL = 2;
    private static final int CONTEXT_COL = 3;
    private static final int UUID_COL = 4;

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
            newSessionIcon = ImageUtilities.mergeImages(anahataIcon, addIcon, 0, 0);
        }

        JButton newButton = new JButton("New");
        newButton.setToolTipText("Create a new AI session");
        if (newSessionIcon != null) {
            newButton.setIcon(new ImageIcon(newSessionIcon));
        }
        newButton.addActionListener(e -> createNewSession());
        toolBar.add(newButton);

        discardButton = new JButton("Discard");
        discardButton.setToolTipText("Close the selected AI session");
        discardButton.setIcon(ImageUtilities.loadImageIcon("org/openide/actions/delete.gif", true));
        discardButton.addActionListener(e -> discardSelectedSession());
        discardButton.setEnabled(false); // Disabled by default
        toolBar.add(discardButton);

        add(toolBar, BorderLayout.NORTH);

        String[] columnNames = {"Session", "Status", "Messages", "Context %", "UUID"};
        model = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Table is now read-only
            }

            @Override
            public Class<?> getColumnClass(int columnIndex) {
                switch (columnIndex) {
                    case SESSION_COL: return String.class;
                    case STATUS_COL: return ChatStatus.class;
                    case MESSAGES_COL: return Integer.class;
                    case CONTEXT_COL: return Double.class;
                    case UUID_COL: return String.class;
                    default: return Object.class;
                }
            }
        };

        table = new JTable(model);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setFillsViewportHeight(true);
        
        // Selection listener to enable/disable discard button
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                discardButton.setEnabled(table.getSelectedRow() != -1);
            }
        });

        // Custom Renderers
        table.setDefaultRenderer(ChatStatus.class, new StatusCellRenderer());
        table.getColumnModel().getColumn(CONTEXT_COL).setCellRenderer(new ContextUsageCellRenderer());

        // Hide the UUID column
        TableColumn uuidColumn = table.getColumnModel().getColumn(UUID_COL);
        uuidColumn.setMinWidth(0);
        uuidColumn.setMaxWidth(0);
        uuidColumn.setWidth(0);
        uuidColumn.setPreferredWidth(0);

        // Sorting
        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(model);
        table.setRowSorter(sorter);
        sorter.setComparator(CONTEXT_COL, Comparator.comparingDouble(d -> (Double) d));
        sorter.setSortKeys(List.of(new javax.swing.RowSorter.SortKey(SESSION_COL, javax.swing.SortOrder.ASCENDING)));

        // Double-click listener to focus a session
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int viewRow = table.getSelectedRow();
                    if (viewRow >= 0) {
                        int modelRow = table.convertRowIndexToModel(viewRow);
                        String sessionUuid = (String) model.getValueAt(modelRow, UUID_COL);
                        findTopComponentByUuid(sessionUuid).ifPresent(TopComponent::requestFocus);
                    }
                }
            }
        });

        add(new JScrollPane(table), BorderLayout.CENTER);

        refreshTimer = new Timer(1000, e -> refreshTable());

        addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentResized(java.awt.event.ComponentEvent evt) {
                setColumnWidths();
            }
        });
    }

    private void setColumnWidths() {
        TableColumn statusColumn = table.getColumnModel().getColumn(STATUS_COL);
        statusColumn.setMinWidth(120);
        statusColumn.setMaxWidth(150);

        TableColumn msgColumn = table.getColumnModel().getColumn(MESSAGES_COL);
        msgColumn.setMinWidth(60);
        msgColumn.setMaxWidth(80);

        TableColumn ctxColumn = table.getColumnModel().getColumn(CONTEXT_COL);
        ctxColumn.setMinWidth(80);
        ctxColumn.setMaxWidth(100);
    }

    /**
     * Performs an intelligent refresh of the table, updating, adding, and removing rows
     * without rebuilding the entire model. This prevents UI interruptions.
     */
    private void refreshTable() {
        try {
            Map<String, AnahataTopComponent> activeTcs = getActiveTopComponents().stream()
                    .filter(tc -> tc.getChat() != null)
                    .collect(Collectors.toMap(AnahataTopComponent::getSessionUuid, Function.identity()));

            // Remove closed sessions
            for (int i = model.getRowCount() - 1; i >= 0; i--) {
                String uuidInTable = (String) model.getValueAt(i, UUID_COL);
                if (!activeTcs.containsKey(uuidInTable)) {
                    model.removeRow(i);
                }
            }

            // Update existing and add new sessions
            activeTcs.forEach((uuid, tc) -> {
                Chat chat = tc.getChat();
                ContextManager cm = chat.getContextManager();
                double percentage = cm.getTokenThreshold() == 0 ? 0 : (double) cm.getTotalTokenCount() / cm.getTokenThreshold();
                String displayName = StringUtils.isNotBlank(chat.getNickname()) ? chat.getNickname() : chat.getShortId();

                Optional<Integer> existingRow = findRowByUuid(uuid);
                if (existingRow.isPresent()) {
                    // Update existing row
                    int row = existingRow.get();
                    model.setValueAt(displayName, row, SESSION_COL);
                    model.setValueAt(chat.getStatusManager().getCurrentStatus(), row, STATUS_COL);
                    model.setValueAt(cm.getContext().size(), row, MESSAGES_COL);
                    model.setValueAt(percentage, row, CONTEXT_COL);
                } else {
                    // Add new row
                    model.addRow(new Object[]{
                        displayName,
                        chat.getStatusManager().getCurrentStatus(),
                        cm.getContext().size(),
                        percentage,
                        uuid
                    });
                }
            });
        } catch (Exception e) {
            log.error("Error refreshing session table", e);
        }
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
        tc.requestFocus();
    }

    private void discardSelectedSession() {
        int viewRow = table.getSelectedRow();
        if (viewRow >= 0) {
            int modelRow = table.convertRowIndexToModel(viewRow);
            String sessionUuid = (String) model.getValueAt(modelRow, UUID_COL);
            findTopComponentByUuid(sessionUuid).ifPresent(TopComponent::close);
        }
    }
    
    private Optional<Integer> findRowByUuid(String uuid) {
        for (int i = 0; i < model.getRowCount(); i++) {
            if (uuid.equals(model.getValueAt(i, UUID_COL))) {
                return Optional.of(i);
            }
        }
        return Optional.empty();
    }

    /**
     * Robustly finds all open AnahataTopComponent instances by iterating through all modes.
     */
    private List<AnahataTopComponent> getActiveTopComponents() {
        List<AnahataTopComponent> tcs = new ArrayList<>();
        WindowManager wm = WindowManager.getDefault();
        for (Mode mode : wm.getModes()) {
            for (TopComponent tc : wm.getOpenedTopComponents(mode)) {
                if (tc instanceof AnahataTopComponent) {
                    tcs.add((AnahataTopComponent) tc);
                }
            }
        }
        return tcs;
    }

    private Optional<AnahataTopComponent> findTopComponentByUuid(String uuid) {
        if (StringUtils.isBlank(uuid)) {
            return Optional.empty();
        }
        return getActiveTopComponents().stream()
                .filter(tc -> uuid.equals(tc.getSessionUuid()))
                .findFirst();
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
