package com.cline.core.tool.impl;

import com.cline.core.tool.AbstractTool;
import com.cline.core.tool.ToolResult;
import com.cline.services.ClineTerminalService;
import com.google.gson.JsonObject;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

/**
 * Tool for attempting to complete a task.
 */
public class AttemptCompletionTool extends AbstractTool {
    private static final Logger LOG = Logger.getInstance(AttemptCompletionTool.class);
    private static final String NAME = "attempt_completion";
    private static final String DESCRIPTION = "Attempt to complete a task";
    
    private final Project project;
    private final ClineTerminalService terminalService;
    
    /**
     * Creates a new attempt completion tool.
     *
     * @param project The project
     */
    public AttemptCompletionTool(Project project) {
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
        
        JsonObject resultProperty = new JsonObject();
        resultProperty.addProperty("type", "string");
        resultProperty.addProperty("description", "The result of the task");
        properties.add("result", resultProperty);
        
        JsonObject commandProperty = new JsonObject();
        commandProperty.addProperty("type", "string");
        commandProperty.addProperty("description", "A CLI command to execute to show a live demo of the result to the user");
        properties.add("command", commandProperty);
        
        schema.add("properties", properties);
        
        JsonObject required = new JsonObject();
        required.addProperty("0", "result");
        schema.add("required", required);
        
        return schema;
    }
    
    @Override
    public boolean validateArgs(@NotNull JsonObject args) {
        if (!args.has("result") || !args.get("result").isJsonPrimitive() || !args.get("result").getAsJsonPrimitive().isString()) {
            return false;
        }
        
        if (args.has("command") && (!args.get("command").isJsonPrimitive() || !args.get("command").getAsJsonPrimitive().isString())) {
            return false;
        }
        
        return true;
    }
    
    @Override
    @NotNull
    public String getValidationErrorMessage(@NotNull JsonObject args) {
        if (!args.has("result")) {
            return "Missing required parameter: result";
        }
        if (!args.get("result").isJsonPrimitive() || !args.get("result").getAsJsonPrimitive().isString()) {
            return "Parameter 'result' must be a string";
        }
        
        if (args.has("command") && (!args.get("command").isJsonPrimitive() || !args.get("command").getAsJsonPrimitive().isString())) {
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
            String result = args.get("result").getAsString();
            String command = args.has("command") ? args.get("command").getAsString() : null;
            
            // Show a dialog with the result
            Messages.showInfoMessage(
                    project,
                    result,
                    "Task Completed"
            );
            
            // Execute the command if provided
            if (command != null && !command.isEmpty()) {
                terminalService.executeCommandAndCaptureOutput(command, 30)
                        .thenAccept(output -> {
                            // Create a result object
                            JsonObject resultObj = new JsonObject();
                            resultObj.addProperty("result", result);
                            resultObj.addProperty("command", command);
                            resultObj.addProperty("output", output);
                            resultObj.addProperty("success", true);
                            
                            // Complete the future with the result
                            completeSuccessfully(future, resultObj);
                        })
                        .exceptionally(e -> {
                            // Create a result object with the error
                            JsonObject resultObj = new JsonObject();
                            resultObj.addProperty("result", result);
                            resultObj.addProperty("command", command);
                            resultObj.addProperty("error", e.getMessage());
                            resultObj.addProperty("success", false);
                            
                            // Complete the future with the result
                            completeSuccessfully(future, resultObj);
                            return null;
                        });
            } else {
                // Create a result object
                JsonObject resultObj = new JsonObject();
                resultObj.addProperty("result", result);
                resultObj.addProperty("success", true);
                
                // Complete the future with the result
                completeSuccessfully(future, resultObj);
            }
        } catch (Exception e) {
            // Complete the future with the error
            completeExceptionally(future, e);
        }
        
        return future;
    }
}