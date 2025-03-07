package com.cline.jetbrains.ui;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import org.jetbrains.annotations.NotNull;

/**
 * Factory for creating the Cline tool window.
 * This class is responsible for creating the main UI component of the plugin.
 */
public class ClineToolWindowFactory implements ToolWindowFactory {
    private static final Logger LOG = Logger.getInstance(ClineToolWindowFactory.class);

    /**
     * Create the tool window content.
     * @param project The current project
     * @param toolWindow The tool window to populate
     */
    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        LOG.info("Creating Cline tool window for project: " + project.getName());
        
        // Create the main panel
        ClineToolWindowPanel panel = new ClineToolWindowPanel(project, toolWindow);
        
        // Add the panel to the tool window
        ContentFactory contentFactory = ContentFactory.getInstance();
        Content content = contentFactory.createContent(panel, "", false);
        toolWindow.getContentManager().addContent(content);
    }

    /**
     * Called when the tool window is initialized.
     * @param toolWindow The tool window
     */
    @Override
    public void init(@NotNull ToolWindow toolWindow) {
        LOG.info("Initializing Cline tool window");
        
        // Set tool window properties
        toolWindow.setStripeTitle("Cline");
        toolWindow.setTitle("Cline");
    }

    /**
     * Determine if the tool window should be available for the project.
     * @param project The project to check
     * @return True if the tool window should be available
     */
    @Override
    public boolean shouldBeAvailable(@NotNull Project project) {
        // The tool window should be available for all projects
        return true;
    }
}