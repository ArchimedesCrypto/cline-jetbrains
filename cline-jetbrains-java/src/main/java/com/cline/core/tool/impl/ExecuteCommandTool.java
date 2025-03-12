package com.cline.core.tool.impl;

import com.cline.core.tool.AbstractTool;
import com.cline.core.tool.ToolResult;
import com.cline.services.ClineTerminalService;
import com.google.gson.JsonObject;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

/**
 * Tool for executing a CLI command.
 */
public class ExecuteCommandTool extends AbstractTool {
    private static final Logger LOG = Logger.getInstance(ExecuteCommandTool.class);
    private static final String NAME = "execute_command";
    private static final String DESCRIPTION = "Execute a CLI command";
    
    private final Project project;
    private final ClineTerminalService terminalService;
    
    /**
     * Creates a new execute command tool.
     *
     * @param project The project
     */
    public ExecuteCommandTool(Project project) {
        super(NAME, DESCRIPTION, createInputSchema());
        this.project = project;
        this.terminalService = ClineTerminalService.getInstance(project);
    }
    
    /**
     * Creates the input schema for the tool.
     *
     * @return The input schema
     */
    private static JsonObject createInputSchema() {
        JsonObject schema = new JsonObject();
        
        JsonObject properties = new JsonObject();
        
        JsonObject commandProperty = new JsonObject();
        commandProperty.addProperty("type", "string");
        commandProperty.addProperty("description", "The CLI command to execute");
        properties.add("command", commandProperty);
        
        schema.add("properties", properties);
        
        JsonObject required = new JsonObject();
        required.addProperty("0", "command");
        schema.add("required", required);
        
        return schema;
    }
    
    @Override
    public boolean validateArgs(@NotNull JsonObject args) {
        if (!args.has("command") || !args.get("command").isJsonPrimitive() || !args.get("command").getAsJsonPrimitive().isString()) {
            return false;
        }
        
        return true;
    }
    
    @Override
    @NotNull
    public String getValidationErrorMessage(@NotNull JsonObject args) {
        if (!args.has("command")) {
            return "Missing required parameter: command";
        }
        if (!args.get("command").isJsonPrimitive() || !args.get("command").getAsJsonPrimitive().isString()) {
            return "Parameter 'command' must be a string";
        }
        
        return "";
    }
    
    @Override
    @NotNull
    public CompletableFuture<ToolResult> execute(@NotNull JsonObject args) {
        CompletableFuture<ToolResult> future = new CompletableFuture<>();
        
        try {
            // Get the parameters
            String command = args.get("command").getAsString();
            
            // Execute the command
            terminalService.executeCommandAndCaptureOutput(command, 60)
                    .thenAccept(output -> {
                        // Create a result object
                        JsonObject result = new JsonObject();
                        result.addProperty("command", command);
                        result.addProperty("output", output);
                        result.addProperty("success", true);
                        
                        // Complete the future with the result
                        completeSuccessfully(future, result);
                    })
                    .exceptionally(e -> {
                        // Create a result object with the error
                        JsonObject result = new JsonObject();
                        result.addProperty("command", command);
                        result.addProperty("error", e.getMessage());
                        result.addProperty("success", false);
                        
                        // Complete the future with the result
                        completeSuccessfully(future, result);
                        return null;
                    });
        } catch (Exception e) {
            // Complete the future with the error
            completeExceptionally(future, e);
        }
        
        return future;
    }
}