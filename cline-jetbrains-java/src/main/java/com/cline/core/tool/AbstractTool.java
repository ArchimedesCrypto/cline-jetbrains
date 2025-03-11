package com.cline.core.tool;

import com.google.gson.JsonObject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

/**
 * Abstract base class for tools that provides common functionality.
 */
public abstract class AbstractTool implements Tool {
    private final String name;
    private final String description;
    private final JsonObject inputSchema;

    /**
     * Creates a new abstract tool.
     *
     * @param name        The tool name
     * @param description The tool description
     * @param inputSchema The input schema
     */
    protected AbstractTool(@NotNull String name, @NotNull String description, @NotNull JsonObject inputSchema) {
        this.name = name;
        this.description = description;
        this.inputSchema = inputSchema;
    }

    @Override
    @NotNull
    public String getName() {
        return name;
    }

    @Override
    @NotNull
    public String getDescription() {
        return description;
    }

    @Override
    @NotNull
    public JsonObject getInputSchema() {
        return inputSchema;
    }

    @Override
    public boolean validateArgs(@NotNull JsonObject args) {
        // Basic validation - can be overridden by subclasses for more specific validation
        return true;
    }

    @Override
    @Nullable
    public String getValidationErrorMessage(@NotNull JsonObject args) {
        // Default implementation - can be overridden by subclasses
        return null;
    }

    /**
     * Helper method to create a successful tool result with a string content.
     *
     * @param content The string content
     * @return A successful tool result
     */
    protected ToolResult successResult(@NotNull String content) {
        return ToolResult.success(content);
    }

    /**
     * Helper method to create a successful tool result with a JSON content.
     *
     * @param content The JSON content
     * @return A successful tool result
     */
    protected ToolResult successResult(@NotNull JsonObject content) {
        return ToolResult.success(content);
    }

    /**
     * Helper method to create a failed tool result.
     *
     * @param errorMessage The error message
     * @return A failed tool result
     */
    protected ToolResult failureResult(@NotNull String errorMessage) {
        return ToolResult.failure(errorMessage);
    }

    /**
     * Helper method to complete a future with a successful tool result.
     *
     * @param future  The future to complete
     * @param content The string content
     */
    protected void completeSuccessfully(@NotNull CompletableFuture<ToolResult> future, @NotNull String content) {
        future.complete(successResult(content));
    }

    /**
     * Helper method to complete a future with a successful tool result.
     *
     * @param future  The future to complete
     * @param content The JSON content
     */
    protected void completeSuccessfully(@NotNull CompletableFuture<ToolResult> future, @NotNull JsonObject content) {
        future.complete(successResult(content));
    }

    /**
     * Helper method to complete a future with a failed tool result.
     *
     * @param future       The future to complete
     * @param errorMessage The error message
     */
    protected void completeExceptionally(@NotNull CompletableFuture<ToolResult> future, @NotNull String errorMessage) {
        future.complete(failureResult(errorMessage));
    }

    /**
     * Helper method to complete a future with a failed tool result from an exception.
     *
     * @param future    The future to complete
     * @param exception The exception
     */
    protected void completeExceptionally(@NotNull CompletableFuture<ToolResult> future, @NotNull Throwable exception) {
        future.complete(failureResult(exception.getMessage() != null ? exception.getMessage() : exception.toString()));
    }
}