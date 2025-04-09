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
                    streamHandler.onError(new IOException("Unexpected response: " + response + " Body: " + response.body().string()));
                    return;
                }

                ResponseBody responseBody = response.body();
                if (responseBody == null) {
                    streamHandler.onError(new IOException("Empty response body"));
                    return;
                }

                try (ResponseBody body = responseBody) {
                    String line;
                    while ((line = body.source().readUtf8Line()) != null) {
                        if (line.startsWith("data: ")) {
                            String dataJson = line.substring(6);
                            if (dataJson.equals("[DONE]")) {
                                streamHandler.onComplete();
                                break;
                            }
                            try {
                                JsonObject data = JsonParser.parseString(dataJson).getAsJsonObject();
                                if (data.has("choices")) {
                                    JsonArray choices = data.getAsJsonArray("choices");
                                    if (choices.size() > 0) {
                                        JsonObject choice = choices.get(0).getAsJsonObject();
                                        if (choice.has("delta")) {
                                            JsonObject delta = choice.getAsJsonObject("delta");
                                            if (delta.has("content") && !delta.get("content").isJsonNull()) {
                                                streamHandler.onTextChunk(delta.get("content").getAsString());
                                            }
                                            // TODO: Handle tool calls in delta
                                        }
                                    }
                                }
                                // TODO: Handle usage info if present in stream
                            } catch (JsonSyntaxException e) {
                                streamHandler.onError(new IOException("Error parsing stream data: " + dataJson, e));
                            }
                        }
                    }
                } catch (IOException e) {
                    streamHandler.onError(e);
                } finally {
                    streamHandler.onComplete(); // Ensure complete is called even on error or unexpected end
                }
            }
        });
    }

    @Override
    public CompletableFuture<Message> sendConversation(Conversation conversation) {
        CompletableFuture<Message> future = new CompletableFuture<>();
        StringBuilder responseBuilder = new StringBuilder();
        final int[] inputTokens = {0};
        final int[] outputTokens = {0};
        final JsonObject[] toolCall = {null}; // Placeholder for tool call info

        sendConversationStreaming(conversation, new StreamHandler() {
            @Override
            public void onTextChunk(String text) {
                responseBuilder.append(text);
            }

            @Override
            public void onToolUse(String toolName, JsonObject toolInput) {
                // TODO: Handle tool use properly if needed in non-streaming
                toolCall[0] = new JsonObject();
                toolCall[0].addProperty("name", toolName);
                toolCall[0].add("input", toolInput);
            }

            @Override
            public void onUsage(int inTokens, int outTokens) {
                inputTokens[0] = inTokens;
                outputTokens[0] = outTokens;
            }

            @Override
            public void onComplete() {
                String content = responseBuilder.toString();
                Message message = Message.createAssistantMessage(content);

                // Add usage metadata
                JsonObject metadata = new JsonObject();
                metadata.addProperty("inputTokens", inputTokens[0]);
                metadata.addProperty("outputTokens", outputTokens[0]);
                // TODO: Calculate and add cost based on model pricing
                // metadata.addProperty("cost", calculateCost(inputTokens[0], outputTokens[0]));
                message.setMetadata(metadata);

                // TODO: Set tool call info on message metadata if toolCall[0] is not null

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