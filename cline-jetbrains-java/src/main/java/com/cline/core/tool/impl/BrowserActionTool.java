package com.cline.core.tool.impl;

import com.cline.core.tool.AbstractTool;
import com.cline.core.tool.ToolResult;
import com.cline.services.ClineBrowserService;
import com.google.gson.JsonObject;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

/**
 * Tool for interacting with a browser.
 */
public class BrowserActionTool extends AbstractTool {
    private static final Logger LOG = Logger.getInstance(BrowserActionTool.class);
    private static final String NAME = "browser_action";
    private static final String DESCRIPTION = "Interact with a browser";
    
    private final Project project;
    private final ClineBrowserService browserService;
    
    /**
     * Creates a new browser action tool.
     *
     * @param project The project
     */
    public BrowserActionTool(Project project) {
        super(NAME, DESCRIPTION, createInputSchema());
        this.project = project;
        this.browserService = ClineBrowserService.getInstance(project);
    }
    
    /**
     * Creates the input schema for the tool.
     *
     * @return The input schema
     */
    private static JsonObject createInputSchema() {
        JsonObject schema = new JsonObject();
        
        JsonObject properties = new JsonObject();
        
        JsonObject actionProperty = new JsonObject();
        actionProperty.addProperty("type", "string");
        actionProperty.addProperty("description", "The action to perform (launch, click, type, scroll_down, scroll_up, close)");
        properties.add("action", actionProperty);
        
        JsonObject urlProperty = new JsonObject();
        urlProperty.addProperty("type", "string");
        urlProperty.addProperty("description", "The URL to launch the browser at (for 'launch' action)");
        properties.add("url", urlProperty);
        
        JsonObject coordinateProperty = new JsonObject();
        coordinateProperty.addProperty("type", "string");
        coordinateProperty.addProperty("description", "The x,y coordinates for the 'click' action");
        properties.add("coordinate", coordinateProperty);
        
        JsonObject textProperty = new JsonObject();
        textProperty.addProperty("type", "string");
        textProperty.addProperty("description", "The text to type (for 'type' action)");
        properties.add("text", textProperty);
        
        schema.add("properties", properties);
        
        JsonObject required = new JsonObject();
        required.addProperty("0", "action");
        schema.add("required", required);
        
        return schema;
    }
    
    @Override
    public boolean validateArgs(@NotNull JsonObject args) {
        if (!args.has("action") || !args.get("action").isJsonPrimitive() || !args.get("action").getAsJsonPrimitive().isString()) {
            return false;
        }
        
        String action = args.get("action").getAsString();
        
        if (action.equals("launch") && (!args.has("url") || !args.get("url").isJsonPrimitive() || !args.get("url").getAsJsonPrimitive().isString())) {
            return false;
        }
        
        if (action.equals("click") && (!args.has("coordinate") || !args.get("coordinate").isJsonPrimitive() || !args.get("coordinate").getAsJsonPrimitive().isString())) {
            return false;
        }
        
        if (action.equals("type") && (!args.has("text") || !args.get("text").isJsonPrimitive() || !args.get("text").getAsJsonPrimitive().isString())) {
            return false;
        }
        
        return true;
    }
    
    @Override
    @NotNull
    public String getValidationErrorMessage(@NotNull JsonObject args) {
        if (!args.has("action")) {
            return "Missing required parameter: action";
        }
        if (!args.get("action").isJsonPrimitive() || !args.get("action").getAsJsonPrimitive().isString()) {
            return "Parameter 'action' must be a string";
        }
        
        String action = args.get("action").getAsString();
        
        if (action.equals("launch")) {
            if (!args.has("url")) {
                return "Missing required parameter for 'launch' action: url";
            }
            if (!args.get("url").isJsonPrimitive() || !args.get("url").getAsJsonPrimitive().isString()) {
                return "Parameter 'url' must be a string";
            }
        }
        
        if (action.equals("click")) {
            if (!args.has("coordinate")) {
                return "Missing required parameter for 'click' action: coordinate";
            }
            if (!args.get("coordinate").isJsonPrimitive() || !args.get("coordinate").getAsJsonPrimitive().isString()) {
                return "Parameter 'coordinate' must be a string";
            }
        }
        
        if (action.equals("type")) {
            if (!args.has("text")) {
                return "Missing required parameter for 'type' action: text";
            }
            if (!args.get("text").isJsonPrimitive() || !args.get("text").getAsJsonPrimitive().isString()) {
                return "Parameter 'text' must be a string";
            }
        }
        
        return null;
    }
    
