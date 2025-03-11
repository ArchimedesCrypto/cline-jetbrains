package com.cline.core.tool.impl;

import com.cline.core.tool.AbstractTool;
import com.cline.core.tool.ToolResult;
import com.cline.services.ClineFileService;
import com.google.gson.JsonObject;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

/**
 * Tool for reading the contents of a file.
 */
public class ReadFileTool extends AbstractTool {
    private static final String NAME = "read_file";
    private static final String DESCRIPTION = "Read the contents of a file at the specified path.";
    
    private final Project project;

    /**
     * Creates a new read file tool.
     *
     * @param project The project
     */
    public ReadFileTool(Project project) {
        super(NAME, DESCRIPTION, createInputSchema());
        this.project = project;
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
        pathProperty.addProperty("description", "The path of the file to read (relative to the current working directory)");
        properties.add("path", pathProperty);
        
        schema.add("properties", properties);
        
        JsonObject required = new JsonObject();
        required.addProperty("0", "path");
        schema.add("required", required);
        
        return schema;
    }

    @Override
    @NotNull
    public CompletableFuture<ToolResult> execute(@NotNull JsonObject args) {
        CompletableFuture<ToolResult> future = new CompletableFuture<>();
        
        if (!validateArgs(args)) {
            completeExceptionally(future, getValidationErrorMessage(args));
            return future;
        }
        
        String path = args.get("path").getAsString();
        
        ClineFileService fileService = ClineFileService.getInstance(project);
        fileService.readFile(path)
                .thenAccept(content -> {
                    JsonObject result = new JsonObject();
                    result.addProperty("content", content);
                    completeSuccessfully(future, result);
                })
                .exceptionally(e -> {
                    completeExceptionally(future, "Error reading file: " + e.getMessage());
                    return null;
                });
        
        return future;
    }

    @Override
    public boolean validateArgs(@NotNull JsonObject args) {
        return args.has("path") && args.get("path").isJsonPrimitive() && args.get("path").getAsJsonPrimitive().isString();
    }

    @Override
    public String getValidationErrorMessage(@NotNull JsonObject args) {
        if (!args.has("path")) {
            return "Missing required parameter: path";
        }
        if (!args.get("path").isJsonPrimitive() || !args.get("path").getAsJsonPrimitive().isString()) {
            return "Parameter 'path' must be a string";
        }
        return null;
    }
}