# Anahata Session Navigator - Design Notes

This document outlines the design and implementation plan for the new Anahata Session Navigator, which will replace the obsolete `AnahataNavigatorTopComponent`.

## 1. Core Requirements

- **View All Active Sessions:** Display a list of all currently open `AnahataTopComponent` instances.
- **Real-time Status:** Show the live status of each chat session (e.g., Idle, API Call, Tool Execution).
- **Session Metrics:** Display key metrics for each session, such as total message count and context window size (token count).
- **Interactivity:**
    - Allow focusing a specific chat window by double-clicking it in the list.
    - Provide a "New Chat" button to open a new `AnahataTopComponent` instance.
- **Data Source:** The navigator will get its data by querying the NetBeans `WindowManager.getDefault().getRegistry()` and filtering for `AnahataTopComponent` instances, as per the user's recommendation. This avoids the need for a custom static registry.

## 2. UI Design

- The main component will be a `JTable` or a `JList` with a custom renderer.
- **Columns:**
    - `Session ID` (shortened UUID)
    - `Status` (with color-coding and timers)
    - `Messages` (count)
    - `Tokens` (count)
- A toolbar will contain the "New Chat" action.

## 3. Implementation Plan

1.  Create the new `AnahataInstancesTopComponent` class.
2.  Add a `JTable` to its layout.
3.  Implement a `TableModel` that is populated by querying the `WindowManager`.
4.  Create a custom `TableCellRenderer` for the `Status` column.
5.  Implement a listener to refresh the table data periodically or on specific events.
6.  Implement the "New Chat" and "Focus Chat" actions.
