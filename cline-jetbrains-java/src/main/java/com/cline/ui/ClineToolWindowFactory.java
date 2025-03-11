package com.cline.ui;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import org.jetbrains.annotations.NotNull;

/**
 * Factory class for creating the Cline tool window.
 * This class is responsible for creating the main UI component of the plugin.
 */
public class ClineToolWindowFactory implements ToolWindowFactory {
    
    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        ClineToolWindowContent toolWindowContent = new ClineToolWindowContent(project, toolWindow);
        Content content = ContentFactory.getInstance().createContent(
                toolWindowContent.getContent(), "", false);
        toolWindow.getContentManager().addContent(content);
    }
    
    @Override
    public void init(@NotNull ToolWindow toolWindow) {
        toolWindow.setStripeTitle("Cline");
        toolWindow.setIcon(com.cline.ui.icons.ClineIcons.CLINE_ICON);
    }
    
    @Override
    public boolean shouldBeAvailable(@NotNull Project project) {
        return true;
    }
}