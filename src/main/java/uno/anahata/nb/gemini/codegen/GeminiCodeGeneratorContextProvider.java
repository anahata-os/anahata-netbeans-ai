/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/NetBeansModuleDevelopment-files/contextProvider.java to edit this template
 */
package uno.anahata.nb.gemini.codegen;

import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.spi.editor.codegen.CodeGeneratorContextProvider;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;

@MimeRegistration(mimeType = "text/plain", service = CodeGeneratorContextProvider.class)
public class GeminiCodeGeneratorContextProvider implements CodeGeneratorContextProvider {

    /**
     * Adds an additional content to the original context and runs the given
     * task with the new context as a parameter.
     *
     * @param context the original context
     * @param task the task to be run
     */
    public void runTaskWithinContext(Lookup context, Task task) {
        //JTextComponent is always guaranteed, you can get it like this
        //JTextComponent component = context.lookup(JTextComponent.class);

        // Create new Lookup with extra items to be used in the task
        // Make sure that newContext contains the original context
        Lookup extraItems = Lookups.fixed(/* Add you extra items here */);
        Lookup newContext = new ProxyLookup(context, extraItems);

        // You may aquire a lock here:
        //try {
        task.run(newContext);
        // } finally {
        // Don't forget to unlock here
        //}
    }

}
