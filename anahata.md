# Project: anahata-netbeans-ai - Anahata NetBeans AI Assistant Plugin

This document provides the essential, high-level overview of the `anahata-netbeans-ai` project. It is intended to be the stable, "always-in-context" guide to the plugin's purpose, architecture, and core principles.

## 1. Purpose
This project is the flagship host application for the `gemini-java-client`. It integrates the Anahata AI Assistant directly into the Apache NetBeans IDE, providing a deeply context-aware development partner.

## 2. Recent Milestones
- **V1 Website Launch:** Completed the `anahata.uno` website (hosted in `/docs`) with a high-impact design, categorized screenshots, and a "Sextete of Productivity" narrative.
- **Dual Licensing:** Implemented a dual-license model:
    - **Apache License 2.0:** For open-source community use.
    - **ASL V108 (The Immutable Edict):** A custom, personality-driven license for commercial use and "fair play" contributions.
- **Tech Stack Branding:** Integrated official NetBeans, Java, and Maven branding into the project's public presence.

## 3. Architectural Overview
The plugin is designed with a clear separation of concerns:
- `uno.anahata.ai.nb`: Central hub and UI (`AnahataTopComponent`).
- `uno.anahata.ai.nb.context`: Sensory system (ContextProviders).
- `uno.anahata.ai.nb.tools`: Actionable intelligence (IDE-specific AI tools).
- `uno.anahata.ai.nb.model`: Domain-driven DTOs and POJOs.
- `uno.anahata.ai.nb.mime`: Editor integration and syntax highlighting.

## 4. Coding Principles
- **Domain-Driven Design (DDD):** Models are expressive and contain domain logic.
- **Javadoc Integrity:** Documentation is a first-class citizen. Never remove existing Javadoc.
- **Butler Principle:** Safety and explicit consent are paramount.

## 5. Managing AI Tools
Available tools are registered in `NetBeansChatConfig.getToolClasses()`.

## 6. Very Important Notes
- **Do not "clean" the project** to avoid deleting runtime JARs.
- **nbmreload** is the preferred way to test changes to tools or dependencies.
- **Hot Reload:** Use `NetBeansProjectJVM.compileAndExecuteInProject` for rapid testing of logic within the IDE's JVM.
