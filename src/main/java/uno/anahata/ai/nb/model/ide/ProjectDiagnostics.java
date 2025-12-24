/* Licensed under the Apache License, Version 2.0 */
package uno.anahata.ai.nb.model.ide;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import lombok.Getter;

@Getter
public class ProjectDiagnostics {
    private final String projectName;
    private final List<JavacAlert> javacAlerts = Collections.synchronizedList(new ArrayList<>());
    private final List<ProjectAlert> projectAlerts = Collections.synchronizedList(new ArrayList<>());

    public ProjectDiagnostics(String projectName) {
        this.projectName = projectName;
    }

    public void addJavacAlert(JavacAlert alert) {
        javacAlerts.add(alert);
    }

    public void addProjectAlert(ProjectAlert alert) {
        projectAlerts.add(alert);
    }
}