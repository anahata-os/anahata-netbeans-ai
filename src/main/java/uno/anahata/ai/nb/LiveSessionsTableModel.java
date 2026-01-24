/* Licensed under the Apache License, Version 2.0 */
package uno.anahata.ai.nb;

import java.util.ArrayList;
import java.util.List;
import javax.swing.table.AbstractTableModel;
import uno.anahata.ai.Chat;
import uno.anahata.ai.status.ChatStatus;

/**
 * Table model for displaying active Anahata AI sessions.
 */
public class LiveSessionsTableModel extends AbstractTableModel {

    private List<AnahataTopComponent> sessions = new ArrayList<>();
    private final String[] columnNames = {"Session", "TC ID", "Status", "Messages", "Context %", "State"};

    /** Column index for the session nickname. */
    public static final int SESSION_COL = 0;
    /** Column index for the TopComponent ID. */
    public static final int TC_ID_COL = 1;
    /** Column index for the chat status. */
    public static final int STATUS_COL = 2;
    /** Column index for the message count. */
    public static final int MESSAGES_COL = 3;
    /** Column index for the context window usage percentage. */
    public static final int CONTEXT_COL = 4;
    /** Column index for the TopComponent open/closed state. */
    public static final int STATE_COL = 5;

    @Override
    public int getRowCount() {
        return sessions.size();
    }

    @Override
    public int getColumnCount() {
        return columnNames.length;
    }

    @Override
    public String getColumnName(int column) {
        return columnNames[column];
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        switch (columnIndex) {
            case MESSAGES_COL:
                return Integer.class;
            case CONTEXT_COL:
                return Double.class;
            case STATUS_COL:
                return ChatStatus.class;
            default:
                return String.class;
        }
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        AnahataTopComponent tc = sessions.get(rowIndex);
        Chat chat = tc.getChat();

        // Handle case where chat is not yet initialized
        if (chat == null && columnIndex == STATE_COL) {
            return tc.isOpened() ? "Open" : "Closed";
        }

        if (chat == null) {
            return "Initializing...";
        }

        switch (columnIndex) {
            case SESSION_COL:
                return chat.getNickname(); // Use the new display name method
            case TC_ID_COL:
                return tc.getTopComponentId();
            case STATUS_COL:
                return chat.getStatusManager().getCurrentStatus();
            case MESSAGES_COL:
                return chat.getContextManager().getContext().size();
            case CONTEXT_COL:
                double p = (double) chat.getContextWindowUsage();
                return p;
            case STATE_COL:
                return tc.isOpened() ? "Open" : "Closed";
            default:
                return null;
        }
    }

    /**
     * Refreshes the table model intelligently, preserving selection by firing
     * granular events.
     */
    public void refresh() {
        List<AnahataTopComponent> newSessions = AnahataTopComponent.getAllSessions();
        
        // Identify removed sessions
        for (int i = sessions.size() - 1; i >= 0; i--) {
            if (!newSessions.contains(sessions.get(i))) {
                sessions.remove(i);
                fireTableRowsDeleted(i, i);
            }
        }

        // Identify added sessions
        for (int i = 0; i < newSessions.size(); i++) {
            AnahataTopComponent tc = newSessions.get(i);
            if (!sessions.contains(tc)) {
                sessions.add(i, tc);
                fireTableRowsInserted(i, i);
            }
        }

        // Identify updated rows (all of them for simplicity in this case)
        if (!sessions.isEmpty()) {
            fireTableRowsUpdated(0, sessions.size() - 1);
        }
    }

    /**
     * Returns the AnahataTopComponent at the specified row.
     * @param row the row index.
     * @return the AnahataTopComponent, or null if the row is out of bounds.
     */
    public AnahataTopComponent getTopComponentAt(int row) {
        if (row >= 0 && row < sessions.size()) {
            return sessions.get(row);
        }
        return null;
    }
}
