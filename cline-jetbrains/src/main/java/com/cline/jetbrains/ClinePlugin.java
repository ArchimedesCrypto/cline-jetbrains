package com.cline.jetbrains;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.startup.StartupActivity;
import org.jetbrains.annotations.NotNull;

/**
 * Main plugin class for Cline JetBrains integration.
 * This class is responsible for initializing the plugin and setting up the TypeScript bridge.
 */
@Service
public final class ClinePlugin implements StartupActivity {
    private static final Logger LOG = Logger.getInstance(ClinePlugin.class);

    /**
     * Called when the plugin is loaded.
     * @param project The current project
     */
    @Override
    public void runActivity(@NotNull Project project) {
        LOG.info("Initializing Cline plugin for project: " + project.getName());
        
        // Initialize the plugin
        initialize(project);
    }

    /**
     * Initialize the plugin components.
     * @param project The current project
     */
    private void initialize(Project project) {
        try {
            // Initialize TypeScript bridge
            initializeTypeScriptBridge(project);
            
            // Register listeners
            registerListeners(project);
            
            LOG.info("Cline plugin initialized successfully");
        } catch (Exception e) {
            LOG.error("Failed to initialize Cline plugin", e);
        }
    }

    /**
     * Initialize the TypeScript bridge.
     * @param project The current project
     */
    private void initializeTypeScriptBridge(Project project) {
        // TODO: Implement TypeScript bridge initialization
        LOG.info("TypeScript bridge initialization placeholder");
    }

    /**
     * Register listeners for project events.
     * @param project The current project
     */
    private void registerListeners(Project project) {
        // TODO: Register listeners for project events
        LOG.info("Listener registration placeholder");
    }

    /**
     * Get the instance of the ClinePlugin service.
     * @return The ClinePlugin instance
     */
    public static ClinePlugin getInstance() {
        return ApplicationManager.getApplication().getService(ClinePlugin.class);
    }
}