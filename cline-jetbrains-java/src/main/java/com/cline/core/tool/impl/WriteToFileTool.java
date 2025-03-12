package com.cline.core.tool.impl;

import com.cline.core.tool.AbstractTool;
import com.cline.core.tool.ToolResult;
import com.cline.services.ClineFileService;
import com.google.gson.JsonObject;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

/**
 * Tool for writing content to a file.
 */
public class WriteToFileTool extends AbstractTool {
    private static final Logger LOG = Logger.getInstance(WriteToFileTool.class);
    private static final String NAME = "write_to_file";
    private static final String DESCRIPTION = "Write content to a file";
    
    private final Project project;
    private final ClineFileService fileService;
    
    /**
     * Creates a new write to file tool.
     *
     * @param project The project
     */
    public WriteToFileTool(Project project) {
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
        pathProperty.addProperty("description", "The path of the file to write to");
        properties.add("path", pathProperty);
        
        JsonObject contentProperty = new JsonObject();
        contentProperty.addProperty("type", "string");
        contentProperty.addProperty("description", "The content to write to the file");
        properties.add("content", contentProperty);
        
        JsonObject lineCountProperty = new JsonObject();
        lineCountProperty.addProperty("type", "integer");
        lineCountProperty.addProperty("description", "The number of lines in the file");
        properties.add("line_count", lineCountProperty);
        
        schema.add("properties", properties);
        
        JsonObject required = new JsonObject();
        required.addProperty("0", "path");
        required.addProperty("1", "content");
        required.addProperty("2", "line_count");
        schema.add("required", required);
        
        return schema;
    }
    
    @Override
    public boolean validateArgs(@NotNull JsonObject args) {
        if (!args.has("path") || !args.get("path").isJsonPrimitive() || !args.get("path").getAsJsonPrimitive().isString()) {
            return false;
        }
        
        if (!args.has("content") || !args.get("content").isJsonPrimitive() || !args.get("content").getAsJsonPrimitive().isString()) {
            return false;
        }
        
        if (!args.has("line_count") || !args.get("line_count").isJsonPrimitive() || !args.get("line_count").getAsJsonPrimitive().isNumber()) {
            return false;
        }
        
        return true;
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
        
        if (!args.has("content")) {
            return "Missing required parameter: content";
        }
        if (!args.get("content").isJsonPrimitive() || !args.get("content").getAsJsonPrimitive().isString()) {
            return "Parameter 'content' must be a string";
        }
        
        if (!args.has("line_count")) {
            return "Missing required parameter: line_count";
        }
        if (!args.get("line_count").isJsonPrimitive() || !args.get("line_count").getAsJsonPrimitive().isNumber()) {
            return "Parameter 'line_count' must be a number";
        }
        
        return null;
    }
    
    @Override
    @NotNull
    public CompletableFuture<ToolResult> execute(@NotNull JsonObject args) {
        CompletableFuture<ToolResult> future = new CompletableFuture<>();
        
        try {
            // Get the parameters
            String path = args.get("path").getAsString();
            String content = args.get("content").getAsString();
            int lineCount = args.get("line_count").getAsInt();
            
            // Write the file
            fileService.writeFile(path, content)
                    .thenAccept(success -> {
                        // Create a result object
                        JsonObject result = new JsonObject();
                        result.addProperty("success", success);
                        result.addProperty("path", path);
                        result.addProperty("line_count", lineCount);
                        
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