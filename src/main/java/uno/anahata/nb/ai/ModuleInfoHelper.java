/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package uno.anahata.nb.ai;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.modules.Dependency;
import org.openide.modules.ModuleInfo;
import org.openide.modules.Modules;
import org.openide.modules.SpecificationVersion;
import org.openide.util.Lookup;
import uno.anahata.gemini.functions.spi.ExecuteJavaCode;

/**
 *
 * @author pablo
 */
public class ModuleInfoHelper {

    private static final Logger logger = Logger.getLogger(ModuleInfoHelper.class.getName());

    public static void initExecuteJavaCode() {

        try {
            String initClassPathString = ExecuteJavaCode.initDefaultCompilerClasspath();
            logger.info("ExecJava initializing initClassPath: " + initClassPathString);
            List<String> geminiClasspath = getGeminiModuleJars();
            String extraClassPathString = ClassPathUtils.classPathToString(geminiClasspath);
            String newClassPathString = initClassPathString + File.pathSeparator + extraClassPathString;
            ExecuteJavaCode.setDefaultCompilerClasspath(newClassPathString);
            logger.info("newClassPathString: " + initClassPathString);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Exception initializing ExecuteJavaCode", e);
        }

    }

    public static List<String> getGeminiModuleJars() throws Exception {
        ModuleInfo thisModuleOnly = Modules.getDefault().ownerOf(ModuleInfoHelper.class);

        if (thisModuleOnly == null) {
            throw new RuntimeException("Could not find the module that owns the ModuleInfoHelper class.");
        }

        logger.info("Found Plugin Module " + thisModuleOnly.getCodeName() + " implementation version " + thisModuleOnly.getImplementationVersion() + " build version " + thisModuleOnly.getBuildVersion());

        Collection<? extends ModuleInfo> allModules = Lookup.getDefault().lookupAll(ModuleInfo.class);
        logger.info("All modules: " + allModules.size());
        
        logger.info("This module" + thisModuleOnly.getCodeName() + " All Modules " + allModules.size());
        List<String> collection = new ArrayList<>();
        Collection<ModuleInfo> processedModules = new ArrayList<>();
        collectJarFiles(thisModuleOnly, allModules, collection, processedModules);
        return collection;
    }

    public static List<String> getAllEnabledModulesJars() throws Exception {

        Collection<? extends ModuleInfo> allModules = Lookup.getDefault().lookupAll(ModuleInfo.class);
        Collection<ModuleInfo> processedModules = new ArrayList<>();

        List<String> collection = new ArrayList<>();
        for (ModuleInfo moduleInfo : allModules) {
            if (moduleInfo.isEnabled()) {
                collectJarFiles(moduleInfo, allModules, collection, processedModules);
            }
        }

        return collection;

    }

    public static void collectJarFiles(ModuleInfo module, Collection allModules, List<String> collectedJars, Collection<ModuleInfo> processedModules) throws Exception {
        boolean enabled = module.isEnabled();

        if (enabled && !processedModules.contains(module)) {
            processedModules.add(module);
            
            File jarFile = getMainJarFile(module);
            if (jarFile != null) {
                String jarPath = jarFile.getAbsolutePath();
                if (jarPath != null) {
                    if (!collectedJars.contains(jarPath)) {
                        collectedJars.add(jarPath);
                    } else {
                        logger.warning("Jar File already located for module: " + module);
                    }
                } else {
                    logger.warning("No absolutePath for module= " + module + " jarFile=" + jarFile);
                }

            } else {
                logger.warning("Could not locate main jar file for module: " + module);
            }

            for (Dependency d : module.getDependencies()) {
                ModuleInfo matchingModule = findMatchingModule(d, (Collection) allModules);
                if (matchingModule != null && !processedModules.contains(matchingModule)) {
                    collectJarFiles(matchingModule, allModules, collectedJars, processedModules);
                }
            }
        }
    }

