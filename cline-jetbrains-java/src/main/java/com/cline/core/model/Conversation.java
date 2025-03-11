package com.cline.core.model;

import com.google.gson.JsonObject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

/**
 * Represents a conversation between a user and the assistant.
 */
public class Conversation {
    private final String id;
    private final List<Message> messages;
    private final Map<String, Object> metadata;
    private final Instant createdAt;
    private Instant updatedAt;
    private String title;

    /**
     * Creates a new conversation.
     *
     * @param id       The conversation ID (generated if null)
     * @param title    The conversation title (optional)
     * @param messages The initial messages (optional)
     * @param metadata Additional metadata (optional)
     */
    public Conversation(@Nullable String id,
                       @Nullable String title,
                       @Nullable List<Message> messages,
                       @Nullable Map<String, Object> metadata) {
        this.id = id != null ? id : UUID.randomUUID().toString();
        this.title = title;
        this.messages = new CopyOnWriteArrayList<>(messages != null ? messages : Collections.emptyList());
        this.metadata = new HashMap<>(metadata != null ? metadata : Collections.emptyMap());
        this.createdAt = Instant.now();
        this.updatedAt = this.createdAt;
    }

    /**
     * Creates a new empty conversation.
     *
     * @return A new empty conversation
     */
    public static Conversation createEmpty() {
        return new Conversation(null, null, null, null);
    }

    /**
     * Creates a new conversation with a system message.
     *
     * @param systemMessage The system message content
     * @return A new conversation with a system message
     */
    public static Conversation createWithSystemMessage(String systemMessage) {
        List<Message> messages = new ArrayList<>();
        messages.add(Message.createSystemMessage(systemMessage));
        return new Conversation(null, null, messages, null);
    }

    /**
     * Gets the conversation ID.
     *
     * @return The conversation ID
     */
    public String getId() {
        return id;
    }

    /**
     * Gets the conversation title.
     *
     * @return The conversation title, or null if not set
     */
    @Nullable
    public String getTitle() {
        return title;
    }

    /**
     * Sets the conversation title.
     *
     * @param title The new title
     */
    public void setTitle(String title) {
        this.title = title;
        this.updatedAt = Instant.now();
    }

    /**
     * Gets the conversation messages.
     *
     * @return An unmodifiable list of messages
     */
    public List<Message> getMessages() {
        return Collections.unmodifiableList(messages);
    }

    /**
     * Gets the conversation metadata.
     *
     * @return The conversation metadata
     */
    public Map<String, Object> getMetadata() {
        return Collections.unmodifiableMap(metadata);
    }

    /**
     * Gets the creation timestamp.
     *
     * @return The creation timestamp
     */
    public Instant getCreatedAt() {
        return createdAt;
    }

    /**
     * Gets the last update timestamp.
     *
     * @return The last update timestamp
     */
    public Instant getUpdatedAt() {
        return updatedAt;
    }

    /**
     * Adds a message to the conversation.
     *
     * @param message The message to add
     */
    public void addMessage(@NotNull Message message) {
        messages.add(message);
        updatedAt = Instant.now();
    }

    /**
     * Adds a user message to the conversation.
     *
     * @param content The message content
     * @return The added message
     */
    public Message addUserMessage(String content) {
        Message message = Message.createUserMessage(content);
        addMessage(message);
        return message;
    }

    /**
     * Adds an assistant message to the conversation.
     *
     * @param content The message content
     * @return The added message
     */
    public Message addAssistantMessage(String content) {
        Message message = Message.createAssistantMessage(content);
        addMessage(message);
        return message;
    }

    /**
     * Adds a system message to the conversation.
     *
     * @param content The message content
     * @return The added message
     */
    public Message addSystemMessage(String content) {
        Message message = Message.createSystemMessage(content);
        addMessage(message);
        return message;
    }

    /**
     * Adds a tool message to the conversation.
     *
     * @param toolName   The name of the tool
     * @param content    The message content
     * @param toolResult The result of the tool execution
     * @return The added message
     */
    public Message addToolMessage(String toolName, String content, JsonObject toolResult) {
        Message message = Message.createToolMessage(toolName, content, toolResult);
        addMessage(message);
        return message;
    }

    /**
     * Gets the last message in the conversation.
     *
     * @return The last message, or null if the conversation is empty
     */
    @Nullable
    public Message getLastMessage() {
        return messages.isEmpty() ? null : messages.get(messages.size() - 1);
    }

    /**
     * Gets the messages with the specified role.
     *
     * @param role The role to filter by
     * @return A list of messages with the specified role
     */
    public List<Message> getMessagesByRole(MessageRole role) {
        return messages.stream()
                .filter(message -> message.getRole() == role)
                .collect(Collectors.toList());
    }

    /**
     * Gets the number of messages in the conversation.
     *
     * @return The number of messages
     */
    public int getMessageCount() {
        return messages.size();
    }

    /**
     * Checks if the conversation is empty.
     *
     * @return True if the conversation has no messages, false otherwise
     */
    public boolean isEmpty() {
        return messages.isEmpty();
    }

    /**
     * Sets a metadata value.
     *
     * @param key   The metadata key
     * @param value The metadata value
     */
    public void setMetadata(String key, Object value) {
        metadata.put(key, value);
        updatedAt = Instant.now();
    }

    /**
     * Gets a metadata value.
     *
     * @param key The metadata key
     * @return The metadata value, or null if not found
     */
    @Nullable
    public Object getMetadata(String key) {
        return metadata.get(key);
    }

    /**
     * Removes a metadata value.
     *
     * @param key The metadata key
     * @return The removed value, or null if not found
     */
    @Nullable
    public Object removeMetadata(String key) {
        Object value = metadata.remove(key);
        if (value != null) {
            updatedAt = Instant.now();
        }
        return value;
    }

    /**
     * Clears all messages from the conversation.
     */
    public void clearMessages() {
        messages.clear();
        updatedAt = Instant.now();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Conversation that = (Conversation) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Conversation{" +
                "id='" + id + '\'' +
                ", title='" + title + '\'' +
                ", messageCount=" + messages.size() +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}