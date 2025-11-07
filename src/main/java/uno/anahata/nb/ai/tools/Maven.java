package uno.anahata.nb.ai.tools;

import java.io.File;
import java.io.IOException;
import java.util.Set;
import java.util.prefs.Preferences;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.resolver.ArtifactNotFoundException;
import org.apache.maven.artifact.resolver.ArtifactResolutionException;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.modules.maven.api.NbMavenProject;
import org.netbeans.modules.maven.embedder.EmbedderFactory;
import org.netbeans.modules.maven.embedder.MavenEmbedder;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbPreferences;
import uno.anahata.gemini.functions.AIToolMethod;

/**
 * Provides AI tool methods for interacting with Maven projects.
 * @author pablo
 */
public class Maven {

    @AIToolMethod("Gets the path to the Maven installation configured in NetBeans.")
    public static String getMavenPath() {
        try {
            Preferences prefs = NbPreferences.root().node("org/netbeans/modules/maven");
            return prefs.get("commandLineMavenPath", "PREFERENCE_NOT_FOUND");
        } catch (Throwable t) {
            return "EXECUTION_FAILED: " + t.toString();
        }
    }

    @AIToolMethod("Downloads all missing sources for a given Maven project's dependencies.")
    public static String downloadProjectSources(String projectId) throws Exception {
        Project project = findProject(projectId);
        NbMavenProject nbMavenProject = project.getLookup().lookup(NbMavenProject.class);
        if (nbMavenProject == null) {
            throw new IllegalStateException("Could not find NbMavenProject for project: " + projectId);
        }

        MavenEmbedder onlineEmbedder = EmbedderFactory.getOnlineEmbedder();
        Set<Artifact> artifacts = nbMavenProject.getMavenProject().getArtifacts();
        int successCount = 0;
        int failCount = 0;
        StringBuilder errors = new StringBuilder();

        for (Artifact art : artifacts) {
            if (downloadArtifactSource(onlineEmbedder, nbMavenProject, art, errors)) {
                successCount++;
            } else {
                failCount++;
            }
        }

        NbMavenProject.fireMavenProjectReload(project);
        return buildResultString("Project", projectId, successCount, failCount, errors);
    }

    @AIToolMethod("Downloads sources for a single, specific dependency of a given Maven project.")
    public static String downloadDependencySource(String projectId, String groupId, String artifactId) throws Exception {
        Project project = findProject(projectId);
        NbMavenProject nbMavenProject = project.getLookup().lookup(NbMavenProject.class);
        if (nbMavenProject == null) {
            throw new IllegalStateException("Could not find NbMavenProject for project: " + projectId);
        }

        MavenEmbedder onlineEmbedder = EmbedderFactory.getOnlineEmbedder();
        Set<Artifact> artifacts = nbMavenProject.getMavenProject().getArtifacts();
        int successCount = 0;
        int failCount = 0;
        StringBuilder errors = new StringBuilder();
        boolean found = false;

        for (Artifact art : artifacts) {
            if (art.getGroupId().equals(groupId) && art.getArtifactId().equals(artifactId)) {
                found = true;
                if (downloadArtifactSource(onlineEmbedder, nbMavenProject, art, errors)) {
                    successCount++;
                } else {
                    failCount++;
                }
                break; 
            }
        }

        if (!found) {
            return "Error: Dependency " + groupId + ":" + artifactId + " not found in project " + projectId;
        }

        NbMavenProject.fireMavenProjectReload(project);
        return buildResultString("Dependency", groupId + ":" + artifactId, successCount, failCount, errors);
    }

    private static boolean downloadArtifactSource(MavenEmbedder embedder, NbMavenProject project, Artifact art, StringBuilder errors) {
        if (Artifact.SCOPE_SYSTEM.equals(art.getScope())) {
            return false; // Skip system-scoped artifacts
        }
        try {
            Artifact sourcesArtifact = embedder.createArtifactWithClassifier(
                art.getGroupId(),
                art.getArtifactId(),
                art.getVersion(),
                art.getType(),
                "sources"
            );
            embedder.resolveArtifact(
                sourcesArtifact,
                project.getMavenProject().getRemoteArtifactRepositories(),
                embedder.getLocalRepository()
            );
            return true;
        } catch (ArtifactNotFoundException e) {
            errors.append("Sources not found for ").append(art.getId()).append("\n");
        } catch (ArtifactResolutionException e) {
            errors.append("Could not resolve sources for ").append(art.getId()).append(": ").append(e.getMessage()).append("\n");
        } catch (Exception e) {
            errors.append("An unexpected error occurred for ").append(art.getId()).append(": ").append(e.getMessage()).append("\n");
        }
        return false;
    }

    private static Project findProject(String id) {
        for (Project project : OpenProjects.getDefault().getOpenProjects()) {
            FileObject root = project.getProjectDirectory();
            if (root.getNameExt().equals(id)) {
                return project;
            }
        }
        throw new IllegalArgumentException("Project not found or not open: " + id);
    }
    
    private static String buildResultString(String targetType, String targetId, int success, int failed, StringBuilder errors) {
        String result = String.format("Source download for %s '%s' complete. Success: %d, Failed: %d.", targetType, targetId, success, failed);
        if (failed > 0) {
            result += "\nErrors:\n" + errors.toString();
        }
        return result;
    }
}