    @Override
    @NotNull
    public CompletableFuture<ToolResult> execute(@NotNull JsonObject args) {
        CompletableFuture<ToolResult> future = new CompletableFuture<>();
        
        try {
            // Get the action
            String action = args.get("action").getAsString();
            
            // Execute the action
            switch (action) {
                case "launch":
                    String url = args.get("url").getAsString();
                    browserService.launchBrowser(url)
                            .thenAccept(screenshot -> {
                                // Create a result object
                                JsonObject result = new JsonObject();
                                result.addProperty("action", action);
                                result.addProperty("url", url);
                                result.addProperty("screenshot", screenshot.getScreenshot());
                                result.addProperty("success", true);
                                
                                // Complete the future with the result
                                completeSuccessfully(future, result);
                            })
                            .exceptionally(e -> {
                                completeExceptionally(future, e);
                                return null;
                            });
                    break;
                    
                case "click":
                    String coordinate = args.get("coordinate").getAsString();
                    browserService.clickAt(coordinate)
                            .thenAccept(screenshot -> {
                                // Create a result object
                                JsonObject result = new JsonObject();
                                result.addProperty("action", action);
                                result.addProperty("coordinate", coordinate);
                                result.addProperty("screenshot", screenshot.getScreenshot());
                                result.addProperty("success", true);
                                
                                // Complete the future with the result
                                completeSuccessfully(future, result);
                            })
                            .exceptionally(e -> {
                                completeExceptionally(future, e);
                                return null;
                            });
                    break;
                    
                case "type":
                    String text = args.get("text").getAsString();
                    browserService.typeText(text)
                            .thenAccept(screenshot -> {
                                // Create a result object
                                JsonObject result = new JsonObject();
                                result.addProperty("action", action);
                                result.addProperty("text", text);
                                result.addProperty("screenshot", screenshot.getScreenshot());
                                result.addProperty("success", true);
                                
                                // Complete the future with the result
                                completeSuccessfully(future, result);
                            })
                            .exceptionally(e -> {
                                completeExceptionally(future, e);
                                return null;
                            });
                    break;
                    
                case "scroll_down":
                    browserService.scrollDown()
                            .thenAccept(screenshot -> {
                                // Create a result object
                                JsonObject result = new JsonObject();
                                result.addProperty("action", action);
                                result.addProperty("screenshot", screenshot.getScreenshot());
                                result.addProperty("success", true);
                                
                                // Complete the future with the result
                                completeSuccessfully(future, result);
                            })
                            .exceptionally(e -> {
                                completeExceptionally(future, e);
                                return null;
                            });
                    break;
                    
                case "scroll_up":
                    browserService.scrollUp()
                            .thenAccept(screenshot -> {
                                // Create a result object
                                JsonObject result = new JsonObject();
                                result.addProperty("action", action);
                                result.addProperty("screenshot", screenshot.getScreenshot());
                                result.addProperty("success", true);
                                
                                // Complete the future with the result
                                completeSuccessfully(future, result);
                            })
                            .exceptionally(e -> {
                                completeExceptionally(future, e);
                                return null;
                            });
                    break;
                    
                case "close":
                    browserService.closeBrowser()
                            .thenAccept(success -> {
                                // Create a result object
                                JsonObject result = new JsonObject();
                                result.addProperty("action", action);
                                result.addProperty("success", success);
                                
                                // Complete the future with the result
                                completeSuccessfully(future, result);
                            })
                            .exceptionally(e -> {
                                completeExceptionally(future, e);
                                return null;
                            });
                    break;
                    
                default:
                    completeExceptionally(future, "Unknown action: " + action);
                    break;
            }
        } catch (Exception e) {
            // Complete the future with the error
            completeExceptionally(future, e);
        }
        
        return future;
    }
}