/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package uno.anahata.nb.ai;

import java.awt.event.ActionEvent;
import java.io.File;
import static java.lang.System.out;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Logger;
import org.openide.modules.Dependency;
import org.openide.modules.InstalledFileLocator;
import org.openide.modules.ModuleInfo;
import org.openide.modules.SpecificationVersion;
import org.openide.util.Lookup;
import uno.anahata.gemini.functions.spi.ExecuteJavaCode;
import static uno.anahata.nb.ai.ClassPathUtils.getExtraClassPath;

/**
 *
 * @author pablo
 */
public class ModuleInfoHelper {

    private static final Logger logger = Logger.getLogger(ModuleInfoHelper.class.getName());

    public static void initExecJava() {
        String initClassPath = ExecuteJavaCode.initDefaultCompilerClasspath();
        List<String> geminiClasspath = getGeminiModuleJars();
        logger.info("ExecJava initializing getGeminiModuleJars(): This module: " + geminiClasspath.size());
        List<String> allModulesClassPath = getAllEnabledModulesJars();
        logger.info("ExecJava initializing getAllEnabledModulesJars()" +  allModulesClassPath.size());
        logger.info("ExecJava initializing default classpath: This module: " + geminiClasspath.size() + ". All active modules " + allModulesClassPath.size());
        String extraClassPathString = ClassPathUtils.classPathToString(geminiClasspath);
        String newClassPathString = initClassPath + File.pathSeparator + extraClassPathString;
        ExecuteJavaCode.setDefaultCompilerClasspath(newClassPathString);
        logger.info("ExecJava compilerClassPAth initialized:");
        //return ExecuteJavaCode.defaultCompilerClasspath;
    }

    public static List<String> getGeminiModuleJars() {
        ModuleInfo thisModuleOnly = null;
        Collection<? extends ModuleInfo> allModules = Lookup.getDefault().lookupAll(ModuleInfo.class);
        Collection<ModuleInfo> processedModules = new ArrayList(); 
        for (ModuleInfo m : allModules) {
            if (m.getCodeName().contains("anahata")) {
                logger.info("Module " + m.getCodeName() + " " + m.getCodeNameBase());
                thisModuleOnly = m;
                break;
            }
        }
        
        if (thisModuleOnly == null) {
            throw new RuntimeException("Could not find: anahata-netbeans-ai");
        }
        logger.info("This module" + thisModuleOnly.getCodeName() + " All Modules " + allModules.size());
        List<String> collection = new ArrayList<>();
        collectJarFiles(thisModuleOnly, allModules, collection, processedModules);
        return collection;
    }
    public static List<String> getAllEnabledModulesJars() {
        
        Collection<? extends ModuleInfo> allModules = Lookup.getDefault().lookupAll(ModuleInfo.class);
        Collection<ModuleInfo> processedModules = new ArrayList(); 
        
        List<String> collection = new ArrayList<>();
        for (ModuleInfo moduleInfo : allModules) {
            if (moduleInfo.isEnabled()) {
                collectJarFiles(moduleInfo, allModules, collection, processedModules);
            }
        }
        
        return collection;

    }


    public static void collectJarFiles(ModuleInfo module, Collection allModules, List<String> collectedJars, Collection<ModuleInfo> processedModules) {
        /*
        String codeNameBase = module.getCodeNameBase();
        String displayName = module.getDisplayName();
        SpecificationVersion specVersion = module.getSpecificationVersion();
        String versionStr = (specVersion != null) ? specVersion.toString() : "N/A";
         */
        boolean enabled = module.isEnabled();

        if (enabled && ! processedModules.contains(module)) {
            processedModules.add(module);
            // Print details
            /*
            logger.info("Display Name: " + displayName);
            logger.info("Code Name : " + module.getCodeName());
            logger.info("Code Name Base: " + codeNameBase);
            logger.info("Version: " + versionStr);
            logger.info("Enabled: " + enabled);
             */

            // Resolve JAR path: Convert code name to file name (replace '.' with '-')
            //String jarName = codeNameBase.replace('.', '-') + ".jar";
            File jarFile = getMainJarFile(module);
            String jarPath = (jarFile != null) ? jarFile.getAbsolutePath() : "Not found";
            if (jarFile != null && jarFile.getAbsolutePath() != null) {
                if (!collectedJars.contains(jarPath)) {
                    collectedJars.add(jarPath);
                    logger.info("Jar located for module: " + module + " " + jarPath);
                } else {
                    logger.fine("Jar File already located for module: " + module + " ");
                }

            } else {
                logger.warning("Could not locate jar file for module: " + module + " " + jarFile);
            }

            //logger.info("JAR File: " + jarFile);
            for (Dependency d : module.getDependencies()) {
                ModuleInfo matchingModule = findMatchingModule(d, (Collection) allModules);
                if (matchingModule != null && ! processedModules.contains(matchingModule)) {
                    collectJarFiles(matchingModule, allModules, collectedJars, processedModules);
                    /*
                        File depJar = getJarFile(matchingModule);
                        if (depJar != null && depJar.getAbsolutePath() != null) {
                            if (!collection.contains(depJar.getAbsolutePath())) {
                                logger.info("\tAdding Dependency: codeName=" + codeName + " v=" + version + " t=" + d.getType() + " comparison=" + d.getComparison() + " jar:" + depJar + " module:" + matchingModule);
                                collection.add(depJar.getAbsolutePath());
                            } else {
                                logger.info("\tAlready added! Dependency: codeName=" + codeName + " v=" + version + " t=" + d.getType() + " comparison=" + d.getComparison() + " module:" + depJar + " module:" + matchingModule);
                            }
                        } else {
                            logger.info("\tCould not add dependency jar: codeName=" + codeName + " v=" + version + " t=" + d.getType() + " comparison=" + d.getComparison() + " module:" + matchingModule);
                        }*/

                } else {
                    //logger.info("\tCould not find matching module for dependency : codeName=" + codeName + " v=" + version + " t=" + d.getType() + " comparison=" + d.getComparison());
                }

            }

        }
        
        

    }

