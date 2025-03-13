package com.cline.services;

import com.cline.core.model.Conversation;
import com.cline.core.model.Message;
import com.cline.services.api.ApiProvider;
import com.cline.services.api.providers.AnthropicProvider;
import com.cline.services.api.providers.OpenAiProvider;
import com.google.gson.JsonObject;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.diagnostic.Logger;

import java.util.concurrent.CompletableFuture;

/**
 * Service for interacting with the Cline API.
 */
@Service
public final class ClineApiService {
    private static final Logger LOG = Logger.getInstance(ClineApiService.class);
    
    // This field can be injected for testing
    private ClineSettingsService settingsService;
    
    // Flag to indicate if we're in test mode
    private boolean testMode = false;
    
    // The current API provider
    private ApiProvider apiProvider;

    /**
     * Functional interface for executing tools.
     */
    public interface Function<T, R> {
        R apply(T t);
    }

    public ClineApiService() {
        // Initialize with default settings
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
        // Reset the API provider to ensure it gets recreated with the new test mode
        this.apiProvider = null;
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
     * Get the appropriate API provider based on settings.
     *
     * @return The API provider
     */
    private ApiProvider getApiProvider() {
        if (apiProvider != null) {
            return apiProvider;
        }

        ClineSettingsService settings = getSettingsService();
        String provider = settings.getApiProvider();

        switch (provider) {
            case "openai":
                apiProvider = new OpenAiProvider(
                    settings.getOpenAiApiKey(),
                    settings.getOpenAiApiEndpoint(),
                    settings.getOpenAiModel(),
                    settings.getMaxTokens(),
                    testMode,
                    settings.isAzureOpenAi(),
                    settings.getAzureApiVersion()
                );
                break;
            case "anthropic":
            default:
                apiProvider = new AnthropicProvider(
                    settings.getApiKey(),
                    settings.getApiEndpoint(),
                    settings.getModel(),
                    settings.getMaxTokens(),
                    testMode,
                    settings.isEnablePromptCaching()
                );
                break;
        }

        return apiProvider;
    }

    /**
     * Send a message to the AI model.
     *
     * @param prompt    The user's prompt
     * @param maxTokens Maximum number of tokens to generate
     * @return A CompletableFuture containing the AI response
     */
    public CompletableFuture<String> sendMessage(String prompt, int maxTokens) {
        return getApiProvider().sendMessage(prompt, maxTokens);
    }

    /**
     * Send a conversation to the AI model with streaming response.
     *
     * @param conversation The conversation
     * @param streamHandler The stream handler for receiving chunks
     */
    public void sendConversationStreaming(Conversation conversation, ApiProvider.StreamHandler streamHandler) {
        getApiProvider().sendConversationStreaming(conversation, streamHandler);
    }

    /**
     * Send a conversation to the AI model.
     *
     * @param conversation The conversation
     * @return A CompletableFuture containing the AI response
     */
    public CompletableFuture<Message> sendConversation(Conversation conversation) {
        return getApiProvider().sendConversation(conversation);
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