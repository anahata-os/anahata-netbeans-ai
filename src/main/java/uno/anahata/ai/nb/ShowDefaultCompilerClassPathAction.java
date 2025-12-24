/* Licensed under the Apache License, Version 2.0 */
package uno.anahata.ai.nb;

import uno.anahata.ai.nb.util.ClassPathBuilder;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.modules.Dependency;
import org.openide.modules.ModuleInfo;
import org.openide.modules.Modules;
import org.openide.windows.IOProvider;
import org.openide.windows.InputOutput;
import org.openide.windows.OutputWriter;
import uno.anahata.ai.tools.spi.RunningJVM;


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
        displayName = "Show Anahata Classpath"
)
@ActionReference(path = "Menu/Tools", position = 10)
public final class ShowDefaultCompilerClassPathAction implements ActionListener {
    
    private static final Logger logger = Logger.getLogger(ShowDefaultCompilerClassPathAction.class.getName());

    
    @Override
    public void actionPerformed(ActionEvent e) {
        InputOutput io = IOProvider.getDefault().getIO("Anahata's Default Compiler's Classpath", true);
        io.select();
        try (OutputWriter out = io.getOut()) {
            
            //initExecuteJavaCode();
            String cp = RunningJVM.getDefaultCompilerClasspath();
            out.println("-----------------------------------------------------------------------");
            String[] s = cp.split(File.pathSeparator);
            
            out.println("-----------------------------------------------------------------------");
            out.println(RunningJVM.getPrettyPrintedDefaultCompilerClasspath());
            out.println("-----------------------------------------------------------------------");
            out.println(" Total jars: " + s.length);
            out.println("-----------------------------------------------------------------------");
            
        }
    }
    
    
}