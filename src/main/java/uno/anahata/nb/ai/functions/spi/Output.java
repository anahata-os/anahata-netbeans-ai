/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package uno.anahata.nb.ai.functions.spi;

import java.awt.Component;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.SwingUtilities;
import org.openide.windows.IOProvider;
import org.openide.windows.InputOutput;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;
import uno.anahata.gemini.functions.AITool;

/**
 *
 * @author pablo
 */
public class Output {

    @AITool("Lists all java.awt.Component(s) in the 'output' TopComponent" )
    public static List<String> getOutputComponents() {
        List<String> tabNames = new ArrayList<>();
        TopComponent outputTC = WindowManager.getDefault().findTopComponent("output"); // "output" ID
        if (outputTC != null) {
            java.awt.Component[] comps = outputTC.getComponents();
            for (java.awt.Component c : comps) {
                tabNames.add(c.getClass() + " -> " + c);
            }
        }
        return tabNames;
    }

}
