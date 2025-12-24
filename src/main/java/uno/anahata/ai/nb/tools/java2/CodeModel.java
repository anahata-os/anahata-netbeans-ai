/* Licensed under the Apache License, Version 2.0 */
package uno.anahata.ai.nb.tools.java2;

import java.io.File;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import javax.lang.model.element.TypeElement;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.java.source.ClassIndex;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.SourceUtils;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.modules.java.source.ui.JavaTypeDescription;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import uno.anahata.ai.nb.model.Page;
import uno.anahata.ai.nb.model.java2.JavaMember;
import uno.anahata.ai.nb.model.java2.JavaMemberSearch;
import uno.anahata.ai.nb.model.java2.JavaType;
import uno.anahata.ai.nb.model.java2.JavaTypeSearch;
import uno.anahata.ai.tools.AIToolMethod;
import uno.anahata.ai.tools.AIToolParam;
import uno.anahata.ai.tools.spi.pojos.FileInfo;

public class CodeModel {

    @AIToolMethod("Finds multiple Java types matching a query and returns a paginated result of minimalist, machine-readable keys.")
    public static Page<JavaType> findTypes(
            @AIToolParam("The search query for the types (e.g., simple name, FQN, wildcards).") String query,
            @AIToolParam("Whether the search should be case-sensitive.") boolean caseSensitive,
            @AIToolParam("Whether to prioritize results from open projects.") boolean preferOpenProjects,
            @AIToolParam("The starting index (0-based) for pagination.") Integer startIndex,
            @AIToolParam("The maximum number of results to return per page.") Integer pageSize) {

        JavaTypeSearch finder = new JavaTypeSearch(query, caseSensitive, preferOpenProjects);
        List<JavaType> allResults = finder.getResults();

        int start = startIndex != null ? startIndex : 0;
        int size = pageSize != null ? pageSize : 100;

        return new Page<>(allResults, start, size);
    }

    @AIToolMethod("Gets the source file for a given JavaType. This is the second step in the 'discovery' (Ctrl+O) workflow.")
    public static String getTypeSources(
            @AIToolParam("The minimalist keychain DTO from a findTypes call.") JavaType javaType) throws Exception {
        return javaType.getSource().getContent();
    }
    
    @AIToolMethod("Gets a list of all members (fields, constructors, methods) for a given type.")
    public static List<JavaMember> getMembers(
            @AIToolParam("The keychain DTO for the type to inspect.") JavaType javaType) throws Exception {
        return javaType.getMembers();
    }

    @AIToolMethod("Gets the source for a type using a specific project's classpath. This is the 'Ctrl+Click', context-aware, one-turn tool.")
    public static FileInfo getSources(
            @AIToolParam("The simple or FQN of the type.") String typeName,
            @AIToolParam("The absolute path of the project directory to use for context.") String projectDirectoryPath) throws Exception {

        Project project = findProject(projectDirectoryPath);

        Sources sources = ProjectUtils.getSources(project);
        SourceGroup[] sourceGroups = sources.getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
        if (sourceGroups.length == 0) {
            throw new Exception("Project has no Java source groups: " + projectDirectoryPath);
        }
        FileObject rootFolder = sourceGroups[0].getRootFolder();
        ClasspathInfo cpInfo = ClasspathInfo.create(rootFolder);

        Set<ElementHandle<TypeElement>> handles = cpInfo.getClassIndex().getDeclaredTypes(
            typeName,
            ClassIndex.NameKind.SIMPLE_NAME,
            EnumSet.of(ClassIndex.SearchScope.SOURCE, ClassIndex.SearchScope.DEPENDENCIES)
        );

        if (handles.isEmpty()) {
            throw new Exception("Could not find type '" + typeName + "' in project '" + projectDirectoryPath + "'");
        }

        ElementHandle<TypeElement> handle = handles.iterator().next();
        FileObject fileObject = SourceUtils.getFile(handle, cpInfo);

        if (fileObject == null) {
            throw new Exception("Could not find source file for " + typeName);
        }

        return getFileInfoFromFileObject(fileObject);
    }

    private static FileInfo getFileInfoFromFileObject(FileObject fo) throws Exception {
        String content = fo.asText();
        return new FileInfo(
            fo.getPath(),
            content,
            content.lines().count(),
            fo.lastModified().getTime(),
            fo.getSize()
        );
    }
    
    private static Project findProject(String projectDirectoryPath) throws Exception {
        FileObject dir = FileUtil.toFileObject(new File(projectDirectoryPath));
        if (dir == null) {
            throw new Exception("Project directory not found: " + projectDirectoryPath);
        }
        for (Project p : OpenProjects.getDefault().getOpenProjects()) {
            if (p.getProjectDirectory().equals(dir)) {
                return p;
            }
        }
        throw new Exception("Project not found or is not open: " + projectDirectoryPath);
    }
}