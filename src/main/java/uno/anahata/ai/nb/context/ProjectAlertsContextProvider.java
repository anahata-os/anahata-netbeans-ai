/* Licensed under the Apache License, Version 2.0 */
package uno.anahata.ai.nb.context;

import com.google.genai.types.Part;
import java.util.Collections;
import java.util.List;
import lombok.Getter;
import lombok.SneakyThrows;
import org.netbeans.api.java.source.SourceUtils;
import uno.anahata.ai.Chat;
import uno.anahata.ai.context.provider.ContextPosition;
import uno.anahata.ai.context.provider.ContextProvider;
import uno.anahata.ai.internal.GsonUtils;
import uno.anahata.ai.nb.model.ide.ProjectDiagnostics;
import uno.anahata.ai.nb.tools.IDE;
import uno.anahata.ai.nb.tools.Projects;

@Getter
public class ProjectAlertsContextProvider extends ContextProvider {

    String projectId;

    public ProjectAlertsContextProvider(String projectId) {
        super(ContextPosition.AUGMENTED_WORKSPACE);
        this.projectId = projectId;
    }

    @Override
    public String getId() {
        return projectId + "-alerts";
    }

    @Override
    public String getDisplayName() {
        return projectId + " Alerts";
    }

    @Override
    @SneakyThrows
    public List<Part> getParts(Chat chat) {

        if (Projects.getOpenProjects().contains(projectId)) {
            // Wait for indexing and background analysis to finish to avoid race conditions
            // on cross-project changes (the 'ripple effect').
            try {
                SourceUtils.waitScanFinished();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            
            ProjectDiagnostics alerts = IDE.getProjectAlerts(projectId);
            return Collections.singletonList(Part.fromText(GsonUtils.getGson().toJson(alerts)));
        } else {
            return Collections.singletonList(Part.fromText(projectId + " is not open, consider disabling this provider"));
        }

    }
}