    public static File getMainJarFile(ModuleInfo module) {
        try {
            URL manifestUrl = module.getClassLoader().getResource("META-INF/MANIFEST.MF");
            if (manifestUrl == null) {
                logger.warning("Could not find manifest for module: " + module);
                return null;
            }

            java.net.JarURLConnection connection = (java.net.JarURLConnection) manifestUrl.openConnection();
            URL fileUrl = connection.getJarFileURL();
            File jarFile = new File(fileUrl.toURI());
            logger.info("Found JAR for " + module.getCodeNameBase() + " at " + jarFile.getAbsolutePath());
            return jarFile;
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error getting JAR for module " + module, e);
            return null;
        }
    }

    public static ModuleInfo findMatchingModule(Dependency dep, Collection<ModuleInfo> allModules) {
        if (dep.getType() == Dependency.TYPE_JAVA) {
            return null; // Skip JDK dependencies
        }
    
        if (dep.getType() != Dependency.TYPE_MODULE &&
            dep.getType() != Dependency.TYPE_PACKAGE &&
            dep.getType() != Dependency.TYPE_REQUIRES &&
            dep.getType() != Dependency.TYPE_RECOMMENDS &&
            dep.getType() != Dependency.TYPE_NEEDS) {
            logger.warning("Skipping unhandled dependency type: " + dep);
            return null;
        }
    
        List<ModuleInfo> candidates = new ArrayList<>();
        String depName = dep.getName();
        int depSlashIdx = depName.indexOf('/');
        String depBase = (depSlashIdx == -1) ? depName : depName.substring(0, depSlashIdx);
        String relSpec = (depSlashIdx == -1) ? null : depName.substring(depSlashIdx + 1);
    
        for (ModuleInfo m : allModules) {
            String moduleCodeName = m.getCodeName();
            int moduleSlashIdx = moduleCodeName.indexOf('/');
            String moduleBase = (moduleSlashIdx == -1) ? moduleCodeName : moduleCodeName.substring(0, moduleSlashIdx);
    
            if (!moduleBase.equals(depBase)) {
                continue;
            }
    
            if (relSpec != null) {
                int moduleRel = (moduleSlashIdx == -1) ? -1 : Integer.parseInt(moduleCodeName.substring(moduleSlashIdx + 1));
                if (moduleRel == -1 || !relSpec.equals(String.valueOf(moduleRel))) { // Simplified, assuming exact match for now
                    continue;
                }
            }
            
            // At this point, the module is a potential candidate by name.
            // Now, check if it satisfies the version requirements.
            boolean versionOk = false;
            int comparison = dep.getComparison();
            String depVersion = dep.getVersion();
    
            if (comparison == Dependency.COMPARE_ANY) {
                versionOk = true;
            } else if (comparison == Dependency.COMPARE_IMPL) {
                String moduleImpl = m.getImplementationVersion();
                if (depVersion != null && depVersion.equals(moduleImpl)) {
                    versionOk = true;
                }
            } else if (comparison == Dependency.COMPARE_SPEC) {
                SpecificationVersion moduleSpec = m.getSpecificationVersion();
                if (moduleSpec != null && depVersion != null) {
                    try {
                        SpecificationVersion depSpec = new SpecificationVersion(depVersion);
                        if (moduleSpec.compareTo(depSpec) >= 0) {
                            versionOk = true;
                        }
                    } catch (NumberFormatException e) {
                        logger.log(Level.SEVERE, "Could not parse specification version for dependency: " + dep + " on module: " + m, e);
                    }
                }
            }
            
            if(versionOk) {
                candidates.add(m);
            }
        }
    
        if (candidates.isEmpty()) {
            return null; // No match found
        }
    
        if (candidates.size() == 1) {
            return candidates.get(0); // Only one choice
        }
    
        // More than one candidate, find the one with the highest spec version.
        ModuleInfo best = null;
        for (ModuleInfo candidate : candidates) {
            if (best == null) {
                best = candidate;
            } else {
                SpecificationVersion bestSpec = best.getSpecificationVersion();
                SpecificationVersion candidateSpec = candidate.getSpecificationVersion();
                if (bestSpec != null && candidateSpec != null) {
                    if (candidateSpec.compareTo(bestSpec) > 0) {
                        best = candidate;
                    }
                }
            }
        }
        return best;
    }
}
