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
 * OpenAI API provider implementation.
 */
public class OpenAiProvider implements ApiProvider {
    private static final MediaType JSON = MediaType.parse("application/json");
    private final OkHttpClient client;
    private final Gson gson;
    private final String apiKey;
    private final String apiEndpoint;
    private final String modelId;
    private final int maxTokens;
    private final boolean testMode;
    private final boolean isAzure;
    private final String azureApiVersion;

    public OpenAiProvider(String apiKey, String apiEndpoint, String modelId, int maxTokens, boolean testMode, boolean isAzure, String azureApiVersion) {
        this.apiKey = apiKey;
        this.apiEndpoint = apiEndpoint;
        this.modelId = modelId;
        this.maxTokens = maxTokens;
        this.testMode = testMode;
        this.isAzure = isAzure;
        this.azureApiVersion = azureApiVersion;
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
        
        JsonArray messagesArray = new JsonArray();
        JsonObject userMessage = new JsonObject();
        userMessage.addProperty("role", "user");
        userMessage.addProperty("content", prompt);
        messagesArray.add(userMessage);
        
        requestBody.add("messages", messagesArray);
        
        String url = isAzure 
            ? apiEndpoint + "/openai/deployments/" + modelId + "/chat/completions?api-version=" + azureApiVersion
            : apiEndpoint + "/chat/completions";
            
        Request.Builder requestBuilder = new Request.Builder()
                .url(url)
                .addHeader("Content-Type", "application/json");
                
        if (isAzure) {
            requestBuilder.addHeader("api-key", apiKey);
        } else {
            requestBuilder.addHeader("Authorization", "Bearer " + apiKey);
        }
        
        Request request = requestBuilder
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
                    
                    if (jsonResponse.has("choices") && jsonResponse.getAsJsonArray("choices").size() > 0) {
                        JsonObject choice = jsonResponse.getAsJsonArray("choices").get(0).getAsJsonObject();
                        if (choice.has("message") && choice.getAsJsonObject("message").has("content")) {
                            future.complete(choice.getAsJsonObject("message").get("content").getAsString());
                        } else {
                            future.completeExceptionally(
                                    new IOException("Invalid response format: " + responseJson)
                            );
                        }
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
            systemMessage.addProperty("content", systemMessages.get(0).getContent());
            messagesArray.add(systemMessage);
        }
        
        // Add user and assistant messages
        for (Message message : conversation.getMessages()) {
            if (message.getRole() == MessageRole.SYSTEM) {
                continue; // Already added system message
            }
            
            JsonObject messageObj = new JsonObject();
            
            // Map Cline message roles to OpenAI roles
            String role;
            switch (message.getRole()) {
                case USER:
                    role = "user";
                    break;
                case ASSISTANT:
                    role = "assistant";
                    break;
                case TOOL:
                    role = "tool";
                    break;
                default:
                    role = "user";
            }
            
            messageObj.addProperty("role", role);
            
            if (message.getRole() == MessageRole.TOOL) {
                // Handle tool messages
                JsonObject contentObj = new JsonObject();
                contentObj.addProperty("tool_call_id", message.getToolName());
                contentObj.addProperty("content", message.getContent());
                messageObj.add("content", contentObj);
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
        String url = isAzure 
            ? apiEndpoint + "/openai/deployments/" + modelId + "/chat/completions?api-version=" + azureApiVersion
            : apiEndpoint + "/chat/completions";
            
        Request.Builder requestBuilder = new Request.Builder()
                .url(url)
                .addHeader("Content-Type", "application/json");
                
        if (isAzure) {
            requestBuilder.addHeader("api-key", apiKey);
        } else {
            requestBuilder.addHeader("Authorization", "Bearer " + apiKey);
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
            false // OpenAI doesn't support prompt caching
        );
    }

    private String getModelName(String modelId) {
        switch (modelId) {
            case "gpt-4":
                return "GPT-4";
            case "gpt-4-turbo":
                return "GPT-4 Turbo";
            case "gpt-3.5-turbo":
                return "GPT-3.5 Turbo";
            default:
                return modelId;
        }
    }
}