    public static File getMainJarFile(ModuleInfo module) {
        String codeNameBase = module.getCodeNameBase();
        String displayName = module.getDisplayName();
        SpecificationVersion specVersion = module.getSpecificationVersion();
        String versionStr = (specVersion != null) ? specVersion.toString() : "N/A";
        // Resolve JAR path: Convert code name to file name (replace '.' with '-')
        String jarName = codeNameBase.replace('.', '-') + ".jar";
        File jarFile = InstalledFileLocator.getDefault().locate("modules/" + jarName, codeNameBase, false);
        return jarFile;
    }

    public static ModuleInfo findMatchingModule(Dependency dep, Collection<ModuleInfo> allModules) {
        boolean required = false;
        if (dep.getType() == Dependency.TYPE_JAVA) {
            return null;
        } else if (dep.getType() == Dependency.TYPE_REQUIRES) {
            //required = true;
        } else if (dep.getType() == Dependency.TYPE_RECOMMENDS) {
            //required = true;
        } else if (dep.getType() == Dependency.TYPE_PACKAGE) {
            //required = true;
        } else if (dep.getType() == Dependency.TYPE_NEEDS) {
            //required = true;
        } else if (dep.getType() != Dependency.TYPE_MODULE) {
            throw new IllegalArgumentException("Dependency " + dep + " must be of type TYPE_MODULE but it is: " + dep.getType() + " " + dep.getName());
        }

        String depName = dep.getName();
        int depSlashIdx = depName.indexOf('/');
        String depBase = (depSlashIdx == -1) ? depName : depName.substring(0, depSlashIdx);
        String relSpec = (depSlashIdx == -1) ? null : depName.substring(depSlashIdx + 1);

        int minRel = -1;
        int maxRel = -1;
        boolean exactRel = false;
        if (relSpec != null) {
            if (relSpec.contains("-")) {
                String[] range = relSpec.split("-");
                if (range.length != 2) {
                    throw new IllegalArgumentException("Invalid release range in dependency");
                }
                minRel = Integer.parseInt(range[0]);
                maxRel = Integer.parseInt(range[1]);
            } else {
                minRel = Integer.parseInt(relSpec);
                maxRel = minRel;
                exactRel = true;
            }
        }

        for (ModuleInfo m : allModules) {
            String moduleCodeName = m.getCodeName();
            int moduleSlashIdx = moduleCodeName.indexOf('/');
            String moduleBase = (moduleSlashIdx == -1) ? moduleCodeName : moduleCodeName.substring(0, moduleSlashIdx);
            int moduleRel = (moduleSlashIdx == -1) ? -1 : Integer.parseInt(moduleCodeName.substring(moduleSlashIdx + 1));

            // Match base code name
            if (!moduleBase.equals(depBase)) {
                continue;
            }

            // Match major release if specified in dependency
            if (relSpec != null) {
                if (moduleRel == -1) {
                    continue; // Module has no release, but dep requires one
                }
                if (moduleRel < minRel || moduleRel > maxRel) {
                    continue;
                }
            } else if (moduleRel != -1) {
                // Dep has no release spec, module has one: still OK in NetBeans (any release satisfies if not specified)
            }

            // Now check version based on comparison type
            int comparison = dep.getComparison();
            String depVersion = dep.getVersion();
            if (comparison == Dependency.COMPARE_ANY) {
                return m; // No version check needed
            } else if (comparison == Dependency.COMPARE_IMPL) {
                String moduleImpl = m.getImplementationVersion();
                if (depVersion != null && depVersion.equals(moduleImpl)) {
                    return m;
                }
            } else if (comparison == Dependency.COMPARE_SPEC) {
                SpecificationVersion moduleSpec = m.getSpecificationVersion();
                if (moduleSpec != null && depVersion != null) {
                    try {
                        SpecificationVersion depSpec = new SpecificationVersion(depVersion);
                        if (moduleSpec.compareTo(depSpec) >= 0) {
                            return m;
                        }
                    } catch (NumberFormatException e) {
                        throw new IllegalArgumentException("Dependency " + dep + " marked as COMPARE_SPEC but cannot compare", e);
                        //e.printStackTrace();
                    }
                }
            }

            // If we reach here, version didn't match
        }

        if (required) {
            //throw new IllegalArgumentException("Dependency " + dep + " marked as required but module not found");
        }

        return null; // No match found
    }
}
