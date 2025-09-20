/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package uno.anahata.nb.ai.deprecated;

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
import uno.anahata.gemini.functions.spi.RunningJVM;

/**
 *
 * @author pablo
 */
public class ClassPathUtils {

    private static final Logger logger = Logger.getLogger(ClassPathUtils.class.getName());
/*
    public static String initExecJava() {
        RunningJVM.defaultCompilerClasspath = RunningJVM.defaultCompilerClasspath + File.pathSeparator + getExtraClassPath();
        logger.info("ExecJava default compilerClassPath: " + RunningJVM.defaultCompilerClasspath.replace(File.pathSeparator, "\n\t"));
        return RunningJVM.defaultCompilerClasspath;
    }
    
  */  
    public static String getExtraClassPath() {
        return fileNamesToPathToString(listNetBeansJars());
    }
    
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
    
    public static String fileNamesToPathToString(List<String> classPath) {
        StringBuilder sb = new StringBuilder();
        for (String string : classPath) {
            if (sb.length() > 0) {
                sb.append(File.pathSeparator);
            }
            sb.append(string);
        }
        return sb.toString();
    }
    
    /*
     * Collects the absolute paths of all JAR files in the "modules" and "lib" 
     * directories of a given NetBeans category such as "platform" or "ide"
     * (e.g. <nbhome>/platform/modules and <nbhome>/platform/lib).
     *
     * @return List of absolute paths to platform JARs.
     */
    public static List<String> listNetBeansJars(String category) {
        Set<String> jarPaths = new HashSet<>();
        String netbeansHome = System.getProperty("netbeans.home");
        String[] categoryDirs = {
            netbeansHome + File.separator + category + File.separator + "modules",
            netbeansHome + File.separator + category + File.separator + "lib",
        };

        for (String dir : categoryDirs) {
            File moduleDir = new File(dir);
            if (moduleDir.exists() && moduleDir.isDirectory()) {
                File[] jars = moduleDir.listFiles((d, name) -> name.endsWith(".jar"));
                if (jars != null) {
                    for (File jar : jars) {
                        jarPaths.add(jar.getAbsolutePath());
                        logger.fine("Added platform JAR from " + dir + ": " + jar.getName());
                    }
                } else {
                    logger.warning("Cannot access platform directory (possible Snap restriction): " + dir);
                }
            } else {
                logger.warning("Platform directory does not exist: " + dir);
            }
        }

        List<String> result = new ArrayList<>(jarPaths);
        Collections.sort(result);
        logger.info("Collected " + result.size() + " platform JARs from " + category);
        return result;
    }

    /**
     * Combines platform and IDE JARs (platform/modules and platform/lib) with
     * netbeans.dynamic.classpath for a comlete classpath
     *
     * @return List of absolute paths to all relevant JARs.
     */
    public static List<String> listNetBeansJars() {
        Set<String> jarPaths = new HashSet<>();

        // Add platform and IDE JARs
        jarPaths.addAll(ClassPathUtils.listNetBeansJars(""));
        //jarPaths.addAll(ClassPathUtils.listNetBeansJars("../ide"));

        // Include dynamic classpath if enabled
        String dynamicClasspath = System.getProperty("netbeans.dynamic.classpath");
        if (dynamicClasspath != null) {
            for (String path : dynamicClasspath.split(File.pathSeparator)) {
                if (path.endsWith(".jar")) {
                    if (!jarPaths.contains(path)) {
                        jarPaths.add(path);
                        logger.info("Added dynamic classpath JAR: " + path);
                    } else {
                        logger.info("dynamic classpath JAR was already present: " + path);
                    }
                    
                }
            }
        } else {
            logger.info("No dynamic classpath JAR: ");
        }

        List<String> result = new ArrayList<>(jarPaths);
        Collections.sort(result);
        logger.info("Collected " + result.size() + " total JARs for classpath");
        return result;
    }


    
    public static String getClassPathOfEverythingOpenInIDE() {
        StringBuilder sb = new StringBuilder();
        Collection c = GlobalPathRegistry.getDefault().getSourceRoots();
        sb.append("Source Roots\n");
        sb.append("------------\n");
        for (Object object : c) {
            sb.append(object + " " + object.getClass() + "\n");
            sb.append("\n");
        }
        c = GlobalPathRegistry.getDefault().getPaths(ClassPath.COMPILE);
        sb.append("Compile\n");
        sb.append("------------\n");
        for (Object object : c) {
            sb.append(object + " " + object.getClass() + "\n");
        }
        c = GlobalPathRegistry.getDefault().getPaths(ClassPath.EXECUTE);
        sb.append("Execute\n");
        sb.append("------------\n");
        for (Object object : c) {
            sb.append(object + " " + object.getClass() + "\n");
        }
        c = GlobalPathRegistry.getDefault().getPaths(ClassPath.SOURCE);
        sb.append("Source\n");
        sb.append("------------\n");
        for (Object object : c) {
            sb.append(object + " " + object.getClass() + "\n");
        }

        return sb.toString();
    }
    
    public List<String> getActiveModules() {
        
    // Fetch all installed modules
        Collection<? extends ModuleInfo> modules = Lookup.getDefault().lookupAll(ModuleInfo.class);

        List<String> enabledModuleJars = new ArrayList<>();
        
        for (ModuleInfo module : modules) {
            String codeNameBase = module.getCodeNameBase();
            String displayName = module.getDisplayName();
            SpecificationVersion specVersion = module.getSpecificationVersion();
            String versionStr = (specVersion != null) ? specVersion.toString() : "N/A";
            boolean enabled = module.isEnabled();
            out.println("Display Name: " + displayName);
            out.println("Code Name Base: " + codeNameBase);
            out.println("Version: " + versionStr);
            out.println("Enabled: " + enabled);
            if (enabled) {
                // Resolve JAR path: Convert code name to file name (replace '.' with '-')
                String jarName = codeNameBase.replace('.', '-') + ".jar";
                File jarFile = InstalledFileLocator.getDefault().locate("modules/" + jarName, codeNameBase, false);
                String jarPath = (jarFile != null) ? jarFile.getAbsolutePath() : "Not found";                
                out.println("JAR Path: " + jarPath);
                enabledModuleJars.add(jarPath);
            }
            // Print details
            
            out.println("--------------------------------------------------");
            
        }
        
        logger.info("Enabled Modules  " + enabledModuleJars.size() + "/" +  modules.size() + "):");
        logger.info("--------------------------------------------------");

        return enabledModuleJars;
        
    }
}
