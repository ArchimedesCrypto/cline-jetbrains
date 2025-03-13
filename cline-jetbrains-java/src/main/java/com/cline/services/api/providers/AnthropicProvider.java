package com.cline.services.api.providers;

import com.cline.core.model.Conversation;
import com.cline.core.model.Message;
import com.cline.core.model.MessageRole;
import com.cline.services.api.ApiProvider;
import com.cline.services.api.ModelInfo;
import com.google.gson.*;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * Anthropic API provider implementation.
 */
public class AnthropicProvider implements ApiProvider {
    private static final MediaType JSON = MediaType.parse("application/json");
    private final OkHttpClient client;
    private final Gson gson;
    private final String apiKey;
    private final String apiEndpoint;
    private final String modelId;
    private final int maxTokens;
    private final boolean testMode;
    private final boolean enablePromptCaching;

    public AnthropicProvider(String apiKey, String apiEndpoint, String modelId, int maxTokens, boolean testMode, boolean enablePromptCaching) {
        this.apiKey = apiKey;
        this.apiEndpoint = apiEndpoint;
        this.modelId = modelId;
        this.maxTokens = maxTokens;
        this.testMode = testMode;
        this.enablePromptCaching = enablePromptCaching;
        this.client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build();
        this.gson = new GsonBuilder().setPrettyPrinting().create();
    }

    @Override
    public CompletableFuture<String> sendMessage(String prompt, int maxTokens) {
        CompletableFuture<String> future = new CompletableFuture<>();
        
        if (apiKey.isEmpty()) {
            return CompletableFuture.failedFuture(
                    new IllegalStateException("API key is not set. Please configure it in the settings.")
            );
        }
        
        JsonObject requestBody = new JsonObject();
        requestBody.addProperty("model", modelId);
        requestBody.addProperty("max_tokens", maxTokens);
        requestBody.addProperty("prompt", prompt);

        Request request = new Request.Builder()
                .url(apiEndpoint + "/completions")
                .addHeader("Content-Type", "application/json")
                .addHeader("x-api-key", apiKey)
                .post(RequestBody.create(
                        JSON,
                        gson.toJson(requestBody)
                ))
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                if (!testMode) {
                    // Log error
                }
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

    @Override
    public void sendConversationStreaming(Conversation conversation, StreamHandler streamHandler) {
        if (apiKey.isEmpty()) {
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
            
            if (enablePromptCaching && supportsPromptCaching(modelId)) {
                // Add cache control for system message
                JsonArray contentArray = new JsonArray();
                JsonObject textContent = new JsonObject();
                textContent.addProperty("type", "text");
                textContent.addProperty("text", systemMessages.get(0).getContent());
                
                JsonObject cacheControl = new JsonObject();
                cacheControl.addProperty("type", "ephemeral");
                textContent.add("cache_control", cacheControl);
                
                contentArray.add(textContent);
                systemMessage.add("content", contentArray);
            } else {
                systemMessage.addProperty("content", systemMessages.get(0).getContent());
            }
            
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
            } else if (message.getRole() == MessageRole.USER && enablePromptCaching && supportsPromptCaching(modelId)) {
                // Add cache control for user messages if prompt caching is enabled
                JsonArray contentArray = new JsonArray();
                JsonObject textContent = new JsonObject();
                textContent.addProperty("type", "text");
                textContent.addProperty("text", message.getContent());
                
                JsonObject cacheControl = new JsonObject();
                cacheControl.addProperty("type", "ephemeral");
                textContent.add("cache_control", cacheControl);
                
                contentArray.add(textContent);
                messageObj.add("content", contentArray);
            } else {
                // Handle regular text messages
                messageObj.addProperty("content", message.getContent());
            }
            
            messagesArray.add(messageObj);
        }
        
        // Create the request body
        JsonObject requestBody = new JsonObject();
        requestBody.addProperty("model", modelId);
        requestBody.addProperty("max_tokens", maxTokens);
        requestBody.add("messages", messagesArray);
        requestBody.addProperty("stream", true);
        
        // Create the request
        Request.Builder requestBuilder = new Request.Builder()
                .url(apiEndpoint + "/v1/messages")
                .addHeader("Content-Type", "application/json")
                .addHeader("x-api-key", apiKey)
                .addHeader("anthropic-version", "2023-06-01");
        
        // Add prompt caching header if needed
        if (enablePromptCaching && supportsPromptCaching(modelId)) {
            requestBuilder.addHeader("anthropic-beta", "prompt-caching-2024-07-31");
        }
        
        Request request = requestBuilder
                .post(RequestBody.create(
                        JSON,
                        gson.toJson(requestBody)
                ))
                .build();
        
        // Execute the request
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                if (!testMode) {
                    // Log error
                }
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
                    // In a real implementation, we would parse the streaming response line by line
                    // This is a simplified implementation for now
                    String responseText = responseBody.string();
                    streamHandler.onTextChunk(responseText);
                    streamHandler.onComplete();
                } finally {
                    responseBody.close();
                }
            }
        });
    }

    @Override
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

    @Override
    public ModelInfo getModel() {
        // Return model information for the current model
        return new ModelInfo(
            modelId,
            getModelName(modelId),
            maxTokens,
            supportsPromptCaching(modelId)
        );
    }

    private String getModelName(String modelId) {
        switch (modelId) {
            case "claude-3-opus-20240229":
                return "Claude 3 Opus";
            case "claude-3-sonnet-20240229":
                return "Claude 3 Sonnet";
            case "claude-3-haiku-20240307":
                return "Claude 3 Haiku";
            case "claude-3-5-sonnet-20241022":
                return "Claude 3.5 Sonnet";
            case "claude-3-5-haiku-20241022":
                return "Claude 3.5 Haiku";
            default:
                return modelId;
        }
    }

    private boolean supportsPromptCaching(String modelId) {
        return modelId.equals("claude-3-5-sonnet-20241022") ||
               modelId.equals("claude-3-5-haiku-20241022") ||
               modelId.equals("claude-3-opus-20240229") ||
               modelId.equals("claude-3-haiku-20240307");
    }
}