package com.cline.services;

import com.intellij.openapi.components.Service;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowManager;
import org.jetbrains.plugins.terminal.ShellTerminalWidget;
import org.jetbrains.plugins.terminal.TerminalToolWindowFactory;
import org.jetbrains.plugins.terminal.TerminalView;

import java.util.concurrent.CompletableFuture;

/**
 * Service for terminal operations in the Cline plugin.
 */
@Service
public final class ClineTerminalService {
    private static final Logger LOG = Logger.getInstance(ClineTerminalService.class);
    private final Project project;

    public ClineTerminalService(Project project) {
        this.project = project;
    }

    public static ClineTerminalService getInstance(Project project) {
        return project.getService(ClineTerminalService.class);
    }

    /**
     * Execute a command in the terminal.
     *
     * @param command The command to execute
     * @return A CompletableFuture that completes when the command is executed
     */
    public CompletableFuture<Void> executeCommand(String command) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        
        try {
            // Get or create terminal window
            ToolWindow terminalToolWindow = ToolWindowManager.getInstance(project)
                    .getToolWindow(TerminalToolWindowFactory.TOOL_WINDOW_ID);
            
            if (terminalToolWindow == null) {
                throw new IllegalStateException("Terminal tool window not found");
            }
            
            // Ensure terminal is visible
            terminalToolWindow.show(() -> {
                try {
                    // Get terminal view
                    TerminalView terminalView = TerminalView.getInstance(project);
                    ShellTerminalWidget terminal = terminalView.createLocalShellWidget(
                            project.getBasePath(), "Cline Terminal");
                    
                    // Execute command
                    terminal.executeCommand(command);
                    future.complete(null);
                } catch (Exception e) {
                    LOG.error("Error executing command in terminal", e);
                    future.completeExceptionally(e);
                }
            });
        } catch (Exception e) {
            LOG.error("Error opening terminal", e);
            future.completeExceptionally(e);
        }
        
        return future;
    }
}