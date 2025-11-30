package uno.anahata.ai.nb.tools;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.spi.java.classpath.ClassPathProvider;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import uno.anahata.ai.tools.AIToolMethod;
import uno.anahata.ai.tools.AIToolParam;
import uno.anahata.ai.tools.spi.RunningJVM;

/**
 * A NetBeans-aware wrapper around the core {@link RunningJVM} tool.
 * This tool understands NetBeans projects and can execute code within their specific context,
 * enabling a "hot-reload" workflow by prioritizing a project's compiled output.
 *
 * <h2>Important Operational Notes for NetBeans Module (NBM) Development:</h2>
 * <p>
 * The behavior of this tool is particularly nuanced when the target project is itself a NetBeans Module.
 * The key challenge is avoiding {@link LinkageError} exceptions, which occur when the tool's dynamic
 * classloader loads a class (e.g., from a NetBeans API JAR) that has already been loaded by the main
 * IDE's classloader.
 * </p>
 * <ul>
 *   <li><b>Scenario 1: Testing the AI Assistant Plugin Itself</b><br>
 *       When modifying the AI Assistant's own tools, you should always set {@code includeCompileAndExecuteDependencies}
 *       to {@code false}. The plugin's own classloader already provides access to all necessary NetBeans APIs.</li>
 *
 *   <li><b>Scenario 2: Testing a <em>Different</em> NBM Project</b><br>
 *       If you are using the AI Assistant to help develop a <em>separate</em> NBM (e.g., a Jenkins plugin),
 *       you will likely need to set {@code includeCompileAndExecuteDependencies} to {@code true} so the
 *       classloader can find your module's specific classes. However, you must be careful to exclude any
 *       dependencies on NetBeans Platform APIs that are already provided by the IDE. This can often be
 *       achieved via {@code <scope>provided</scope>} or exclusions in the target project's {@code pom.xml}.</li>
 * </ul>
 *
 * @author Anahata
 */
@Slf4j
public class NetBeansProjectJVM {

    /**
     * Compiles and executes Java source code within the context of a specific NetBeans project.
     * This tool automatically constructs a classpath that includes and prioritizes the project's build output
     * (e.g., 'target/classes'), allowing for the immediate testing of newly written or modified code and supporting the Compile On Save feature
     * without needing to rebuild the project. It's the key to a 'hot-reload' workflow.
     *
     * This tool is a proxy that uses {@link RunningJVM} to do the actual compilation and execution. Its primary
     * responsibility is to correctly construct the {@code extraClassPath} parameter for the underlying JVM tool.
     *
     * @param projectId The ID (directory name) of the NetBeans project to run in.
     * @param sourceCode Source code of a public class named <b>Anahata</b> that has <b>no package declaration</b> and <b>implements java.util.concurrent.Callable</b>.
     * @param includeCompileAndExecuteDependencies Whether to include the project's COMPILE and EXECUTE dependencies. The project's own build output is always included.
     * @param includeTestDependencies Whether to include the project's test source folders and test dependencies in the classpath.
     * @param compilerOptions Optional additional compiler options.
     * @return The result of the execution.
     * @throws Exception on error.
     */
    @AIToolMethod(
            value = "Compiles and executes Java source code within the context of a specific NetBeans project. "
            + "This tool enables a powerful 'hot-reload' workflow by creating a dynamic classpath that prioritizes the project's own build directories (e.g., 'target/classes') over the application's default classpath. "
            + "This ensures that any newly compiled classes are used immediately."
            + "\n\n<b>IMPORTANT NOTE for NetBeans Module (NBM) Development:</b>"
            + "\nWhen the target project is an NBM that is deployed on the current netbeans instance (e.g. the Anahata NetBeans Plugin), always set `includeCompileAndExecuteDependencies` to `false`.",
            requiresApproval = true
    )
    public static Object compileAndExecuteInProject(
            @AIToolParam("The ID (directory name) of the NetBeans project to run in.") String projectId,
            @AIToolParam("Source code of a public class named **Anahata** that has **no package declaration** and **implements java.util.concurrent.Callable**.") String sourceCode,
            @AIToolParam("Whether to include the project's COMPILE and EXECUTE **dependencies** (the target/classess dir is always included regardless of this flag).") boolean includeCompileAndExecuteDependencies,
            @AIToolParam("Whether to include the project's test source folders and test dependencies in the classpath (only for running code that uses test sources).") boolean includeTestDependencies,
            @AIToolParam("Optional additional compiler options.") String[] compilerOptions) throws Exception {

        Project project = Projects.findProject(projectId);
        
        ClassPathProvider cpp = project.getLookup().lookup(ClassPathProvider.class);
        if (cpp == null) {
            throw new IllegalStateException("Could not find ClassPathProvider for project: " + projectId);
        }

        List<String> internalPaths = new ArrayList<>();
        List<String> dependencyPaths = new ArrayList<>();
        String projectDir = project.getProjectDirectory().getPath();

        Sources sources = ProjectUtils.getSources(project);
        SourceGroup[] javaGroups = sources.getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
        SourceGroup[] resourceGroups = sources.getSourceGroups(JavaProjectConstants.SOURCES_TYPE_RESOURCES);
        
        List<SourceGroup> allSourceGroups = new ArrayList<>(Arrays.asList(javaGroups));
        allSourceGroups.addAll(Arrays.asList(resourceGroups));
        
        for (SourceGroup sg : allSourceGroups) {
            boolean isTest = sg.getDisplayName().toLowerCase().contains("test");
            if (isTest && !includeTestDependencies) {
                continue;
            }

            ClassPath compileCp = cpp.findClassPath(sg.getRootFolder(), ClassPath.COMPILE);
            ClassPath executeCp = cpp.findClassPath(sg.getRootFolder(), ClassPath.EXECUTE);
            
            Set<FileObject> allRoots = new LinkedHashSet<>();
            if (compileCp != null) allRoots.addAll(Arrays.asList(compileCp.getRoots()));
            if (executeCp != null) allRoots.addAll(Arrays.asList(executeCp.getRoots()));

            for (FileObject entry : allRoots) {
                URL url = entry.toURL();
                File f = FileUtil.archiveOrDirForURL(url);

                if (f != null) {
                    String absolutePath = f.getAbsolutePath();
                    boolean isInternal = absolutePath.startsWith(projectDir);

                    if (isInternal) {
                        if (!internalPaths.contains(absolutePath)) {
                           internalPaths.add(absolutePath);
                        }
                    } else if (includeCompileAndExecuteDependencies) {
                        if (!dependencyPaths.contains(absolutePath)) {
                            dependencyPaths.add(absolutePath);
                        }
                    }
                }
            }
        }
        
        log.info("Constructing classpath for project '{}'", projectId);
        log.info("Found {} internal project directories (e.g., target/classes):", internalPaths.size());
        for (String path : internalPaths) {
            log.info("  - {}", path);
        }

        if (includeCompileAndExecuteDependencies) {
            log.info("Including {} resolved dependency JARs.", dependencyPaths.size());
        } else {
            log.info("Resolved dependency JARs are excluded by request.");
        }

        List<String> finalPathElements = new ArrayList<>(internalPaths);
        finalPathElements.addAll(dependencyPaths);

        if (finalPathElements.isEmpty()) {
            throw new IllegalStateException("Could not resolve any classpath entries for project: " + projectId);
        }

        String extraClassPath = String.join(File.pathSeparator, finalPathElements);

        return RunningJVM.compileAndExecuteJava(sourceCode, extraClassPath, compilerOptions);
    }
}