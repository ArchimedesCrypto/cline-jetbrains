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
 * Tool for applying a diff to a file.
 */
public class ApplyDiffTool extends AbstractTool {
    private static final Logger LOG = Logger.getInstance(ApplyDiffTool.class);
    private static final String NAME = "apply_diff";
    private static final String DESCRIPTION = "Apply a diff to a file";
    
    private final Project project;
    private final ClineFileService fileService;
    
    /**
     * Creates a new apply diff tool.
     *
     * @param project The project
     */
    public ApplyDiffTool(Project project) {
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
        pathProperty.addProperty("description", "The path of the file to modify");
        properties.add("path", pathProperty);
        
        JsonObject diffProperty = new JsonObject();
        diffProperty.addProperty("type", "string");
        diffProperty.addProperty("description", "The search/replace block defining the changes");
        properties.add("diff", diffProperty);
        
        JsonObject startLineProperty = new JsonObject();
        startLineProperty.addProperty("type", "integer");
        startLineProperty.addProperty("description", "The line number where the search block starts");
        properties.add("start_line", startLineProperty);
        
        JsonObject endLineProperty = new JsonObject();
        endLineProperty.addProperty("type", "integer");
        endLineProperty.addProperty("description", "The line number where the search block ends");
        properties.add("end_line", endLineProperty);
        
        schema.add("properties", properties);
        
        JsonObject required = new JsonObject();
        required.addProperty("0", "path");
        required.addProperty("1", "diff");
        required.addProperty("2", "start_line");
        required.addProperty("3", "end_line");
        schema.add("required", required);
        
        return schema;
    }
    
    @Override
    public boolean validateArgs(@NotNull JsonObject args) {
        if (!args.has("path") || !args.get("path").isJsonPrimitive() || !args.get("path").getAsJsonPrimitive().isString()) {
            return false;
        }
        
        if (!args.has("diff") || !args.get("diff").isJsonPrimitive() || !args.get("diff").getAsJsonPrimitive().isString()) {
            return false;
        }
        
        if (!args.has("start_line") || !args.get("start_line").isJsonPrimitive() || !args.get("start_line").getAsJsonPrimitive().isNumber()) {
            return false;
        }
        
        if (!args.has("end_line") || !args.get("end_line").isJsonPrimitive() || !args.get("end_line").getAsJsonPrimitive().isNumber()) {
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
        
        if (!args.has("diff")) {
            return "Missing required parameter: diff";
        }
        if (!args.get("diff").isJsonPrimitive() || !args.get("diff").getAsJsonPrimitive().isString()) {
            return "Parameter 'diff' must be a string";
        }
        
        if (!args.has("start_line")) {
            return "Missing required parameter: start_line";
        }
        if (!args.get("start_line").isJsonPrimitive() || !args.get("start_line").getAsJsonPrimitive().isNumber()) {
            return "Parameter 'start_line' must be a number";
        }
        
        if (!args.has("end_line")) {
            return "Missing required parameter: end_line";
        }
        if (!args.get("end_line").isJsonPrimitive() || !args.get("end_line").getAsJsonPrimitive().isNumber()) {
            return "Parameter 'end_line' must be a number";
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
            String diff = args.get("diff").getAsString();
            int startLine = args.get("start_line").getAsInt();
            int endLine = args.get("end_line").getAsInt();
            
            // Apply the diff
            fileService.applyDiff(path, diff, startLine, endLine)
                    .thenAccept(success -> {
                        // Create a result object
                        JsonObject result = new JsonObject();
                        result.addProperty("success", success);
                        result.addProperty("path", path);
                        result.addProperty("start_line", startLine);
                        result.addProperty("end_line", endLine);
                        
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