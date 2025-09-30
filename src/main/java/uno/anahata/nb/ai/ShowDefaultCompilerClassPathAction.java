/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package uno.anahata.nb.ai;

import uno.anahata.nb.ai.deprecated.ClassPathUtils;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.modules.Dependency;
import org.openide.modules.ModuleInfo;
import org.openide.modules.Modules;
import org.openide.util.NbBundle.Messages;
import org.openide.windows.IOProvider;
import org.openide.windows.InputOutput;
import org.openide.windows.OutputWriter;
import uno.anahata.gemini.functions.spi.RunningJVM;
import static uno.anahata.nb.ai.deprecated.ModuleInfoHelper.getGeminiModuleJars;

/**
 * Show gemini's compilers classpath on a new output tab.
 * 
 * @author pablo
 */
@ActionID(
        category = "Tools",
        id = "uno.anahata.nb.ai.ShowDefaultCompilerClassPathAction"
)
@ActionRegistration(
        displayName = "Show Gemini Classpath"
)
@ActionReference(path = "Menu/Tools", position = 10)
public final class ShowDefaultCompilerClassPathAction implements ActionListener {
    
    private static final Logger logger = Logger.getLogger(ShowDefaultCompilerClassPathAction.class.getName());

    OutputWriter out;

    Set<ModuleInfo> processed = new HashSet<>();

    @Override
    public void actionPerformed(ActionEvent e) {
        InputOutput io = IOProvider.getDefault().getIO("Gemini Compiler's Classpath", true);
        io.select();
        processed.clear();
        try (OutputWriter out = io.getOut()) {
            this.out = out;
            //initExecuteJavaCode();
            String cp = RunningJVM.getDefaultCompilerClasspath();
            out.println("-----------------------------------------------------------------------");
            String[] s = cp.split(File.pathSeparator);
            for (int i = 0; i < s.length; i++) {
                out.println(s[i]);
            }
            
            out.println("-----------------------------------------------------------------------");
            out.println(" Total jars: " + s.length);
            out.println("-----------------------------------------------------------------------");
            
        }
    }
    
    
    
    public static void initRunningJVM() {

        try {
            String javaClassPath = RunningJVM.initDefaultCompilerClasspath();
            String netbeansDynamicClassPath = System.getProperty("netbeans.dynamic.classpath");
            logger.info("javaClassPath: " + javaClassPath);
            logger.info("nbDynamic: " + netbeansDynamicClassPath);
            
            Set<File> moduleClassPath = getModuleClassPath();
            logger.info("moduleClasspath: " + moduleClassPath);
            String moduleClassPathStr = ClassPathUtils.filesToClassPathString(moduleClassPath);
            
            String newClassPathString = 
                      javaClassPath + File.pathSeparator 
                    + netbeansDynamicClassPath + File.pathSeparator
                    + moduleClassPathStr;
            RunningJVM.setDefaultCompilerClasspath(newClassPathString);
            logger.info("Final Classpath: " + newClassPathString);
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
        // Get the module system
        Modules modules = Modules.getDefault();

        if (d.getType() == Dependency.TYPE_RECOMMENDS) {
        } else if (d.getType() == Dependency.TYPE_REQUIRES) {

        } else if (d.getType() == Dependency.TYPE_JAVA) {

        } else if (d.getType() == Dependency.TYPE_MODULE) {
            // Get the code name of the dependency (e.g., "org.openide.filesystems")
            String codeName = d.getName();
            // Remove version or other qualifiers if present (e.g., "org.openide.filesystems/1")
            String codeNameBase = codeName.contains("/") ? codeName.substring(0, codeName.indexOf('/')) : codeName;
            // Find the ModuleInfo for the dependency
            return modules.findCodeNameBase(codeNameBase);
        } else {
            //out.println(d.getType() + " ** Not TYPE_MODULE: " + d + " of module " + moduleInfo);
        }
        return null;

    }

    private static List<File> getAllModuleJarsUsingReflection(ModuleInfo thisModule) {
        try {
            // Use reflection to call getAllJars() since we cannot cast to StandardModule directly
            Method getAllJarsMethod = thisModule.getClass().getMethod("getAllJars");

            //out.println("Successfully found method: " + getAllJarsMethod.getName());
            // *** THIS IS THE CRITICAL FIX ***
            // We must set it to accessible to bypass Java's module access restrictions.
            getAllJarsMethod.setAccessible(true);
            //out.println("Applied 'setAccessible(true)' to the method.");

            @SuppressWarnings("unchecked")
            List<File> allJars = (List<File>) getAllJarsMethod.invoke(thisModule);

            return allJars;

        } catch (NoSuchMethodException nsme) {
            nsme.printStackTrace(System.out);
            //logger.log(Level.SEVERE, "Exception in getAllJars");
            //out.println("\nTEST FAILED: The method 'getAllJars' does not exist on the class " + thisModule.getClass().getName());
        } catch (Exception ex) {
            //out.println("\nTEST FAILED: An exception occurred while trying to call 'getAllJars'.");
            ex.printStackTrace(System.out);
        }
        return null;

    }

    /**
     * Gets the XML configuration file for a given module using the NetBeans
     * APIs.
     *
     * @param module The module to inspect.
     * @return The FileObject for the module's config file, or null if not
     * found.
     */
    private FileObject getModuleConfigFile(ModuleInfo module) {
        String codeNameBase = module.getCodeNameBase().replace('.', '-');
        String configFilePath = "Modules/" + codeNameBase + ".xml";
        return FileUtil.getConfigFile(configFilePath);
    }
}
