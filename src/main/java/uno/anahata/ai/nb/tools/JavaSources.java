/* Licensed under the Apache License, Version 2.0 */
package uno.anahata.ai.nb.tools;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.stream.Collectors;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.classpath.GlobalPathRegistry;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.platform.JavaPlatformManager;
import org.netbeans.api.java.queries.SourceForBinaryQuery;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import uno.anahata.ai.tools.AIToolMethod;
import uno.anahata.ai.tools.AIToolParam;
import uno.anahata.ai.tools.ContextBehavior;
import uno.anahata.ai.nb.model.java.SourceFileInfo;
import uno.anahata.ai.nb.model.java.SourceOrigin;
import uno.anahata.ai.tools.spi.pojos.TextChunk;
import uno.anahata.ai.internal.TextUtils;

/**
 * Provides tools for retrieving Java source code.
 * @author Anahata
 */
public class JavaSources {

    // Helper class to pass results internally
    private static class ClassSearchResult {
        public final FileObject classFile;
        public final ClassPath ownerCp;
        public final JavaPlatform platform; // Can be null if not a JDK class

        public ClassSearchResult(FileObject classFile, ClassPath ownerCp, JavaPlatform platform) {
            this.classFile = classFile;
            this.ownerCp = ownerCp;
            this.platform = platform;
        }
    }

    @AIToolMethod(
        value = "Gets rich, contextual information about a Java source file, including its origin (project, JAR, or JDK) and its content, with support for safe pagination. " +
                "This is the primary tool for reading source code.",
        requiresApproval = false,
        behavior = ContextBehavior.STATEFUL_REPLACE
    )
    public static SourceFileInfo getSource(
            @AIToolParam("The fully qualified name of the class.") String fqn,
            @AIToolParam(value = "The starting line number (1-based). Use null for the beginning of the file.", required = false) Integer startLine,
            @AIToolParam(value = "The number of lines to return. Use null for all lines.", required = false) Integer lineCount,
            @AIToolParam(value = "The maximum length of each line. Use null for no limit.", required = false) Integer maxLineLength) throws Exception {

        // Step 1: Find .class file
        String classAsPath = fqn.replace('.', '/') + ".class";
        ClassSearchResult searchResult = findClassFile(classAsPath);
        if (searchResult == null) {
            throw new IllegalStateException("Error: .class file not found for " + fqn);
        }

        // Step 2: Query for source roots
        FileObject classFileRoot = searchResult.ownerCp.findOwnerRoot(searchResult.classFile);
        SourceForBinaryQuery.Result sfbqResult = SourceForBinaryQuery.findSourceRoots(classFileRoot.toURL());
        FileObject[] sourceRoots = sfbqResult.getRoots();
        if (sourceRoots.length == 0) {
            throw new IllegalStateException("Error: Source root not found for " + fqn);
        }
        FileObject sourceRoot = sourceRoots[0]; // Use the first one

        // Step 3: Find the .java file
        String sourcePath = fqn.replace('.', '/') + ".java";
        FileObject sourceFile = sourceRoot.getFileObject(sourcePath);
        if (sourceFile == null) {
            throw new IllegalStateException("Error: .java file not found in source root for " + fqn);
        }

        // Step 4: Read content
        String content;
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(sourceFile.getInputStream(), StandardCharsets.UTF_8))) {
            content = reader.lines().collect(Collectors.joining("\n"));
        }

        // Step 5: Classify and determine origin
        Project owner = FileOwnerQuery.getOwner(sourceFile);
        SourceOrigin originType;
        String originLocation;

        if (owner != null) {
            originType = SourceOrigin.PROJECT;
            originLocation = owner.getProjectDirectory().getName();
        } else {
            if (searchResult.platform != null) {
                originType = SourceOrigin.JDK;
                Collection<FileObject> installFolders = searchResult.platform.getInstallFolders();
                String installPath = "Unknown Location";
                if (installFolders != null && !installFolders.isEmpty()) {
                    installPath = installFolders.iterator().next().getPath();
                }
                originLocation = searchResult.platform.getDisplayName() + " (" + installPath + ")";
            } else {
                originType = SourceOrigin.JAR;
                FileObject archive = FileUtil.getArchiveFile(sourceRoot);
                originLocation = (archive != null) ? archive.getPath() : sourceRoot.getPath();
            }
        }

        // Step 6: Handle pagination
        TextChunk chunk = null;
        String fullContent = null;
        boolean isPaginated = (startLine != null && startLine > 1) 
                || (lineCount != null && lineCount > 0) 
                || (maxLineLength != null && maxLineLength > 0);

        if (isPaginated) {
            chunk = TextUtils.processText(content, startLine != null ? startLine - 1 : 0, lineCount, null, maxLineLength);
        } else {
            fullContent = content;
        }

        return new SourceFileInfo(
                sourceFile.getPath(),
                fullContent,
                content.lines().count(),
                sourceFile.lastModified().getTime(),
                sourceFile.getSize(),
                originType,
                originLocation,
                chunk
        );
    }

    private static ClassSearchResult findClassFile(String classAsPath) {
        // Check project sources first
        for (ClassPath cp : GlobalPathRegistry.getDefault().getPaths(ClassPath.SOURCE)) {
            FileObject fo = cp.findResource(classAsPath);
            if (fo != null) return new ClassSearchResult(fo, cp, null);
        }
        // Then check dependencies
        for (ClassPath cp : GlobalPathRegistry.getDefault().getPaths(ClassPath.EXECUTE)) {
            FileObject fo = cp.findResource(classAsPath);
            if (fo != null) return new ClassSearchResult(fo, cp, null);
        }
        // Finally, check all installed JDKs
        for (JavaPlatform platform : JavaPlatformManager.getDefault().getInstalledPlatforms()) {
            ClassPath bootstrapLibraries = platform.getBootstrapLibraries();
            FileObject fo = bootstrapLibraries.findResource(classAsPath);
            if (fo != null) {
                return new ClassSearchResult(fo, bootstrapLibraries, platform);
            }
        }
        return null;
    }
}
