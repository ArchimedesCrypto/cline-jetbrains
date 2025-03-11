package com.cline.core.tool;

import com.google.gson.JsonObject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

/**
 * Interface for tools that can be executed by the assistant.
 */
public interface Tool {
    /**
     * Gets the name of the tool.
     *
     * @return The tool name
     */
    @NotNull
    String getName();

    /**
     * Gets the description of the tool.
     *
     * @return The tool description
     */
    @NotNull
    String getDescription();

    /**
     * Gets the input schema for the tool.
     *
     * @return The input schema as a JSON object
     */
    @NotNull
    JsonObject getInputSchema();

    /**
     * Executes the tool with the given arguments.
     *
     * @param args The arguments for the tool
     * @return A future that completes with the tool result
     */
    @NotNull
    CompletableFuture<ToolResult> execute(@NotNull JsonObject args);

    /**
     * Validates the arguments against the input schema.
     *
     * @param args The arguments to validate
     * @return True if the arguments are valid, false otherwise
     */
    boolean validateArgs(@NotNull JsonObject args);

    /**
     * Gets the error message for invalid arguments.
     *
     * @param args The invalid arguments
     * @return The error message
     */
    @Nullable
    String getValidationErrorMessage(@NotNull JsonObject args);
}