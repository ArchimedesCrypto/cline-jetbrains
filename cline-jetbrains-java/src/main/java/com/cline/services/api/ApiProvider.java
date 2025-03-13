package com.cline.services.api;

import com.cline.core.model.Conversation;
import com.cline.core.model.Message;
import com.google.gson.JsonObject;

import java.util.concurrent.CompletableFuture;

/**
 * Interface for API providers.
 */
public interface ApiProvider {
    /**
     * Interface for handling streaming API responses.
     */
    interface StreamHandler {
        void onTextChunk(String text);
        void onToolUse(String toolName, JsonObject toolInput);
        void onUsage(int inputTokens, int outputTokens);
        void onComplete();
        void onError(Throwable error);
    }

    /**
     * Send a message to the AI model.
     *
     * @param prompt    The user's prompt
     * @param maxTokens Maximum number of tokens to generate
     * @return A CompletableFuture containing the AI response
     */
    CompletableFuture<String> sendMessage(String prompt, int maxTokens);

    /**
     * Send a conversation to the AI model with streaming response.
     *
     * @param conversation The conversation
     * @param streamHandler The stream handler for receiving chunks
     */
    void sendConversationStreaming(Conversation conversation, StreamHandler streamHandler);

    /**
     * Send a conversation to the AI model.
     *
     * @param conversation The conversation
     * @return A CompletableFuture containing the AI response
     */
    CompletableFuture<Message> sendConversation(Conversation conversation);

    /**
     * Get the model information.
     *
     * @return The model ID and information
     */
    ModelInfo getModel();
}