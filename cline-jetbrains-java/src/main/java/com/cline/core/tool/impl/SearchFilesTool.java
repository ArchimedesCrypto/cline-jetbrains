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
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Tool for searching files with a regex pattern.
 */
public class SearchFilesTool extends AbstractTool {
    private static final Logger LOG = Logger.getInstance(SearchFilesTool.class);
    private static final String NAME = "search_files";
    private static final String DESCRIPTION = "Search files with a regex pattern";
    
    private final Project project;
    private final ClineFileService fileService;
    
    /**
     * Creates a new search files tool.
     *
     * @param project The project
     */
    public SearchFilesTool(Project project) {
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
        pathProperty.addProperty("description", "The path of the directory to search in");
        properties.add("path", pathProperty);
        
        JsonObject regexProperty = new JsonObject();
        regexProperty.addProperty("type", "string");
        regexProperty.addProperty("description", "The regular expression pattern to search for");
        properties.add("regex", regexProperty);
        
        JsonObject filePatternProperty = new JsonObject();
        filePatternProperty.addProperty("type", "string");
        filePatternProperty.addProperty("description", "Glob pattern to filter files (e.g., '*.ts' for TypeScript files)");
        properties.add("file_pattern", filePatternProperty);
        
        schema.add("properties", properties);
        
        JsonObject required = new JsonObject();
        required.addProperty("0", "path");
        required.addProperty("1", "regex");
        schema.add("required", required);
        
        return schema;
    }
    
    @Override
    public boolean validateArgs(@NotNull JsonObject args) {
        if (!args.has("path") || !args.get("path").isJsonPrimitive() || !args.get("path").getAsJsonPrimitive().isString()) {
            return false;
        }
        
        if (!args.has("regex") || !args.get("regex").isJsonPrimitive() || !args.get("regex").getAsJsonPrimitive().isString()) {
            return false;
        }
        
        if (args.has("file_pattern") && (!args.get("file_pattern").isJsonPrimitive() || !args.get("file_pattern").getAsJsonPrimitive().isString())) {
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
        
        if (!args.has("regex")) {
            return "Missing required parameter: regex";
        }
        if (!args.get("regex").isJsonPrimitive() || !args.get("regex").getAsJsonPrimitive().isString()) {
            return "Parameter 'regex' must be a string";
        }
        
        if (args.has("file_pattern") && (!args.get("file_pattern").isJsonPrimitive() || !args.get("file_pattern").getAsJsonPrimitive().isString())) {
            return "Parameter 'file_pattern' must be a string";
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
            String regex = args.get("regex").getAsString();
            String filePattern = args.has("file_pattern") ? args.get("file_pattern").getAsString() : "*";
            
            // Search the files
            fileService.searchFiles(path, regex, filePattern)
                    .thenAccept(results -> {
                        // Create a result object
                        JsonObject result = new JsonObject();
                        JsonArray matches = new JsonArray();
                        
                        for (Map.Entry<String, List<String>> entry : results.entrySet()) {
                            JsonObject fileMatch = new JsonObject();
                            fileMatch.addProperty("file", entry.getKey());
                            
                            JsonArray fileMatches = new JsonArray();
                            for (String match : entry.getValue()) {
                                fileMatches.add(match);
                            }
                            
                            fileMatch.add("matches", fileMatches);
                            matches.add(fileMatch);
                        }
                        
                        result.add("matches", matches);
                        result.addProperty("count", matches.size());
                        
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