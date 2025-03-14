package com.cline.core.tool.impl;

import com.cline.core.tool.AbstractTool;
import com.cline.core.tool.ToolResult;
import com.cline.services.ClineFileService;
import com.google.gson.JsonObject;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

/**
 * Tool for reading files.
 */
public class ReadFileTool extends AbstractTool {
    private static final String NAME = "read_file";
    private static final String DESCRIPTION = "Read the contents of a file";
    
    private final Project project;
    private final ClineFileService fileService;
    
    /**
     * Creates a new read file tool.
     *
     * @param project The project
     */
    public ReadFileTool(Project project) {
        super(NAME, DESCRIPTION, createInputSchema());
        this.project = project;
        this.fileService = ClineFileService.getInstance(project);
    }
    
    /**
     * Creates the input schema for the tool.
     *
     * @return The input schema
     */
    private static JsonObject createInputSchema() {
        JsonObject schema = new JsonObject();
        
        JsonObject properties = new JsonObject();
        
        JsonObject pathProperty = new JsonObject();
        pathProperty.addProperty("type", "string");
        pathProperty.addProperty("description", "The path of the file to read");
        properties.add("path", pathProperty);
        
        schema.add("properties", properties);
        
        JsonObject required = new JsonObject();
        required.addProperty("0", "path");
        schema.add("required", required);
        
        return schema;
    }
    
    @Override
    public boolean validateArgs(@NotNull JsonObject args) {
        return args.has("path") && args.get("path").isJsonPrimitive() && args.get("path").getAsJsonPrimitive().isString();
    }
    
    @Override
    @NotNull
    public String getValidationErrorMessage(@NotNull JsonObject args) {
        if (!args.has("path")) {
            return "Missing required parameter: path";
        }
        if (!args.get("path").isJsonPrimitive() || !args.get("path").getAsJsonPrimitive().isString()) {
            return "Parameter 'path' must be a string";
        }
        return null;
    }
    
    @Override
    @NotNull
    public CompletableFuture<ToolResult> execute(@NotNull JsonObject args) {
        CompletableFuture<ToolResult> future = new CompletableFuture<>();
        
        try {
            // Get the path parameter
            String path = args.get("path").getAsString();
            
            // Read the file
            fileService.readFile(path)
                    .thenAccept(content -> {
                        // Create a result object
                        JsonObject result = new JsonObject();
                        result.addProperty("content", content);
                        
                        // Complete the future with the result
                        completeSuccessfully(future, result);
                    })
                    .exceptionally(e -> {
                        // Complete the future with the error
                        completeExceptionally(future, e);
                        return null;
                    });
        } catch (Exception e) {
            // Complete the future with the error
            completeExceptionally(future, e);
        }
        
        return future;
    }
}