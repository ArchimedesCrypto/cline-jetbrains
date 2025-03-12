package com.cline.core.model;

import com.google.gson.JsonObject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Represents a message in a conversation.
 */
public class Message {
    private final String id;
    private String content;
    private final MessageRole role;
    private final Instant timestamp;
    private JsonObject metadata;
    private String toolName;
    private JsonObject toolInput;
    private JsonObject toolResult;

    /**
     * Creates a new message.
     *
     * @param id         The message ID (generated if null)
     * @param content    The message content
     * @param role       The message role
     * @param timestamp  The message timestamp (current time if null)
     * @param metadata   Additional metadata (optional)
     * @param toolName   The name of the tool if this is a tool message (optional)
     * @param toolResult The result of the tool execution if this is a tool message (optional)
     */
    public Message(@Nullable String id,
                   @NotNull String content,
                   @NotNull MessageRole role,
                   @Nullable Instant timestamp,
                   @Nullable JsonObject metadata,
                   @Nullable String toolName,
                   @Nullable JsonObject toolResult) {
        this.id = id != null ? id : UUID.randomUUID().toString();
        this.content = content;
        this.role = role;
        this.timestamp = timestamp != null ? timestamp : Instant.now();
        this.metadata = metadata != null ? metadata : new JsonObject();
        this.toolName = toolName;
        this.toolResult = toolResult;
    }

    /**
     * Creates a new user message.
     *
     * @param content The message content
     * @return A new user message
     */
    public static Message createUserMessage(String content) {
        return new Message(null, content, MessageRole.USER, null, null, null, null);
    }

    /**
     * Creates a new assistant message.
     *
     * @param content The message content
     * @return A new assistant message
     */
    public static Message createAssistantMessage(String content) {
        return new Message(null, content, MessageRole.ASSISTANT, null, null, null, null);
    }

    /**
     * Creates a new system message.
     *
     * @param content The message content
     * @return A new system message
     */
    public static Message createSystemMessage(String content) {
        return new Message(null, content, MessageRole.SYSTEM, null, null, null, null);
    }

    /**
     * Creates a new tool message.
     *
     * @param toolName   The name of the tool
     * @param content    The message content
     * @param toolResult The result of the tool execution
     * @return A new tool message
     */
    public static Message createToolMessage(String toolName, String content, JsonObject toolResult) {
        return new Message(null, content, MessageRole.TOOL, null, null, toolName, toolResult);
    }

    /**
     * Gets the message ID.
     *
     * @return The message ID
     */
    public String getId() {
        return id;
    }

    /**
     * Gets the message content.
     *
     * @return The message content
     */
    public String getContent() {
        return content;
    }

    /**
     * Sets the message content.
     *
     * @param content The new content
     */
    public void setContent(String content) {
        this.content = content;
    }

    /**
     * Gets the message role.
     *
     * @return The message role
     */
    public MessageRole getRole() {
        return role;
    }

    /**
     * Gets the message timestamp.
     *
     * @return The message timestamp
     */
    public Instant getTimestamp() {
        return timestamp;
    }

    /**
     * Gets the message metadata.
     *
     * @return The message metadata
     */
    public JsonObject getMetadata() {
        return metadata;
    }

    /**
     * Sets the message metadata.
     *
     * @param metadata The new metadata
     */
    public void setMetadata(JsonObject metadata) {
        this.metadata = metadata != null ? metadata : new JsonObject();
    }

    /**
     * Gets the name of the tool if this is a tool message.
     *
     * @return The name of the tool, or null if this is not a tool message
     */
    @Nullable
    public String getToolName() {
        return toolName;
    }

    /**
     * Sets the name of the tool.
     *
     * @param toolName The name of the tool
     */
    public void setToolName(String toolName) {
        this.toolName = toolName;
    }

    /**
     * Gets the tool input if this is a tool use message.
     *
     * @return The tool input, or null if this is not a tool use message
     */
    @Nullable
    public JsonObject getToolInput() {
        return toolInput;
    }

    /**
     * Sets the tool input.
     *
     * @param toolInput The tool input
     */
    public void setToolInput(JsonObject toolInput) {
        this.toolInput = toolInput;
    }

    /**
     * Gets the result of the tool execution if this is a tool message.
     *
     * @return The result of the tool execution, or null if this is not a tool message
     */
    @Nullable
    public JsonObject getToolResult() {
        return toolResult;
    }

    /**
     * Sets the tool result.
     *
     * @param toolResult The tool result
     */
    public void setToolResult(JsonObject toolResult) {
        this.toolResult = toolResult;
    }

    /**
     * Checks if this message is a tool message.
     *
     * @return True if this is a tool message, false otherwise
     */
    public boolean isToolMessage() {
        return role == MessageRole.TOOL && toolName != null;
    }
    
    /**
     * Checks if this message is a tool use message.
     * A tool use message is an assistant message that requests to use a tool.
     *
     * @return True if this is a tool use message, false otherwise
     */
    public boolean isToolUse() {
        if (role != MessageRole.ASSISTANT) return false;
        
        // Check if the content contains a tool use request
        // This is a simplified check; in a real implementation, we would parse the content
        // to determine if it's a tool use request
        return content != null && content.contains("tool");
    }
    
    /**
     * Checks if this message is a command message.
     * A command message is an assistant message that requests to execute a command.
     *
     * @return True if this is a command message, false otherwise
     */
    public boolean isCommand() {
        if (role != MessageRole.ASSISTANT) return false;
        
        // Check if the content contains a command request
        // This is a simplified check; in a real implementation, we would parse the content
        // to determine if it's a command request
        return content != null && content.contains("command");
    }
    
    /**
     * Checks if this message is an error message.
     *
     * @return True if this is an error message, false otherwise
     */
    public boolean isError() {
        // Check if the metadata contains an error flag
        // This is a simplified check; in a real implementation, we would check for specific error flags
        return metadata != null && metadata.has("error");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Message message = (Message) o;
        return Objects.equals(id, message.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Message{" +
                "id='" + id + '\'' +
                ", role=" + role +
                ", timestamp=" + timestamp +
                ", content='" + (content.length() > 50 ? content.substring(0, 47) + "..." : content) + '\'' +
                '}';
    }
}