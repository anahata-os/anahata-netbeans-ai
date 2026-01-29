# Project: anahata-netbeans-ai - Anahata NetBeans AI Assistant Plugin (v1 plugin)

This document provides the essential, high-level overview of the `anahata-netbeans-ai` project. It is intended to be the stable, "always-in-context" guide to the plugin's purpose, architecture, and core principles.

## 1. Purpose
This project is the flagship host application for the `gemini-java-client`. It integrates the Anahata AI Assistant directly into the Apache NetBeans IDE, providing a deeply context-aware development partner.

## 2. Runtime Environment & Classpath Visibility
The plugin operates within the NetBeans module system. Access to other modules is strictly controlled by the `pom.xml`:
- **Public API Access:** Only modules listed in the `<dependencies>` section are accessible via their public packages.
- **Implementation Access:** Modules listed as `<type>impl</type>` in the `nbm-maven-plugin` configuration allow access to their private/internal packages.
- **Runtime Classpath:** The `RunningJVM` tool's default classpath is initialized to the plugin's runtime classpath. A grouped, pretty-printed view of the available JARs is provided in your context on every turn. Note that this is a summary; the actual underlying classpath contains hundreds of individual JARs. To retrieve the full, flat list of absolute paths, call `RunningJVM.getDefaultCompilerClasspath()`.

## 3. Recent Milestones
- **v28.0.11 (Performance & Stability):** Refined the "Autonomous JVM Agent" narrative. Improved JIT execution reliability and updated the visual documentation.
- **v28.0.10 (Autonomous Agent Transition):** Launched the "Autonomous JVM Agent" narrative. Added the Support Panel, Pruning Improvements (PAYG v2), and enhanced JIT execution capabilities.
- **GitHub Actions Migration (Modern Deployment):** Successfully transitioned from branch-based deployment to a direct GitHub Actions workflow. This enables automated Javadoc generation and injection into the website without polluting the `master` branch.
- **Automated Javadoc Integration:** Javadocs are now automatically generated and served at `www.anahata.uno/apidocs/` using a custom "merge" strategy in CI.
- **V1 Website Launch:** Completed the `anahata.uno` website (hosted in `/docs`) with a high-impact design, categorized screenshots, and a "Sextete of Productivity" narrative.
- **Dual Licensing:** Implemented a dual-license model (Apache 2.0 and ASL V108).
- **Tech Stack Branding:** Integrated official NetBeans, Java, and Maven branding into the project's public presence.

## 4. Architectural Overview
The plugin is designed with a clear separation of concerns:
- `uno.anahata.ai.nb`: Central hub and UI (`AnahataTopComponent`).
- `uno.anahata.ai.nb.context`: Sensory system (ContextProviders).
- `uno.anahata.ai.nb.tools`: Actionable intelligence (IDE-specific AI tools).
- `uno.anahata.ai.nb.model`: Domain-driven DTOs and POJOs.
- `uno.anahata.ai.nb.mime`: Editor integration and syntax highlighting.

## 5. CI/CD & Website Deployment
- **GitHub Actions:** The `.github/workflows/javadoc.yml` workflow manages the deployment.
- **Deployment Method:** Uses `actions/deploy-pages` for direct deployment from the build runner.
- **Custom Domain:** Uses `www.anahata.uno` (configured via `CNAME`).
- **Detailed Strategy**: See `ci.md` for the full CI/CD and Javadoc strategy.

## 6. Coding Principles
- **Domain-Driven Design (DDD):** Models are expressive and contain domain logic.
- **Javadoc Integrity:** Documentation is a first-class citizen. Never remove existing Javadoc.
- **Butler Principle:** Safety and explicit consent are paramount.

## 7. Managing AI Tools
Available tools are registered in `NetBeansChatConfig.getToolClasses()`.

## 8. Very Important Notes
- **Do not "clean" the project** to avoid deleting runtime JARs.
- **nbmreload** is the preferred way to test changes to tools or dependencies.
- **IMPORTANT (nbmreload & Context):** When you invoke `nbmreload`, the NetBeans module classloader disposes of all old classes. To preserve continuity, the current conversation context is automatically serialized to a Kryo file. After the reload, the new version of the plugin loads this saved context (using the same UUID) into a fresh `AnahataTopComponent` instance. **You must totally stop your current task** before reloading, as the `Chat` instance is entirely replaced.
- **IMPORTANT (gemini-java-client Dependency):** Since `gemini-java-client` is the main dependency of this plugin, you **must** run `maven clean install` on the `gemini-java-client` project before reloading the plugin if any changes have been made to the client. "Compile on Save" does not build the JAR, and `nbmreload` packages the plugin using JARs, not the `target/classes` directory.
- **Hot Reload:** Use `NetBeansProjectJVM.compileAndExecuteInProject` for rapid testing of logic within the IDE's JVM.
- **Note on Dependency Warnings:** You may see a warning about `aopalliance:asm:jar:9.8` being missing from the local repository. This is a known issue in the current NetBeans release and is fixed in the next version. You can safely ignore this warning.
