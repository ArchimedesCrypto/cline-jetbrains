package com.cline.services;

import com.cline.core.model.Conversation;
import com.cline.core.model.Message;
import com.cline.core.model.MessageRole;
import com.google.gson.*;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.diagnostic.Logger;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * Service for interacting with the Cline API.
 */
@Service
public final class ClineApiService {
    private static final Logger LOG = Logger.getInstance(ClineApiService.class);
    private final OkHttpClient client;
    private final Gson gson;
    
    // This field can be injected for testing
    private ClineSettingsService settingsService;
    
    // Flag to indicate if we're in test mode
    private boolean testMode = false;

    /**
     * Interface for handling streaming API responses.
     */
    public interface StreamHandler {
        void onTextChunk(String text);
        void onToolUse(String toolName, JsonObject toolInput);
        void onUsage(int inputTokens, int outputTokens);
        void onComplete();
        void onError(Throwable error);
    }

    /**
     * Functional interface for executing tools.
     */
    public interface Function<T, R> {
        R apply(T t);
    }

    public ClineApiService() {
        this.client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build();
        this.gson = new GsonBuilder().setPrettyPrinting().create();
    }

    public static ClineApiService getInstance() {
        return ApplicationManager.getApplication().getService(ClineApiService.class);
    }
    
    /**
     * Gets the settings service, creating it if necessary.
     *
     * @return The settings service
     */
    private ClineSettingsService getSettingsService() {
        if (settingsService == null) {
            settingsService = ClineSettingsService.getInstance();
        }
        return settingsService;
    }
    
    /**
     * Set test mode for unit testing.
     *
     * @param testMode true to enable test mode, false to disable
     */
    public void setTestMode(boolean testMode) {
        this.testMode = testMode;
    }
    
    /**
     * Log an error message, but only if not in test mode.
     *
     * @param message The error message
     * @param e The exception
     */
    private void logError(String message, Throwable e) {
        if (!testMode) {
            LOG.error(message, e);
        }
    }

