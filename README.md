<p align="center">
  <img src="docs/assets/messi_photo.png" width="800" alt="Messi Photo">
</p>

# 🚀 Code from the Heart: The Autonomous JVM Agent

[![Sponsor anahata-os](https://img.shields.io/badge/Sponsor-%E2%9D%A4-%23db61a2.svg?logo=GitHub)](https://github.com/sponsors/anahata-os)
[![Maven Central](https://img.shields.io/maven-central/v/uno.anahata/anahata-ai-nb)](https://central.sonatype.com/artifact/uno.anahata/anahata-ai-nb)
[![Javadoc](https://img.shields.io/badge/Javadoc-Reference-blue)](https://www.anahata.uno/apidocs/)
[![Deploy Javadoc](https://github.com/anahata-os/anahata-netbeans-ai/actions/workflows/javadoc.yml/badge.svg)](https://www.anahata.uno/apidocs/)

**Stop accepting "almost right." Demand an AI that lives inside your runtime.**

Anahata is an unprecedented, deeply integrated **Autonomous AI Agent** for the Apache NetBeans IDE. It's more than a chatbot—it's an insider that operates directly within your JVM, capable of executing any Java code with any required classpath.

**[Website](https://anahata.uno) | [Anahata TV (YouTube)](https://www.youtube.com/@anahata108) | [Discord](https://discord.gg/M396BNtX)**

---

## 📢 Latest Release: 28.0.10 (gemini-java-client 1.0.9)

We've released **v28.0.10** today (Jan 26, 2026). This release marks the transition to the **Autonomous Agent** architecture:

- **v28.0.10:** Added **Support Panel**, **Pruning Improvements** (PAYG v2), and the **Autonomous JVM Agent** engine.
- **v28.0.9:** Internal stability fixes for session persistence and improved tool call batching.
- **v28.0.8:** gemini-java-client 1.0.8 upgrade (Kill switch, togglable tools, support panel), session nicknames, agent spawning, and compact maven build summaries.

### ⚡ Instant Updates & Critical Hotfixes
Get the latest Anahata features and essential bug fixes the moment they're released to Maven Central.

<a href="https://anahata.uno/instant_updates.html" target="_blank" style="text-decoration: none;">
    <img src="https://img.shields.io/maven-central/v/uno.anahata/anahata-ai-nb" alt="Maven Central Latest Version" style="height: 25px; vertical-align: middle; margin-right: 10px;">
</a>
<span style="font-size: 1rem; color: #333;">Latest Version on Maven Central</span>

---

## 🚀 The Killer Advantage: Autonomous JVM Execution

Anahata's main competitive advantage is its ability to **write, compile, and execute Java code directly within the NetBeans JVM**. It doesn't just suggest code; it **does** things.

### The "Any Classpath" Superpower
The agent can identify a need for a specific library, download it from Maven Central at runtime, and execute logic using it immediately—all without modifying your project's `pom.xml`.

### 🎯 Prompts that prove the power:
- **"Change the Look and Feel to FlatLaf IntelliJ Dark and refresh all windows."** (Direct UI manipulation)
- **"Set the java.util.logging levels of 'org.netbeans.modules.maven' to FINEST."** (Runtime diagnostics)
- **"Analyze the heap and find the largest objects currently in memory."** (JVM introspection)
- **"Create a custom Swing component and inject it into the main window."** (Runtime environment extension)
- **"Search Maven Central for a MIDI library and play a C major scale."** (Dynamic classpath expansion)

---

## ✨ The "Deep Strike" Philosophy: Beyond Autocomplete

While other AI tools are just glorified autocomplete, Anahata is a true IDE partner. We win by executing a "Deep Strike" beyond surface-level code suggestions, targeting the core of the development workflow.

<p align="center">
  <a href="docs/screenshots/deep-strike-splash.png"><img src="docs/screenshots/deep-strike-splash.png" width="80%" alt="The Deep Strike Philosophy"></a>
</p>

|  | GitHub Copilot, Tabnine, etc. | **Anahata NetBeans Plugin** |
| :--- | :--- | :--- |
| **Architecture** | Stateless Text Completion | **Autonomous JVM Agent** |
| **Execution** | Suggests code snippets | **Compiles & Executes Java code in-process (JIT)** |
| **Project Context** | Limited file visibility | **Full access to Maven Index, Classpath, and Diagnostics** |
| **Capabilities** | Text-based assistance | **Executes IDE actions, runs builds, performs refactoring** |

---

## 🧠 Intelligent Context Management: The AI's Memory

Anahata features a sophisticated, AI-driven **Prune-As-You-Go (PAYG) v2** algorithm. Unlike other assistants that hit a "token wall" and lose their memory, Anahata dynamically manages its context window in real-time.

<table align="center">
  <tr>
    <td align="center" width="50%">
      <a href="docs/screenshots/context-heatmap-pie.png"><img src="docs/screenshots/context-heatmap-pie.png" width="100%" alt="Token Usage at a Glance"></a><br>
      <b>Visual Token Transparency</b><br>
      Full breakdown of how your context is being used.
    </td>
    <td align="center" width="50%">
      <a href="docs/screenshots/context-providers-tab.png"><img src="docs/screenshots/context-providers-tab.png" width="100%" alt="Dynamic Context Providers"></a><br>
      <b>Dynamic Awareness</b><br>
      Anahata pulls context from Maven, Git, and the Editor.
    </td>
  </tr>
  <tr>
    <td align="center">
      <a href="docs/screenshots/prune-as-you-go.png"><img src="docs/screenshots/prune-as-you-go.png" width="100%" alt="PAYG Algorithm in Action"></a><br>
      <b>Prune-As-You-Go (PAYG)</b><br>
      The AI keeps its own workspace tidy and efficient.
    </td>
    <td align="center">
      <a href="docs/screenshots/context-heatmap-detailed.png"><img src="docs/screenshots/context-heatmap-detailed.png" width="100%" alt="Deep Context Inspection"></a><br>
      <b>Deep Inspection</b><br>
      Drill down into every message and part in the history.
    </td>
  </tr>
</table>

---

## 🛠️ Deep Java Intelligence: Mastering the Ecosystem

Anahata doesn't just guess; it *knows*. By leveraging NetBeans' deep understanding of Java, it provides assistance that is syntactically and semantically perfect.

<table align="center">
  <tr>
    <td align="center" width="50%">
      <a href="docs/screenshots/tools-tab-javasources.png"><img src="docs/screenshots/tools-tab-javasources.png" width="100%" alt="Source-Level Mastery"></a><br>
      <b>Source-Level Mastery</b><br>
      Direct access to project sources and dependencies.
    </td>
    <td align="center" width="50%">
      <a href="docs/screenshots/live-sessions-maven-integration.png"><img src="docs/screenshots/live-sessions-maven-integration.png" width="100%" alt="Maven Integration"></a><br>
      <b>Maven Ecosystem</b><br>
      Trigger goals and manage dependencies via natural language.
    </td>
  </tr>
  <tr>
    <td align="center">
      <a href="docs/screenshots/refactor_rename.png"><img src="docs/screenshots/refactor_rename.png" width="100%" alt="Safe Refactoring"></a><br>
      <b>Safe Refactoring</b><br>
      Rename classes and members using native NetBeans APIs.
    </td>
    <td align="center" width="50%">
      <a href="docs/screenshots/tools-tab-javadocs.png"><img src="docs/screenshots/tools-tab-javadocs.png" width="100%" alt="Javadoc Awareness"></a><br>
      <b>Javadoc Awareness</b><br>
      The AI reads and writes documentation as a first-class citizen.
    </td>
  </tr>
</table>

---

## 🛡️ Safety & Trust: The Butler Principle

We believe in **Explicit Consent**. Anahata never acts without your approval. Every tool execution and code change is presented for your review.

<table align="center">
  <tr>
    <td align="center" width="50%">
      <a href="docs/screenshots/explicit-consent-dialog.png"><img src="docs/screenshots/explicit-consent-dialog.png" width="100%" alt="Explicit Consent"></a><br>
      <b>Explicit Consent</b><br>
      You are always in control of what the AI executes.
    </td>
    <td align="center" width="50%">
      <a href="docs/screenshots/suggest-change-confirmation.png"><img src="docs/screenshots/suggest-change-confirmation.png" width="100%" alt="Interactive Code Proposals"></a><br>
      <b>Interactive Proposals</b><br>
      Review AI-generated code changes with a single click.
    </td>
  </tr>
  <tr>
    <td align="center">
      <a href="docs/screenshots/diff-dialog-pom.png"><img src="docs/screenshots/diff-dialog-pom.png" width="100%" alt="Visual Diff Review"></a><br>
      <b>Visual Diff Review</b><br>
      See exactly what will change before it hits the disk.
    </td>
    <td align="center">
      <a href="docs/screenshots/tool-confirmation-overview.png"><img src="docs/screenshots/tool-confirmation-overview.png" width="100%" alt="Full Audit Trail"></a><br>
      <b>Full Audit Trail</b><br>
       a transparent history of every tool call and its outcome.
    </td>
  </tr>
</table>

---

## ❤️ Support Operation "Deep Strike"

This ambitious, community-driven project thrives on your support. If you believe in the mission of creating a truly intelligent development partner, please consider contributing.

-   **[Sponsor us on GitHub](https://github.com/sponsors/anahata-os)**: The most direct way to fuel our continued innovation.
-   **[Join our Discord Server](https://discord.gg/M396BNtX)**: Connect with the community and get real-time support.
-   **Commercial Licensing**: For proprietary, closed-source applications, please refer to the **[Anahata Software License (ASL) V108](ASL_108.md)**.

---
<p align="center">
  <b>Visca el Barça!</b>
</p>
