# Announcing Anahata: A Pure-Java, Open Source AI Assistant for Apache NetBeans

![Anahata Logo](http://anahata.uno/assets/logo-horizontal.png)

**By Rishi Priyadarshi**

After months of intensive development, I am thrilled to announce that **Anahata**, our Open Source, Pure-Java AI Assistant, has officially been released and is now available on the **Apache NetBeans Plugin Portal**.

Anahata isn't just another "chat-in-a-sidebar" plugin. It is a deeply integrated, context-aware development partner designed specifically for the Java ecosystem, running on the latest **NetBeans 28**.

## Why Anahata?

The vision for Anahata was simple but ambitious: build an AI agent that understands the developer's environment as well as they do. To achieve this, we built it from the ground up using **Pure Java**, licensed under the **Apache License 2.0**, ensuring it remains a first-class citizen of the Open Source community.

## Key Features for the Modern Java Developer

### 1. Deep Contextual Awareness
Anahata doesn't just see your code; it understands your project. Through its advanced Context Providers, it has real-time access to:
*   **Project Overviews:** Full source trees and Maven dependency graphs.
*   **Live Diagnostics:** Javac alerts and project-level problems.
*   **IDE State:** Open tabs, output window content, and even live screenshots of your running application.

![Augmented Context](http://anahata.uno/screenshots/context-providers-tab_refined.png)

### 2. Local Tool Execution ("Actionable Intelligence")
Anahata can do more than talk—it can act. It comes equipped with a suite of local tools that allow it to:
*   **Refactor Code:** Perform safe renames and moves using the NetBeans Refactoring API.
*   **Manage Dependencies:** Search the Maven index and update your `pom.xml` automatically.
*   **Execute Logic:** Compile and run Java code snippets directly within the IDE's JVM for rapid prototyping.

![Maven Mastery](http://anahata.uno/screenshots/live-sessions-maven-integration_refined.png)

### 3. Safety & Transparency: The Butler Principle
We believe that an AI should be a helpful butler, not an autonomous loose cannon. Anahata follows the **Butler Principle**:
*   **Explicit Consent:** Every tool call requires your approval.
*   **Native Diffing:** Proposed code changes are presented in the standard NetBeans diff dialog for your review.
*   **Full Visibility:** You can see exactly what context is being sent to the model at any time.

![Intelligent Patching](http://anahata.uno/screenshots/diff-dialog-pom_refined.png)

### 4. Visual Intelligence & Creative Flow
We believe productivity is tied to state-of-mind. Anahata includes:
*   **Visual Context:** The ability to capture and analyze IDE windows or host screens to help debug UI issues.
*   **Integrated Radio & DJ:** A built-in creative dashboard to keep you in the "flow" with music, directly from your assistant.

![Integrated Radio](http://anahata.uno/screenshots/radio-tool-active_refined.png)

## Built on a Solid Foundation

Anahata is built using the best-of-breed technologies in the Java ecosystem:

| **Apache NetBeans** | **Apache Maven** | **Google Gemini** |
|:---:|:---:|:---:|
| ![NetBeans](http://anahata.uno/assets/logos/apache-netbeans.svg) | ![Maven](https://maven.apache.org/images/maven-logo-black-on-white.png) | ![Gemini](http://anahata.uno/assets/logos/gemini.svg) |

Anahata is powered by the **Gemini Java Client**, an enterprise-grade framework we developed to bring Google's Gemini models into the Java world with type-safety and annotation-driven tool support.

## Get Started Today
Anahata is now live and ready to transform your development workflow. You can find it directly in the NetBeans Plugin Manager or via the official portal:

👉 **[Anahata on the NetBeans Plugin Portal](https://plugins.netbeans.apache.org/catalogue/?id=108)**

We invite the Foojay community to take it for a spin, contribute to the code, and help us shape the future of AI-assisted development in the OpenJDK ecosystem.

***

![Visca el Barça!](http://anahata.uno/assets/goat.png)
