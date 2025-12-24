/* Licensed under the Apache License, Version 2.0 */
package uno.anahata.ai.nb.model.java2;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.modules.java.source.ui.JavaTypeDescription;
import org.netbeans.modules.jumpto.type.TypeProviderAccessor;
import org.netbeans.spi.jumpto.type.SearchType;
import org.netbeans.spi.jumpto.type.TypeDescriptor;
import org.netbeans.spi.jumpto.type.TypeProvider;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;

/**
 * A "Finder" command object that searches for Java types upon instantiation.
 * It encapsulates the logic for interacting with the NetBeans TypeProvider SPI
 * and holds the results in a private final field.
 */
public class JavaTypeSearch {

    private final List<JavaType> results;

    public JavaTypeSearch(String query, boolean caseSensitive, boolean preferOpenProjects) {
        // Determine SearchType
        final SearchType searchType;
        if (query.contains("*") || query.contains("?")) {
            searchType = caseSensitive ? SearchType.REGEXP : SearchType.CASE_INSENSITIVE_REGEXP;
        } else {
            searchType = caseSensitive ? SearchType.CAMEL_CASE : SearchType.CASE_INSENSITIVE_CAMEL_CASE;
        }

        // Perform Search
        Collection<? extends TypeProvider> providers = Lookup.getDefault().lookupAll(TypeProvider.class);
        final List<TypeDescriptor> descriptors = new ArrayList<>();
        TypeProvider.Context context = TypeProviderAccessor.DEFAULT.createContext(null, query, searchType);
        TypeProvider.Result result = TypeProviderAccessor.DEFAULT.createResult(descriptors, new String[1], context);
        for (TypeProvider provider : providers) {
            provider.computeTypeNames(context, result);
        }
        
        // Filter for Java-specific descriptors
        List<JavaTypeDescription> javaDescriptors = descriptors.stream()
                .filter(td -> td instanceof JavaTypeDescription)
                .map(td -> (JavaTypeDescription) td)
                .collect(Collectors.toList());

        // Sort Results
        if (!preferOpenProjects) {
            javaDescriptors.sort(Comparator.comparing(TypeDescriptor::getSimpleName, String.CASE_INSENSITIVE_ORDER));
        } else {
            final Set<Project> openProjects = new HashSet<>(List.of(OpenProjects.getDefault().getOpenProjects()));
            javaDescriptors.sort((td1, td2) -> {
                FileObject fo1 = td1.getFileObject();
                Project p1 = (fo1 != null) ? FileOwnerQuery.getOwner(fo1) : null;
                boolean p1Open = (p1 != null) && openProjects.contains(p1);

                FileObject fo2 = td2.getFileObject();
                Project p2 = (fo2 != null) ? FileOwnerQuery.getOwner(fo2) : null;
                boolean p2Open = (p2 != null) && openProjects.contains(p2);

                if (p1Open && !p2Open) return -1;
                if (!p1Open && p2Open) return 1;

                return td1.getSimpleName().compareToIgnoreCase(td2.getSimpleName());
            });
        }
        
        // Convert to JavaType
        this.results = Collections.unmodifiableList(
            javaDescriptors.stream()
                .map(JavaType::new)
                .collect(Collectors.toList())
        );
    }

    /**
     * Gets the results of the search.
     * @return An unmodifiable list of found JavaTypes.
     */
    public List<JavaType> getResults() {
        return results;
    }
}
