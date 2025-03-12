package com.cline.core.tool.impl;

import com.cline.core.tool.AbstractTool;
import com.cline.core.tool.ToolResult;
import com.google.gson.JsonObject;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

/**
 * Tool for asking the user a follow-up question.
 */
public class AskFollowupQuestionTool extends AbstractTool {
    private static final Logger LOG = Logger.getInstance(AskFollowupQuestionTool.class);
    private static final String NAME = "ask_followup_question";
    private static final String DESCRIPTION = "Ask the user a follow-up question";
    
    private final Project project;
    
    /**
     * Creates a new ask followup question tool.
     *
     * @param project The project
     */
    public AskFollowupQuestionTool(Project project) {
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
        
        JsonObject questionProperty = new JsonObject();
        questionProperty.addProperty("type", "string");
        questionProperty.addProperty("description", "The question to ask the user");
        properties.add("question", questionProperty);
        
        schema.add("properties", properties);
        
        JsonObject required = new JsonObject();
        required.addProperty("0", "question");
        schema.add("required", required);
        
        return schema;
    }
    
    @Override
    public boolean validateArgs(@NotNull JsonObject args) {
        if (!args.has("question") || !args.get("question").isJsonPrimitive() || !args.get("question").getAsJsonPrimitive().isString()) {
            return false;
        }
        
        return true;
    }
    
    @Override
    @NotNull
    public String getValidationErrorMessage(@NotNull JsonObject args) {
        if (!args.has("question")) {
            return "Missing required parameter: question";
        }
        if (!args.get("question").isJsonPrimitive() || !args.get("question").getAsJsonPrimitive().isString()) {
            return "Parameter 'question' must be a string";
        }
        
        return null;
    }
    
    @Override
    @NotNull
    public CompletableFuture<ToolResult> execute(@NotNull JsonObject args) {
        CompletableFuture<ToolResult> future = new CompletableFuture<>();
        
        try {
            // Get the parameters
            String question = args.get("question").getAsString();
            
            // Show a dialog to ask the question
            String answer = Messages.showInputDialog(
                    project,
                    question,
                    "Cline Follow-up Question",
                    Messages.getQuestionIcon()
            );
            
            // Create a result object
            JsonObject result = new JsonObject();
            result.addProperty("question", question);
            result.addProperty("answer", answer != null ? answer : "");
            
            // Complete the future with the result
            completeSuccessfully(future, result);
        } catch (Exception e) {
            // Complete the future with the error
            completeExceptionally(future, e);
        }
        
        return future;
    }
}