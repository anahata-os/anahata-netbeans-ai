/* Licensed under the Apache License, Version 2.0 */
package uno.anahata.ai.nb.util;

import java.io.File;
import static java.lang.System.out;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.classpath.GlobalPathRegistry;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.modules.Dependency;
import org.openide.modules.InstalledFileLocator;
import org.openide.modules.ModuleInfo;
import org.openide.modules.Modules;
import org.openide.modules.SpecificationVersion;
import org.openide.util.Lookup;
import uno.anahata.ai.tools.spi.RunningJVM;

/**
 * 
 * @author pablo
 */
public class ClassPathBuilder {

    private static final Logger logger = Logger.getLogger(ClassPathBuilder.class.getName());

    
    public static String filesToClassPathString(Set<File> classPath) {
        StringBuilder sb = new StringBuilder();
        for (File jarFile : classPath) {
            if (sb.length() > 0) {
                sb.append(File.pathSeparator);
            }
            sb.append(jarFile.getAbsolutePath());
        }
        return sb.toString();
    }
   
    
    
}