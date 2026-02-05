<p align="center">
  <img src="docs/assets/messi_photo.png" width="800" alt="Messi Photo">
</p>

# 🚀 Code from the Heart: The Autonomous JVM Agent

[![Sponsor anahata-os](https://img.shields.io/badge/Sponsor-%E2%9D%A4-%23db61a2.svg?logo=GitHub)](https://github.com/sponsors/anahata-os)
[![Maven Central](https://img.shields.io/maven-central/v/uno.anahata/anahata-ai-nb)](https://central.sonatype.com/artifact/uno.anahata/anahata-ai-nb)
[![Javadoc](https://img.shields.io/badge/Javadoc-Reference-blue)](https://www.anahata.uno/apidocs/)
[![Deploy Javadoc](https://github.com/anahata-os/anahata-netbeans-ai/actions/workflows/javadoc.yml/badge.svg)](https://www.anahata.uno/apidocs/)

**Stop accepting "almost right." Demand an AI that lives inside your runtime.**

> [!IMPORTANT]
> **Latest Release: v28.0.18 (UI/UX Stability & Theme Overhaul)**
> This release introduces a theme-independent UI engine, theme persistence, and critical UI/UX stability fixes, including the "Modal Hang" resolution and high-visibility split pane dividers.

Anahata is an unprecedented, deeply integrated **Autonomous AI Agent** for the Apache NetBeans IDE. It's more than a chatbot—it's an **insider** that operates directly within your JVM, capable of executing LLM-generated Java code with any required classpath.

**[Website](https://anahata.uno) | [Anahata TV (YouTube)](https://www.youtube.com/@anahata108) | [Discord](https://discord.gg/M396BNtX) | [v2 is coming!](https://github.com/anahata-os/anahata-asi)**

---

## 🚀 The Insider Advantage: Runtime Agency

Anahata's main competitive advantage is its ability to **write, compile, and execute Java code directly within the NetBeans JVM**. It doesn't just suggest code; it **does** things.

### The "Any Classpath" Superpower
The agent can identify a need for a specific library, download it from Maven Central at runtime, and execute logic using it immediately—all without modifying your project's `pom.xml`.

### 🎯 Prompts that prove the power:
- **"Change the Look and Feel to FlatLaf IntelliJ Dark and refresh all windows."** (Direct UI manipulation)
- **"Set the java.util.logging levels of 'org.netbeans.modules.maven' to FINEST."** (Runtime diagnostics)
- **"Analyze the heap and find the largest objects currently in memory."** (JVM introspection)
- **"Download the sources for 'org.openide.util' and explain how the Lookup system works."** (Instant Source Surfing)
- **"Search Maven Central for a MIDI library and play a C major scale."** (Dynamic classpath expansion)

---

## ✨ The "Deep Strike" Philosophy: Beyond Autocomplete

While other AI tools are just glorified autocomplete, Anahata is a true IDE partner. We win by executing a "Deep Strike" beyond surface-level code suggestions, targeting the core of the development workflow.

<p align="center">
  <a href="docs/screenshots/deep-strike-splash.png"><img src="docs/screenshots/deep-strike-splash.png" width="80%" alt="The Deep Strike Philosophy"></a>
</p>

| Feature | GitHub Copilot, Tabnine, etc. | **Anahata NetBeans Plugin** |
| :--- | :--- | :--- |
| **Architecture** | Stateless Text Completion | **Autonomous JVM Agent** |
| **Execution** | Suggests code snippets | **Executes LLM-generated Java code in-process (JIT)** |
| **Source Intelligence** | Limited to training data | **Instant Source Surfing (Downloads & parses dependency sources)** |
| **Project Context** | Limited file visibility | **Full access to Maven Index, Classpath, and Diagnostics** |
| **Capabilities** | Text-based assistance | **Executes IDE actions, runs builds, performs refactoring** |

---

## 🛠️ Deep Java Intelligence: Mastering the Ecosystem

Anahata doesn't just guess; it *knows*. By leveraging NetBeans' deep understanding of Java, it provides assistance that is syntactically and semantically perfect.

<table align="center">
  <tr>
    <td align="center" width="50%">
      <a href="docs/screenshots/depency_sources.png"><img src="docs/screenshots/depency_sources.png" width="100%" alt="Instant Source Surfing"></a><br>
      <b>Instant Source Surfing</b><br>
      The AI downloads and reads the source of any dependency.
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

## 📺 Watch the Agent in Action
[![Anahata TV](docs/screenshots/beating_vscode.png)](https://www.youtube.com/watch?v=yav3jTbkfv4)
*(Click to watch Messi beating VS Code on Anahata TV)*

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
      <a href="docs/screenshots/diff-dialog-pom.png"><img src="docs/screenshots/diff-dialog-pom.png" width="100%" alt="Visual Diff Review"></a><br>
      <b>Visual Diff Review</b><br>
      See exactly what will change before it hits the disk.
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
  <b>Visca el Barça!</b> 🔵🔴
</p>
