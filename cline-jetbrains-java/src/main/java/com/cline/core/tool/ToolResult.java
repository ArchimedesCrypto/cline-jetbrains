package com.cline.core.tool;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Represents the result of a tool execution.
 */
public class ToolResult {
    private final boolean success;
    private final JsonElement content;
    private final String errorMessage;

    /**
     * Creates a successful tool result.
     *
     * @param content The result content
     */
    public ToolResult(@NotNull JsonElement content) {
        this.success = true;
        this.content = content;
        this.errorMessage = null;
    }

    /**
     * Creates a failed tool result.
     *
     * @param errorMessage The error message
     */
    public ToolResult(@NotNull String errorMessage) {
        this.success = false;
        this.content = null;
        this.errorMessage = errorMessage;
    }

    /**
     * Creates a tool result.
     *
     * @param success      Whether the tool execution was successful
     * @param content      The result content (if successful)
     * @param errorMessage The error message (if failed)
     */
    public ToolResult(boolean success, @Nullable JsonElement content, @Nullable String errorMessage) {
        this.success = success;
        this.content = content;
        this.errorMessage = errorMessage;
    }

    /**
     * Creates a successful tool result with a string content.
     *
     * @param content The string content
     * @return A successful tool result
     */
    public static ToolResult success(@NotNull String content) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("text", content);
        return new ToolResult(jsonObject);
    }

    /**
     * Creates a successful tool result with a JSON content.
     *
     * @param content The JSON content
     * @return A successful tool result
     */
    public static ToolResult success(@NotNull JsonElement content) {
        return new ToolResult(content);
    }

    /**
     * Creates a failed tool result.
     *
     * @param errorMessage The error message
     * @return A failed tool result
     */
    public static ToolResult failure(@NotNull String errorMessage) {
        return new ToolResult(errorMessage);
    }

    /**
     * Checks if the tool execution was successful.
     *
     * @return True if successful, false otherwise
     */
    public boolean isSuccess() {
        return success;
    }

    /**
     * Gets the result content.
     *
     * @return The result content, or null if the execution failed
     */
    @Nullable
    public JsonElement getContent() {
        return content;
    }

    /**
     * Gets the error message.
     *
     * @return The error message, or null if the execution was successful
     */
    @Nullable
    public String getErrorMessage() {
        return errorMessage;
    }

    /**
     * Converts the result to a JSON object.
     *
     * @return A JSON object representing the result
     */
    public JsonObject toJson() {
        JsonObject json = new JsonObject();
        json.addProperty("success", success);
        
        if (success && content != null) {
            json.add("content", content);
        } else if (!success && errorMessage != null) {
            json.addProperty("error", errorMessage);
        }
        
        return json;
    }

    @Override
    public String toString() {
        return "ToolResult{" +
                "success=" + success +
                ", content=" + (content != null ? content : "null") +
                ", errorMessage='" + errorMessage + '\'' +
                '}';
    }
}