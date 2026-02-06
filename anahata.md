/* Licensed under the Anahata Software License (ASL) v 108. See the LICENSE file for details. Força Barça! */
# Project: anahata-netbeans-ai - Anahata NetBeans AI Assistant Plugin (v1 plugin)

This document provides the essential, high-level overview of the `anahata-netbeans-ai` project. It is intended to be the stable, "always-in-context" guide to the plugin's purpose, architecture, and core principles.

## 1. Purpose
This project is the flagship host application for the `gemini-java-client`. It integrates the Anahata AI Assistant directly into the Apache NetBeans IDE, providing a deeply context-aware development partner.

## 2. Git & Release Coordination (CRITICAL)
- **Primary Branch:** The main development branch for this project is **`master`** (not `main`).
- **Release Synchronization:** When releasing a new version of the `gemini-java-client` library, you **MUST WAIT at least 5 to 10 minutes** before pushing the corresponding plugin update to GitHub.
- **Rationale:** The GitHub Actions build for the plugin will fail if it cannot find the newly released library artifact in Maven Central. Central synchronization takes time; pushing too early will break the CI/CD pipeline.

## 3. Runtime Environment & Classpath Visibility
The plugin operates within the NetBeans module system. Access to other modules is strictly controlled by the `pom.xml`:
- **Public API Access:** Only modules listed in the `<dependencies>` section are accessible via their public packages.
- **Implementation Access:** Modules listed as `<type>impl</type>` in the `nbm-maven-plugin` configuration allow access to their private/internal packages.
- **Runtime Classpath:** The `RunningJVM` tool's default classpath is initialized to the plugin's runtime classpath. A grouped, pretty-printed view of the available JARs is provided in your context on every turn. Note that this is a summary; the actual underlying classpath contains hundreds of individual JARs. To retrieve the full, flat list of absolute paths, call `RunningJVM.getDefaultCompilerClasspath()`.

## 4. Recent Milestones
- **v28.1.0 (First Stable Version):** Marks the transition to a stable production state. Includes the new Vector Icon System, enhanced PAYG v2 context management, and critical UI/UX stability fixes.
- **v28.0.14 (Polymorphic Code Discovery):** Upgraded the `CodeModel` to use a polymorphic "Keychain" pattern. `JavaMember` now extends `JavaType`, allowing for recursive, zero-turn exploration of nested and anonymous inner classes across projects, dependencies, and the JDK.
- **v28.0.13 (Context Safety & Intelligence):** Implemented the "90% Soft Limit" for context window management. Added Dynamic Model Intelligence to automatically discover model-specific token limits from the Gemini API.
- **v28.0.12 (Performance & Stability):** Optimized project alerts retrieval by 'surfing' the IDE's internal ErrorsCache, reducing turn latency from seconds to milliseconds.
- **v28.0.11 (Performance & Stability):** Refined the "Autonomous JVM Agent" narrative. Improved JIT execution reliability and updated the visual documentation.
- **v28.0.10 (Autonomous Agent Transition):** Launched the "Autonomous JVM Agent" narrative. Added the Support Panel, Pruning Improvements (PAYG v2), and enhanced JIT execution capabilities.

## 5. Architectural Overview
The plugin is designed with a clear separation of concerns:
- `uno.anahata.ai.nb`: Central hub and UI (`AnahataTopComponent`).
- `uno.anahata.ai.nb.context`: Sensory system (ContextProviders).
- `uno.anahata.ai.nb.tools`: Actionable intelligence (IDE-specific AI tools).
- `uno.anahata.ai.nb.model`: Domain-driven DTOs and POJOs.
- `uno.anahata.ai.nb.mime`: Editor integration and syntax highlighting.

### 5.1. The "Keychain" Pattern (`JavaType`)
The `JavaType` class implements a "Keychain" pattern for type identity, combining an `ElementHandle` with the `URL` of the class file. This ensures that a type's identity is globally resolvable across different classpaths (Project, Maven, JDK) and remains stable across multiple conversation turns.

## 6. CI/CD & Website Deployment
- **GitHub Actions:** The `.github/workflows/javadoc.yml` workflow manages the deployment.
- **Deployment Method:** Uses `actions/deploy-pages` for direct deployment from the build runner.
- **Custom Domain:** Uses `www.anahata.uno` (configured via `CNAME`).
- **Detailed Strategy**: See `ci.md` for the full CI/CD and Javadoc strategy.

## 7. Coding Principles
- **Domain-Driven Design (DDD):** Models are expressive and contain domain logic.
- **Javadoc Integrity:** Documentation is a first-class citizen. Never remove existing Javadoc.
- **Butler Principle:** Safety and explicit consent are paramount.

## 8. Managing AI Tools
Available tools are registered in `NetBeansChatConfig.getToolClasses()`.

## 9. Very Important Notes (Plugin Development Protocol)

> [!DANGER]
> **CRITICAL: THE RUNTIME BOUNDARY RULE**
> Changing a file in this plugin project or its dependencies **DOES NOT** automatically update the tools you are currently using. The IDE's tool registry is static until the module is reloaded.

- **Do not "clean" the project** to avoid deleting runtime JARs.
- **nbmreload** is the **MANDATORY** way to deploy changes to tools or dependencies.

### 9.1. The Reload Protocol
1.  **Modify & Verify:** Use `suggestChange` followed by `NetBeansProjectJVM.compileAndExecuteInProject` to verify the logic.
2.  **Check for Errors:** Before reloading, ensure the `Project Alerts` context provider shows **zero** compilation errors.
3.  **Invoke Reload:** Call `Projects.invokeAction(projectId="anahata-netbeans-ai", action="nbmreload")`.
4.  **STOP IMMEDIATELY:** Once you receive the `FunctionResponse` for `nbmreload`, you are **FORBIDDEN** from calling any more tools or continuing the conversation in that turn. The current `Chat` instance is marked for garbage collection.
5.  **WAIT FOR RESTORATION:** You must wait for the next **real user message** on the new chat instance and new classloader before proceeding with any tasks or demonstrations.

### 9.2. Dependency Handling
- **gemini-java-client:** Since this is the main dependency, you **must** run `maven clean install` on the `gemini-java-client` project before reloading the plugin if it was modified. "Compile on Save" is insufficient for `nbmreload`.

## 10. Note on Dependency Warnings
You may see a warning about `aopalliance:asm:jar:9.8` being missing from the local repository. This is a known issue in the current NetBeans release and is fixed in the next version. You can safely ignore this warning.

## 11. THE "NEVER-TRUE" DEPENDENCY RULE

> [!DANGER]
> **CRITICAL: CLASSPATH SAFETY**
> When using `NetBeansProjectJVM.compileAndExecuteInProject` on this project, the `includeCompileAndExecuteDependencies` flag **MUST** be set to `false`. 
> 
> **Rationale:** The plugin's parent classloader already provides all necessary NetBeans APIs and dependencies. Setting this to `true` will cause `LinkageError`s and break critical IDE services (like `URLMapper`). The project's `target/classes` is always included automatically.
