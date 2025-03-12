package com.cline.core.tool.impl;

import com.cline.core.tool.AbstractTool;
import com.cline.core.tool.ToolResult;
import com.cline.services.ClineFileService;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Tool for listing files in a directory.
 */
public class ListFilesTool extends AbstractTool {
    private static final Logger LOG = Logger.getInstance(ListFilesTool.class);
    private static final String NAME = "list_files";
    private static final String DESCRIPTION = "List files in a directory";
    
    private final Project project;
    private final ClineFileService fileService;
    
    /**
     * Creates a new list files tool.
     *
     * @param project The project
     */
    public ListFilesTool(Project project) {
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
        pathProperty.addProperty("description", "The path of the directory to list contents for");
        properties.add("path", pathProperty);
        
        JsonObject recursiveProperty = new JsonObject();
        recursiveProperty.addProperty("type", "boolean");
        recursiveProperty.addProperty("description", "Whether to list files recursively");
        properties.add("recursive", recursiveProperty);
        
        schema.add("properties", properties);
        
        JsonObject required = new JsonObject();
        required.addProperty("0", "path");
        schema.add("required", required);
        
        return schema;
    }
    
    @Override
    public boolean validateArgs(@NotNull JsonObject args) {
        if (!args.has("path") || !args.get("path").isJsonPrimitive() || !args.get("path").getAsJsonPrimitive().isString()) {
            return false;
        }
        
        if (args.has("recursive") && (!args.get("recursive").isJsonPrimitive() || !args.get("recursive").getAsJsonPrimitive().isBoolean())) {
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
        
        if (args.has("recursive") && (!args.get("recursive").isJsonPrimitive() || !args.get("recursive").getAsJsonPrimitive().isBoolean())) {
            return "Parameter 'recursive' must be a boolean";
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
            boolean recursive = args.has("recursive") && args.get("recursive").getAsBoolean();
            
            // List the files
            fileService.listFiles(path, recursive)
                    .thenAccept(files -> {
                        // Create a result object
                        JsonObject result = new JsonObject();
                        JsonArray filesArray = new JsonArray();
                        
                        for (String file : files) {
                            filesArray.add(file);
                        }
                        
                        result.add("files", filesArray);
                        result.addProperty("count", filesArray.size());
                        result.addProperty("path", path);
                        result.addProperty("recursive", recursive);
                        
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