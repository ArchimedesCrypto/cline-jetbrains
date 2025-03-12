package com.cline.services.api;

import com.cline.services.ClineSettingsService;
import com.intellij.openapi.diagnostic.Logger;

import java.net.HttpURLConnection;
import java.util.Base64;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

/**
 * Authentication for API requests.
 */
public class ApiAuthentication {
    private static final Logger LOG = Logger.getInstance(ApiAuthentication.class);
    
    private final ClineSettingsService settingsService;
    private String cachedToken;
    private long tokenExpirationTime;
    
    /**
     * Creates a new API authentication.
     *
     * @param settingsService The settings service
     */
    public ApiAuthentication(ClineSettingsService settingsService) {
        this.settingsService = settingsService;
        this.cachedToken = null;
        this.tokenExpirationTime = 0;
    }
    
    /**
     * Applies authentication to a connection.
     *
     * @param connection The connection
     * @return A CompletableFuture that completes when authentication is applied
     */
    public CompletableFuture<Void> applyAuthentication(HttpURLConnection connection) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        
        // Get the API key
        String apiKey = settingsService.getApiKey();
        if (apiKey == null || apiKey.isEmpty()) {
            future.completeExceptionally(new RuntimeException("API key is not set"));
            return future;
        }
        
        // Apply the API key as a bearer token
        connection.setRequestProperty("Authorization", "Bearer " + apiKey);
        
        // Complete the future
        future.complete(null);
        
        return future;
    }
    
    /**
     * Gets a token for API requests.
     *
     * @return A CompletableFuture that completes with the token
     */
    public CompletableFuture<String> getToken() {
        CompletableFuture<String> future = new CompletableFuture<>();
        
        // Check if we have a cached token that's still valid
        if (cachedToken != null && System.currentTimeMillis() < tokenExpirationTime) {
            future.complete(cachedToken);
            return future;
        }
        
        // Get the API key
        String apiKey = settingsService.getApiKey();
        if (apiKey == null || apiKey.isEmpty()) {
            future.completeExceptionally(new RuntimeException("API key is not set"));
            return future;
        }
        
        // For now, just use the API key as the token
        // In a real implementation, we would exchange the API key for a token
        cachedToken = apiKey;
        tokenExpirationTime = System.currentTimeMillis() + 3600000; // 1 hour
        
        future.complete(cachedToken);
        
        return future;
    }
    
    /**
     * Refreshes the token.
     *
     * @return A CompletableFuture that completes with the new token
     */
    public CompletableFuture<String> refreshToken() {
        // Clear the cached token
        cachedToken = null;
        tokenExpirationTime = 0;
        
        // Get a new token
        return getToken();
    }
    
    /**
     * Checks if the API key is set.
     *
     * @return Whether the API key is set
     */
    public boolean isApiKeySet() {
        String apiKey = settingsService.getApiKey();
        return apiKey != null && !apiKey.isEmpty();
    }
}