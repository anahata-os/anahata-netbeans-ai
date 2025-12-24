/* Licensed under the Apache License, Version 2.0 */
package uno.anahata.ai.nb.util;

import uno.anahata.ai.nb.util.ClassPathBuilder;
import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.modules.Dependency;
import org.openide.modules.ModuleInfo;
import org.openide.modules.Modules;
import uno.anahata.ai.tools.spi.RunningJVM;
import java.lang.reflect.Method;
import java.util.Collections;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import uno.anahata.ai.nb.AnahataInstaller;

/**
 * Utility class for introspecting modules and their classpaths. Primary used for RunningJVM.
 * 
 * @author priyanahata
 */
public final class NetBeansModuleUtils {

    private static final Logger logger = Logger.getLogger(NetBeansModuleUtils.class.getName());

    private NetBeansModuleUtils() {
        // Utility class
    }
    
    // --- Classpath Initialization Logic 

    public static void initRunningJVM() {
        try {
            String javaClassPath = RunningJVM.initDefaultCompilerClasspath();
            String netbeansDynamicClassPath = System.getProperty("netbeans.dynamic.classpath");
            logger.info("javaClassPath: " + javaClassPath);
            logger.info("nbDynamic: " + netbeansDynamicClassPath);

            Set<File> moduleClassPath = getModuleClassPath();
            logger.info("moduleClasspath: " + moduleClassPath);
            String moduleClassPathStr = ClassPathBuilder.filesToClassPathString(moduleClassPath);

            String newClassPathString =
                      javaClassPath + File.pathSeparator
                    + netbeansDynamicClassPath + File.pathSeparator
                    + moduleClassPathStr;
            RunningJVM.setDefaultCompilerClasspath(newClassPathString);
            logger.info("Final Classpath:\n" + RunningJVM.getPrettyPrintedDefaultCompilerClasspath());
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Exception initializing RunningJVM default compilers classpath", e);
        }
    }

    private static Set<File> getModuleClassPath() {
        Set<ModuleInfo> processed = new HashSet();
        ModuleInfo thisModule = Modules.getDefault().ownerOf(AnahataInstaller.class);
        return getClassPath(thisModule, processed);
    }

    private static Set<File> getClassPath(ModuleInfo mi, Set<ModuleInfo> processed) {
        Set<File> ret = new HashSet();
        processed.add(mi);
        ret.addAll(getAllModuleJarsUsingReflection(mi));
        for (Dependency d : mi.getDependencies()) {
            ModuleInfo dependantModule = getDependantModuleInfo(mi, d);
            if (dependantModule != null && !processed.contains(dependantModule)) {
                ret.addAll(getClassPath(dependantModule, processed));
            }
        }
        return ret;
    }

    public static ModuleInfo getDependantModuleInfo(ModuleInfo moduleInfo, Dependency d) {
        Modules modules = Modules.getDefault();

        if (d.getType() == Dependency.TYPE_MODULE) {
            String codeName = d.getName();
            String codeNameBase = codeName.contains("/") ? codeName.substring(0, codeName.indexOf('/')) : codeName;
            return modules.findCodeNameBase(codeNameBase);
        }
        return null;
    }

    public static List<File> getAllModuleJarsUsingReflection(ModuleInfo thisModule) {
        try {
            Method getAllJarsMethod = thisModule.getClass().getMethod("getAllJars");
            getAllJarsMethod.setAccessible(true);
            @SuppressWarnings("unchecked")
            List<File> allJars = (List<File>) getAllJarsMethod.invoke(thisModule);
            return allJars;
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "Exception in getAllModuleJarsUsingReflection for module " + thisModule.getCodeNameBase(), ex);
        }
        return Collections.emptyList();
    }
    
    /**
     * Gets the XML configuration file for a given module using the NetBeans
     * APIs.
     *
     * @param module The module to inspect.
     * @return The FileObject for the module's config file, or null if not
     * found.
     */
    public static FileObject getModuleConfigFile(ModuleInfo module) {
        String codeNameBase = module.getCodeNameBase().replace('.', '-');
        String configFilePath = "Modules/" + codeNameBase + ".xml";
        return FileUtil.getConfigFile(configFilePath);
    }
}