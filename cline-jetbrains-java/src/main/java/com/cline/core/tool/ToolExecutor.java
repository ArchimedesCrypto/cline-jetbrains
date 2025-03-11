package com.cline.core.tool;

import com.google.gson.JsonObject;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

/**
 * Service for executing tools.
 */
@Service
public final class ToolExecutor {
    private static final Logger LOG = Logger.getInstance(ToolExecutor.class);
    
    private final Project project;
    private final ToolRegistry toolRegistry;

    /**
     * Creates a new tool executor.
     *
     * @param project The project
     */
    public ToolExecutor(Project project) {
        this.project = project;
        this.toolRegistry = ToolRegistry.getInstance(project);
    }

    /**
     * Gets the tool executor instance for the given project.
     *
     * @param project The project
     * @return The tool executor instance
     */
    public static ToolExecutor getInstance(@NotNull Project project) {
        return project.getService(ToolExecutor.class);
    }

    /**
     * Executes a tool with the given name and arguments.
     *
     * @param toolName The name of the tool to execute
     * @param args     The arguments for the tool
     * @return A future that completes with the tool result
     */
    @NotNull
    public CompletableFuture<ToolResult> executeTool(@NotNull String toolName, @NotNull JsonObject args) {
        LOG.info("Executing tool: " + toolName);
        
        Tool tool = toolRegistry.getTool(toolName);
        if (tool == null) {
            CompletableFuture<ToolResult> future = new CompletableFuture<>();
            future.complete(ToolResult.failure("Tool not found: " + toolName));
            return future;
        }
        
        if (!tool.validateArgs(args)) {
            CompletableFuture<ToolResult> future = new CompletableFuture<>();
            String errorMessage = tool.getValidationErrorMessage(args);
            if (errorMessage == null) {
                errorMessage = "Invalid arguments for tool: " + toolName;
            }
            future.complete(ToolResult.failure(errorMessage));
            return future;
        }
        
        try {
            return tool.execute(args)
                    .exceptionally(e -> {
                        LOG.error("Error executing tool: " + toolName, e);
                        return ToolResult.failure("Error executing tool: " + e.getMessage());
                    });
        } catch (Exception e) {
            LOG.error("Error executing tool: " + toolName, e);
            CompletableFuture<ToolResult> future = new CompletableFuture<>();
            future.complete(ToolResult.failure("Error executing tool: " + e.getMessage()));
            return future;
        }
    }

    /**
     * Executes a tool with the given name and arguments.
     *
     * @param toolName The name of the tool to execute
     * @param args     The arguments for the tool as a string
     * @return A future that completes with the tool result
     */
    @NotNull
    public CompletableFuture<ToolResult> executeToolFromString(@NotNull String toolName, @NotNull String args) {
        try {
            JsonObject jsonArgs = com.google.gson.JsonParser.parseString(args).getAsJsonObject();
            return executeTool(toolName, jsonArgs);
        } catch (Exception e) {
            LOG.error("Error parsing tool arguments: " + args, e);
            CompletableFuture<ToolResult> future = new CompletableFuture<>();
            future.complete(ToolResult.failure("Error parsing tool arguments: " + e.getMessage()));
            return future;
        }
    }
}