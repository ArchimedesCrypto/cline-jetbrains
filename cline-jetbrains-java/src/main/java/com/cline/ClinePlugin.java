package com.cline;

import com.cline.core.tool.ToolExecutor;
import com.cline.core.tool.ToolRegistry;
import com.cline.services.ClineApiService;
import com.cline.services.ClineFileService;
import com.cline.services.ClineSettingsService;
import com.cline.services.ClineTerminalService;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.StartupActivity;
import org.jetbrains.annotations.NotNull;

/**
 * Main entry point for the Cline plugin.
 * This class is responsible for initializing the plugin and registering services.
 */
public class ClinePlugin implements StartupActivity {
    private static final Logger LOG = Logger.getInstance(ClinePlugin.class);

    @Override
    public void runActivity(@NotNull Project project) {
        LOG.info("Cline plugin initialized for project: " + project.getName());
        
        // Initialize services and components
        initializeServices(project);
    }

    private void initializeServices(Project project) {
        try {
            LOG.info("Initializing Cline services...");
            
            // Initialize core services
            ClineSettingsService settingsService = ClineSettingsService.getInstance();
            LOG.info("Settings service initialized");
            
            ClineApiService apiService = ClineApiService.getInstance();
            LOG.info("API service initialized");
            
            ClineFileService fileService = ClineFileService.getInstance(project);
            LOG.info("File service initialized");
            
            ClineTerminalService terminalService = ClineTerminalService.getInstance(project);
            LOG.info("Terminal service initialized");
            
            // Initialize tool registry and executor
            ToolRegistry toolRegistry = ToolRegistry.getInstance(project);
            LOG.info("Tool registry initialized with " + toolRegistry.getToolCount() + " tools");
            
            ToolExecutor toolExecutor = ToolExecutor.getInstance(project);
            LOG.info("Tool executor initialized");
            
            LOG.info("All Cline services initialized successfully");
        } catch (Exception e) {
            LOG.error("Error initializing Cline services", e);
        }
    }
}