/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package uno.anahata.ai.nb.vcs;

import java.io.File;
import java.util.logging.Logger;
import org.netbeans.modules.versioning.spi.VCSAnnotator;
import org.netbeans.modules.versioning.spi.VCSHistoryProvider;
import org.netbeans.modules.versioning.spi.VCSInterceptor;
import org.netbeans.modules.versioning.spi.VCSVisibilityQuery;
import org.netbeans.modules.versioning.spi.VersioningSystem;
import org.netbeans.modules.versioning.spi.VersioningSystem.Registration;
import org.openide.util.lookup.ServiceProvider;

/**
 * Registers our custom VersioningSystem, which is required to activate the
 * VCSAnnotator pipeline for projects that are managed by the Anahata AI Assistant.
 */
/*
@ServiceProvider(service = VersioningSystem.class)
@Registration(
    displayName = "Anahata AI Context",
    menuLabel = "Anahata AI",
    metadataFolderNames = {"anahata.md"}, // We'll use the presence of anahata.md to identify managed projects.
    actionsCategory = "Team"
)*/
public class AnahataContextVCS extends VersioningSystem {

    private static final Logger LOG = Logger.getLogger(AnahataContextVCS.class.getName());
    private final VCSAnnotator annotator = new AnahataContextAnnotator();

    @Override
    public VCSAnnotator getVCSAnnotator() {
        return annotator;
    }

    @Override
    public File getTopmostManagedAncestor(File file) {
        File current = file;
        while (current != null) {
            if (new File(current, "anahata.md").exists()) {
                return current;
            }
            current = current.getParentFile();
        }
        return null;
    }

    //<editor-fold defaultstate="collapsed" desc="Unused SPI Methods">
    @Override
    public VCSInterceptor getVCSInterceptor() {
        return null;
    }

    @Override
    public VCSHistoryProvider getVCSHistoryProvider() {
        return null;
    }

    @Override
    public void getOriginalFile(File workingCopy, File originalFile) {
        // Not applicable
    }

    @Override
    public VCSVisibilityQuery getVisibilityQuery() {
        return null;
    }
    //</editor-fold>
}
