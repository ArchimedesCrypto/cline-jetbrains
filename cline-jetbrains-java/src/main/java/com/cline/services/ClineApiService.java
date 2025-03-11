package com.cline.services;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.diagnostic.Logger;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
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

    public ClineApiService() {
        this.client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build();
        this.gson = new Gson();
    }

    public static ClineApiService getInstance() {
        return ApplicationManager.getApplication().getService(ClineApiService.class);
    }

    /**
     * Send a message to the AI model.
     *
     * @param prompt   The user's prompt
     * @param maxTokens Maximum number of tokens to generate
     * @return A CompletableFuture containing the AI response
     */
    public CompletableFuture<String> sendMessage(String prompt, int maxTokens) {
        ClineSettingsService settings = ClineSettingsService.getInstance();
        
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
                LOG.error("API request failed", e);
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
}