    /**
     * Send a message to the AI model.
     *
     * @param prompt    The user's prompt
     * @param maxTokens Maximum number of tokens to generate
     * @return A CompletableFuture containing the AI response
     */
    public CompletableFuture<String> sendMessage(String prompt, int maxTokens) {
        ClineSettingsService settings = getSettingsService();
        
        if (settings.getApiKey().isEmpty()) {
            return CompletableFuture.failedFuture(
                    new IllegalStateException("API key is not set. Please configure it in the settings.")
            );
        }

        CompletableFuture<String> future = new CompletableFuture<>();
        
        JsonObject requestBody = new JsonObject();
        requestBody.addProperty("model", settings.getModel());
        requestBody.addProperty("max_tokens", maxTokens);
        requestBody.addProperty("prompt", prompt);

        Request request = new Request.Builder()
                .url(settings.getApiEndpoint() + "/completions")
                .addHeader("Content-Type", "application/json")
                .addHeader("x-api-key", settings.getApiKey())
                .post(RequestBody.create(
                        MediaType.parse("application/json"),
                        gson.toJson(requestBody)
                ))
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                logError("API request failed", e);
                future.completeExceptionally(e);
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                try (ResponseBody responseBody = response.body()) {
                    if (!response.isSuccessful() || responseBody == null) {
                        future.completeExceptionally(
                                new IOException("Unexpected response: " + response)
                        );
                        return;
                    }

                    String responseJson = responseBody.string();
                    JsonObject jsonResponse = gson.fromJson(responseJson, JsonObject.class);
                    
                    if (jsonResponse.has("completion")) {
                        future.complete(jsonResponse.get("completion").getAsString());
                    } else {
                        future.completeExceptionally(
                                new IOException("Invalid response format: " + responseJson)
                        );
                    }
                }
            }
        });

        return future;
    }

    /**
     * Send a conversation to the AI model with streaming response.
     *
     * @param conversation The conversation
     * @param streamHandler The stream handler for receiving chunks
     */
    public void sendConversationStreaming(Conversation conversation, StreamHandler streamHandler) {
        ClineSettingsService settings = getSettingsService();
        
        if (settings.getApiKey().isEmpty()) {
            streamHandler.onError(new IllegalStateException("API key is not set. Please configure it in the settings."));
            return;
        }

        // Prepare the messages for the API request
        JsonArray messagesArray = new JsonArray();
        
        // Add system message if present
        List<Message> systemMessages = conversation.getMessagesByRole(MessageRole.SYSTEM);
        if (!systemMessages.isEmpty()) {
            JsonObject systemMessage = new JsonObject();
            systemMessage.addProperty("role", "system");
            systemMessage.addProperty("content", systemMessages.get(0).getContent());
            messagesArray.add(systemMessage);
        }
        
        // Add user and assistant messages
        for (Message message : conversation.getMessages()) {
            if (message.getRole() == MessageRole.SYSTEM) {
                continue; // Already added system message
            }
            
            JsonObject messageObj = new JsonObject();
            messageObj.addProperty("role", message.getRole().getValue());
            
            if (message.getRole() == MessageRole.TOOL) {
                // Handle tool messages
                JsonObject contentObj = new JsonObject();
                contentObj.addProperty("type", "tool_result");
                contentObj.addProperty("tool_use_id", message.getToolName());
                contentObj.addProperty("content", message.getContent());
                
                JsonArray contentArray = new JsonArray();
                contentArray.add(contentObj);
                messageObj.add("content", contentArray);
            } else {
                // Handle regular text messages
                messageObj.addProperty("content", message.getContent());
            }
            
            messagesArray.add(messageObj);
        }
        
        // Create the request body
        JsonObject requestBody = new JsonObject();
        requestBody.addProperty("model", settings.getModel());
        requestBody.addProperty("max_tokens", settings.getMaxTokens());
        requestBody.add("messages", messagesArray);
        requestBody.addProperty("stream", true);
        
        // Create the request
        Request request = new Request.Builder()
                .url(settings.getApiEndpoint() + "/v1/messages")
                .addHeader("Content-Type", "application/json")
                .addHeader("x-api-key", settings.getApiKey())
                .addHeader("anthropic-version", "2023-06-01")
                .post(RequestBody.create(
                        MediaType.parse("application/json"),
                        gson.toJson(requestBody)
                ))
                .build();
        
        // Execute the request
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                logError("API streaming request failed", e);
                streamHandler.onError(e);
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                if (!response.isSuccessful()) {
                    streamHandler.onError(new IOException("Unexpected response: " + response));
                    return;
                }
                
                ResponseBody responseBody = response.body();
                if (responseBody == null) {
                    streamHandler.onError(new IOException("Empty response body"));
                    return;
                }
                
                try {
                    // Simplified implementation - in a real implementation, we would parse the streaming response
                    String responseText = responseBody.string();
                    streamHandler.onTextChunk(responseText);
                    streamHandler.onComplete();
                } finally {
                    responseBody.close();
                }
            }
        });
    }

    /**
     * Send a conversation to the AI model.
     *
     * @param conversation The conversation
     * @return A CompletableFuture containing the AI response
     */
    public CompletableFuture<Message> sendConversation(Conversation conversation) {
        CompletableFuture<Message> future = new CompletableFuture<>();
        StringBuilder responseBuilder = new StringBuilder();
        
        sendConversationStreaming(conversation, new StreamHandler() {
            @Override
            public void onTextChunk(String text) {
                responseBuilder.append(text);
            }

            @Override
            public void onToolUse(String toolName, JsonObject toolInput) {
                // Not implemented in this simplified version
            }

            @Override
            public void onUsage(int inputTokens, int outputTokens) {
                // Not implemented in this simplified version
            }

            @Override
            public void onComplete() {
                String content = responseBuilder.toString();
                Message message = Message.createAssistantMessage(content);
                future.complete(message);
            }

            @Override
            public void onError(Throwable error) {
                future.completeExceptionally(error);
            }
        });
        
        return future;
    }

    /**
     * Execute a tool and send the result back to the AI.
     *
     * @param conversation The conversation
     * @param toolName The name of the tool
     * @param toolInput The tool input parameters
     * @param toolExecutor The tool executor function
     * @return A CompletableFuture containing the AI response
     */
    public CompletableFuture<Message> executeToolAndContinue(
            Conversation conversation,
            String toolName,
            JsonObject toolInput,
            Function<JsonObject, CompletableFuture<JsonObject>> toolExecutor) {
        
        return toolExecutor.apply(toolInput)
                .thenCompose(toolResult -> {
                    // Add the tool result to the conversation
                    conversation.addToolMessage(toolName, toolResult.toString(), toolResult);
                    
                    // Continue the conversation
                    return sendConversation(conversation);
                });
    }
}