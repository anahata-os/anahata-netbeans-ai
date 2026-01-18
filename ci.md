# Continuous Integration & Deployment (CI/CD) - Anahata V1

## Artifact Publishing
The Anahata NetBeans Module (NBM) is published to **Sonatype Central Portal** via a GitHub Action (`deploy.yml`), triggered on release tags or manual dispatch.

### Deployment Paths
-   **Releases & Snapshots**: Published to the **Sonatype Central Portal** ecosystem.
-   **Credentials**: Uses the `sonatype-central` server ID for credential management in GitHub Actions.
-   **Verification**: The build uses the `central-publishing-maven-plugin` to handle the deferred deployment and portal integration.

## Website & Javadoc Deployment
The project website and Javadocs are deployed to **GitHub Pages** using the modern Actions-based deployment method.

-   **Workflow**: `.github/workflows/javadoc.yml`
-   **Custom Domain**: [https://www.anahata.uno](https://www.anahata.uno)
-   **Deployment Method**: Direct deployment from the build runner (no `gh-pages` branch).

### Javadoc Strategy
Javadocs are automatically generated and injected into the website's `apidocs/` folder during the deployment process.
-   **Storage Path**: `apidocs/`
-   **Theme**: Uses the custom `anahata-barca-theme.css` for a consistent Blaugrana aesthetic.
-   **Access**: [https://www.anahata.uno/apidocs/](https://www.anahata.uno/apidocs/)

## Current Status
-   **V1**: This project is the stable, production-ready version of Anahata.
-   **V2 Transition**: The V2 (JASI) portal is live at `asi.anahata.uno`.
