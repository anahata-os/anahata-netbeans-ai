package uno.anahata.nb.ai.tools;

import java.util.prefs.Preferences;
import org.openide.util.NbPreferences;
import uno.anahata.gemini.functions.AIToolMethod;

/**
 * Provides AI tool methods for interacting with Maven projects.
 * @author pablo
 */
public class Maven {
    @AIToolMethod("Gets the path to the Maven installation configured in NetBeans.")
    public static String getMavenPath() {
        try {
            Preferences prefs = NbPreferences.root().node("org/netbeans/modules/maven");
            return prefs.get("commandLineMavenPath", "PREFERENCE_NOT_FOUND");
        } catch (Throwable t) {
            return "EXECUTION_FAILED: " + t.toString();
        }
    }
}
