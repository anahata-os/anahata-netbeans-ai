package uno.anahata.nb.ai.tools;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.spi.java.classpath.ClassPathProvider;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import uno.anahata.gemini.functions.AIToolMethod;
import uno.anahata.gemini.functions.AIToolParam;
import uno.anahata.gemini.functions.spi.RunningJVM;

/**
 * A NetBeans-aware wrapper around the core RunningJVM tool.
 * This tool understands NetBeans projects and can execute code within their specific context,
 * enabling a "hot-reload" workflow by prioritizing a project's compiled output.
 *
 * @author Anahata
 */
public class NetBeansProjectJVM {

    @AIToolMethod(
            value = "Compiles and executes Java source code within the context of a specific NetBeans project. " +
                    "This tool automatically constructs a classpath that includes and prioritizes the project's build output " +
                    "(e.g., 'target/classes'), allowing for the immediate testing of newly written or modified code and supporting the Compile On Save feature " +
                    "without needing to reload the entire plugin. It's the key to a 'hot-reload' workflow.",
            requiresApproval = true
    )
    public static Object compileAndExecuteInProject(
            @AIToolParam("The ID (directory name) of the NetBeans project to run in.") String projectId,
            @AIToolParam("Source code of a public class named 'Anahata' that implements java.util.concurrent.Callable.") String sourceCode,
            @AIToolParam("Whether to include the project's COMPILE and EXECUTE **dependencies** (the target/classess dir is always included regardless of this flag).") boolean includeCompileAndExecuteDependencies,
            @AIToolParam("Whether to include the project's test source folders and test dependencies in the classpath (only for running code that uses test sources).") boolean includeTestDependencies,
            @AIToolParam("Optional additional compiler options.") String[] compilerOptions) throws Exception {

        Project project = Projects.findProject(projectId);
        if (project == null) {
            throw new IllegalArgumentException("Project not found: " + projectId);
        }

        ClassPathProvider cpp = project.getLookup().lookup(ClassPathProvider.class);
        if (cpp == null) {
            throw new IllegalStateException("Could not find ClassPathProvider for project: " + projectId);
        }

        List<String> internalPaths = new ArrayList<>();
        List<String> dependencyPaths = new ArrayList<>();
        String projectDir = project.getProjectDirectory().getPath();

        Sources sources = ProjectUtils.getSources(project);
        // Get all Java and Resources source groups
        SourceGroup[] javaGroups = sources.getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
        SourceGroup[] resourceGroups = sources.getSourceGroups(JavaProjectConstants.SOURCES_TYPE_RESOURCES);
        
        List<SourceGroup> allSourceGroups = new ArrayList<>(Arrays.asList(javaGroups));
        allSourceGroups.addAll(Arrays.asList(resourceGroups));
        
        for (SourceGroup sg : allSourceGroups) {
            // Filter out test sources if not requested, based on the display name heuristic
            boolean isTest = sg.getDisplayName().toLowerCase().contains("test");
            if (isTest && !includeTestDependencies) {
                continue;
            }

            // We need to check both COMPILE and EXECUTE classpaths to get a complete picture.
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
                    // Check if the classpath entry is the project's own build output (internal)
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

        // Combine lists, ensuring internal paths come first for class overriding
        List<String> finalPathElements = new ArrayList<>(internalPaths);
        finalPathElements.addAll(dependencyPaths);

        if (finalPathElements.isEmpty()) {
            throw new IllegalStateException("Could not resolve any classpath entries for project: " + projectId);
        }

        String extraClassPath = String.join(File.pathSeparator, finalPathElements);

        return RunningJVM.compileAndExecuteJava(sourceCode, extraClassPath, compilerOptions);
    }
}
