# Anahata NetBeans AI Plugin - Task List

This document tracks all active, pending, and future work items for the `anahata-netbeans-ai` project.

## 1. V1 Launch Goals (Immediate Focus)

-   [ ] **Implement `AddProjectAction`:** The context menu action to add a single project to the chat is currently a non-functional placeholder. This needs to be implemented to dynamically create and register a project-specific context provider on demand.
-   [ ] **Fix `AnahataNodeFactory`:** This component currently has a memory leak and does not refresh its view when new files (like `anahata.md`) are added to the project root. This must be fixed.
-   [ ] **Node Decoration (High Priority):** The file decoration in the NetBeans project tree for files that are "in-context" is currently non-functional. This is a critical pre-launch feature. *Next Step: Research NetBeans Git module source for correct implementation pattern.*
-   [ ] **Local History Integration:** Implement a mechanism to write user and AI messages to the NetBeans local history for the relevant files being modified.
-   [ ] **Tab NickName and Color Coding:** Implement dynamic color-coding and nicknaming for `AnahataTopComponent` tabs based on the chat's status and session ID.
-   [ ] **Plugin Portal Submission:** Package the plugin and submit it to the Apache NetBeans Plugin Portal.
-   [ ] **Performance Tuning:** Investigate and improve the initial startup time of the `AnahataTopComponent`.
-   [ ] **UI Polish:**
    -   [ ] Implement the fix to display the `explanation` text in the `Coding.proposeChange` modal diff dialog (e.g., by wrapping it in a `TitledBorder`).

## 2. Future Tool Enhancements
-   [ ] **Parallelize Maven Artifact Downloads:** Review the `downloadProjectDependencies` tool to execute downloads in parallel to improve performance.

## 3. Current Task Board (As of 2025-11-19)

This section tracks our active work items to ensure continuity across sessions.

-   **[Done] Task A: Implement Session Manager UI & Rich Status Display:**
    -   **Status:** Done.
    -   **Description:** Created the new `AnahataInstancesTopComponent` to act as a session manager. It displays a live list of all active chat windows, shows their real-time status with color-coding, and allows for quick navigation by double-clicking. The main chat tabs also now dynamically update their color and tooltip to reflect the current status.

-   **[Done] Task J: Design and Refactor Maven Tools:**
    -   **Status:** Done.
    -   **Description:** The fragmented Maven tools (`Maven`, `MavenPom`, `MavenSearch`) have been consolidated into a single, robust `MavenTools` class. The old classes have been deprecated and de-registered from the `NetBeansChatConfig`.

-   **[On Hold] Task I: Test Schema Generation for `Tree` class:**
    -   **Status:** On Hold.
    -   **Description:** The investigation into serializing `com.google.genai.types` objects for debugging is complete. The findings have been documented in `jsonSchema.md`. This task is paused to focus on the higher-priority Maven tool redesign.

-   **[High Priority] Task B: Implement Granular Tool Status:**
    -   **Status:** To Do.
    -   **Description:** Enhance the status reporting to show which specific tool is executing (e.g., "Tool Execution (Maven.runGoals)...").
    -   **Next Step:** Add `setExecutingToolName(String)` to `StatusManager` and integrate it with `ToolManager` and the UI.

-   **[In Progress] Task N: Research Refactoring APIs:**
    -   **Status:** In Progress.
    -   **Description:** Before refactoring the `StatusListener`, we must research the NetBeans programmatic refactoring APIs.
    -   **Sub-Tasks:**
        -   [x] **Explore API:** Initial exploration of the `org.netbeans.modules.refactoring.api` package has begun.
        -   [ ] Investigate key classes and interfaces to discover available tools (rename, find usages, change parameters, etc.).
        -   [ ] Analyze the compatibility of these source-level tools with Lombok's annotation processing.
        -   [ ] Document findings in `netbeans.md` notes.


- **Refactor Tool Limitation:** The `Refactor.rename` tool fails programmatically when multiple open projects have conflicting/similar package names, due to ambiguity. The user can, however, perform the same refactoring successfully via the UI in such cases.