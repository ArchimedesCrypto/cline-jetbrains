package com.cline.services;

import com.intellij.openapi.components.Service;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowManager;
import org.jetbrains.plugins.terminal.ShellTerminalWidget;
import org.jetbrains.plugins.terminal.TerminalToolWindowFactory;
import org.jetbrains.plugins.terminal.TerminalView;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

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
    
    /**
     * Execute a command in the terminal and capture the output.
     * Note: This is a simplified implementation. In a real implementation,
     * we would need to use JediTerm's terminal emulator to capture the output.
     *
     * @param command The command to execute
     * @param timeout The timeout in seconds
     * @return A CompletableFuture containing the command output
     */
    public CompletableFuture<String> executeCommandAndCaptureOutput(String command, int timeout) {
        // In a real implementation, we would capture the terminal output
        // For now, we'll just execute the command and return a placeholder
        return executeCommand(command)
                .thenApply(v -> "Command executed: " + command);
    }
    
    /**
     * Execute an interactive command in the terminal.
     *
     * @param command The command to execute
     * @param inputs The inputs to provide to the command
     * @return A CompletableFuture that completes when the command is executed
     */
    public CompletableFuture<Void> executeInteractiveCommand(String command, String... inputs) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        
        executeCommand(command)
                .thenCompose(v -> {
                    // Wait a bit for the command to start
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                    
                    // Send inputs
                    return sendInputs(inputs);
                })
                .thenAccept(v -> future.complete(null))
                .exceptionally(e -> {
                    future.completeExceptionally(e);
                    return null;
                });
        
        return future;
    }
    
    /**
     * Send inputs to the terminal.
     *
     * @param inputs The inputs to send
     * @return A CompletableFuture that completes when the inputs are sent
     */
    private CompletableFuture<Void> sendInputs(String... inputs) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        
        try {
            ToolWindow terminalToolWindow = ToolWindowManager.getInstance(project)
                    .getToolWindow(TerminalToolWindowFactory.TOOL_WINDOW_ID);
            
            if (terminalToolWindow == null) {
                throw new IllegalStateException("Terminal tool window not found");
            }
            
            terminalToolWindow.show(() -> {
                try {
                    TerminalView terminalView = TerminalView.getInstance(project);
                    ShellTerminalWidget terminal = getFirstTerminalWidget(terminalView);
                    
                    if (terminal == null) {
                        throw new IllegalStateException("No active terminal found");
                    }
                    
                    // Send inputs with delays
                    for (String input : inputs) {
                        sendText(terminal, input + "\n");
                        Thread.sleep(300); // Small delay between inputs
                    }
                    
                    future.complete(null);
                } catch (Exception e) {
                    LOG.error("Error sending inputs to terminal", e);
                    future.completeExceptionally(e);
                }
            });
        } catch (Exception e) {
            LOG.error("Error accessing terminal", e);
            future.completeExceptionally(e);
        }
        
        return future;
    }
    
    /**
     * Gets the first terminal widget from the terminal view.
     *
     * @param terminalView The terminal view
     * @return The first terminal widget, or null if none exists
     */
    private ShellTerminalWidget getFirstTerminalWidget(TerminalView terminalView) {
        // This is a workaround since getFirstTerminalWidget() is not available
        // In a real implementation, we would use reflection or other means to get the first terminal
        List<ShellTerminalWidget> widgets = terminalView.getWidgets();
        return widgets.isEmpty() ? null : widgets.get(0);
    }
    
    /**
     * Sends text to a terminal widget.
     *
     * @param terminal The terminal widget
     * @param text The text to send
     */
    private void sendText(ShellTerminalWidget terminal, String text) {
        // This is a workaround since sendText() is not available
        // In a real implementation, we would use reflection or other means to send text
        terminal.executeCommand(text);
    }
}