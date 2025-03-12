package com.cline.core.tool;

import com.cline.core.model.Conversation;
import com.cline.core.model.Message;
import com.cline.services.ClineApiService;
import com.google.gson.JsonObject;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Service for executing tools.
 */
@Service
public final class ToolExecutor {
    private static final Logger LOG = Logger.getInstance(ToolExecutor.class);
    private final Project project;
    private final Map<String, Tool> tools = new HashMap<>();

    public ToolExecutor(Project project) {
        this.project = project;
    }

    public static ToolExecutor getInstance(Project project) {
        return project.getService(ToolExecutor.class);
    }

    /**
     * Registers a tool.
     *
     * @param tool The tool to register
     */
    public void registerTool(@NotNull Tool tool) {
        tools.put(tool.getName(), tool);
        LOG.info("Registered tool: " + tool.getName());
    }

    /**
     * Unregisters a tool.
     *
     * @param toolName The name of the tool to unregister
     */
    public void unregisterTool(@NotNull String toolName) {
        tools.remove(toolName);
        LOG.info("Unregistered tool: " + toolName);
    }

    /**
     * Gets a registered tool by name.
     *
     * @param toolName The name of the tool
     * @return The tool, or null if not found
     */
    @Nullable
    public Tool getTool(@NotNull String toolName) {
        return tools.get(toolName);
    }

    /**
     * Gets all registered tools.
     *
     * @return A map of tool names to tools
     */
    @NotNull
    public Map<String, Tool> getTools() {
        return new HashMap<>(tools);
    }

    /**
     * Executes a tool.
     *
     * @param toolName The name of the tool to execute
     * @param args     The arguments for the tool
     * @return A future that completes with the tool result
     */
    @NotNull
    public CompletableFuture<ToolResult> executeTool(@NotNull String toolName, @NotNull JsonObject args) {
        Tool tool = getTool(toolName);
        if (tool == null) {
            return CompletableFuture.completedFuture(
                    ToolResult.failure("Tool not found: " + toolName)
            );
        }

        if (!tool.validateArgs(args)) {
            String errorMessage = tool.getValidationErrorMessage(args);
            if (errorMessage == null) {
                errorMessage = "Invalid arguments for tool: " + toolName;
            }
            return CompletableFuture.completedFuture(ToolResult.failure(errorMessage));
        }

        try {
            return tool.execute(args);
        } catch (Exception e) {
            LOG.error("Error executing tool: " + toolName, e);
            return CompletableFuture.completedFuture(
                    ToolResult.failure("Error executing tool: " + e.getMessage())
            );
        }
    }

    /**
     * Processes a message for tool use requests and executes any tools.
     *
     * @param conversation The conversation
     * @param message      The message to process
     * @return A future that completes when all tools are executed
     */
    @NotNull
    public CompletableFuture<Void> processToolUses(@NotNull Conversation conversation, @NotNull Message message) {
        // In a real implementation, we would parse the message content for tool use requests
        // For now, we'll just check if the message contains a tool use
        if (message.isToolUse()) {
            // This is a simplified implementation
            // In a real implementation, we would extract the tool name and arguments from the message
            return CompletableFuture.completedFuture(null);
        }
        
        return CompletableFuture.completedFuture(null);
    }
}