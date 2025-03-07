package com.cline.jetbrains.actions;

import com.cline.jetbrains.services.ClineProjectService;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowManager;
import org.jetbrains.annotations.NotNull;

/**
 * Action for creating a new Cline task.
 * This class is responsible for creating a new task in the Cline tool window.
 */
public class NewTaskAction extends AnAction {
    private static final Logger LOG = Logger.getInstance(NewTaskAction.class);

    /**
     * Constructor.
     */
    public NewTaskAction() {
        super("New Task", "Create a new Cline task", null);
    }

    /**
     * Perform the action.
     * @param e The action event
     */
    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        if (project == null) {
            return;
        }
        
        LOG.info("Creating new Cline task for project: " + project.getName());
        
        // Get the Cline tool window
        ToolWindowManager toolWindowManager = ToolWindowManager.getInstance(project);
        ToolWindow toolWindow = toolWindowManager.getToolWindow("Cline");
        
        if (toolWindow == null) {
            LOG.error("Cline tool window not found");
            return;
        }
        
        // Show the tool window
        toolWindow.show(() -> {
            // Get the project service
            ClineProjectService projectService = ClineProjectService.getInstance(project);
            
            // TODO: Implement new task creation
            LOG.info("New task creation placeholder");
        });
    }

    /**
     * Update the action presentation.
     * @param e The action event
     */
    @Override
    public void update(@NotNull AnActionEvent e) {
        // Enable the action only if a project is open
        Project project = e.getProject();
        e.getPresentation().setEnabled(project != null);
    }
}
