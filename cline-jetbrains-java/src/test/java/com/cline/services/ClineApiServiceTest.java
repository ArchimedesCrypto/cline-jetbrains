package com.cline.services;

import com.cline.core.model.Conversation;
import com.cline.core.model.Message;
import com.cline.services.api.ApiProvider;
import com.cline.services.api.providers.AnthropicProvider;
import com.cline.services.api.providers.OpenAiProvider;
import com.google.gson.JsonObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Tests for the ClineApiService class.
 */
public class ClineApiServiceTest {
    
    private ClineApiService apiService;
    
    @Mock
    private ClineSettingsService settingsService;
    
    @Mock
    private ApiProvider mockProvider;
    
    @BeforeEach
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        
        // Mock settings for Anthropic
        when(settingsService.getApiProvider()).thenReturn("anthropic");
        when(settingsService.getApiKey()).thenReturn("test-api-key");
        when(settingsService.getApiEndpoint()).thenReturn("https://api.example.com");
        when(settingsService.getModel()).thenReturn("test-model");
        when(settingsService.getMaxTokens()).thenReturn(1000);
        when(settingsService.isEnablePromptCaching()).thenReturn(true);
        
        // Mock settings for OpenAI
        when(settingsService.getOpenAiApiKey()).thenReturn("test-openai-key");
        when(settingsService.getOpenAiApiEndpoint()).thenReturn("https://api.openai.com");
        when(settingsService.getOpenAiModel()).thenReturn("gpt-4");
        when(settingsService.isAzureOpenAi()).thenReturn(false);
        when(settingsService.getAzureApiVersion()).thenReturn("2023-05-15");
        
        // Create API service
        apiService = new ClineApiService();
        
        // Enable test mode to prevent logging errors
        apiService.setTestMode(true);
        
        // Inject mocked dependencies using reflection
        java.lang.reflect.Field settingsServiceField = ClineApiService.class.getDeclaredField("settingsService");
        settingsServiceField.setAccessible(true);
        settingsServiceField.set(apiService, settingsService);
        
        // Inject mock provider
        java.lang.reflect.Field providerField = ClineApiService.class.getDeclaredField("apiProvider");
        providerField.setAccessible(true);
        providerField.set(apiService, mockProvider);
        
        // Mock provider methods to return failed futures
        when(mockProvider.sendMessage(anyString(), anyInt())).thenReturn(
            CompletableFuture.failedFuture(new Exception("Test exception"))
        );
        
        when(mockProvider.sendConversation(any(Conversation.class))).thenReturn(
            CompletableFuture.failedFuture(new Exception("Test exception"))
        );
        
        doAnswer(invocation -> {
            ApiProvider.StreamHandler handler = invocation.getArgument(1);
            handler.onError(new Exception("Test exception"));
            return null;
        }).when(mockProvider).sendConversationStreaming(any(Conversation.class), any(ApiProvider.StreamHandler.class));
    }
    
    @Test
    public void testSendMessage() throws Exception {
        // Create a latch to wait for the async operation
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<String> result = new AtomicReference<>();
        AtomicBoolean hasError = new AtomicBoolean(false);
        
        // Send a message
        apiService.sendMessage("Hello, world!", 100)
                .thenAccept(response -> {
                    result.set(response);
                    latch.countDown();
                })
                .exceptionally(e -> {
                    hasError.set(true);
                    latch.countDown();
                    return null;
                });
        
        // Wait for the operation to complete (with timeout)
        boolean completed = latch.await(10, TimeUnit.SECONDS);
        
        // Assert that the operation completed
        assertTrue(completed, "Operation timed out");
        
        // Since we're mocking a failure, we expect an error
        assertTrue(hasError.get(), "Expected an error due to mocked failure");
        
        // Verify that the provider method was called
        verify(mockProvider).sendMessage("Hello, world!", 100);
    }
    
    @Test
    public void testSendConversation() throws Exception {
        // Create a conversation
        Conversation conversation = Conversation.createEmpty();
        conversation.addUserMessage("Hello, world!");
        
        // Create a latch to wait for the async operation
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<Message> result = new AtomicReference<>();
        AtomicBoolean hasError = new AtomicBoolean(false);
        
        // Send the conversation
        apiService.sendConversation(conversation)
                .thenAccept(response -> {
                    result.set(response);
                    latch.countDown();
                })
                .exceptionally(e -> {
                    hasError.set(true);
                    latch.countDown();
                    return null;
                });
        
        // Wait for the operation to complete (with timeout)
        boolean completed = latch.await(10, TimeUnit.SECONDS);
        
        // Assert that the operation completed
        assertTrue(completed, "Operation timed out");
        
        // Since we're mocking a failure, we expect an error
        assertTrue(hasError.get(), "Expected an error due to mocked failure");
        
        // Verify that the provider method was called
        verify(mockProvider).sendConversation(conversation);
    }
    
    @Test
    public void testSendConversationStreaming() throws Exception {
        // Create a conversation
        Conversation conversation = Conversation.createEmpty();
        conversation.addUserMessage("Hello, world!");
        
        // Create a mock stream handler
        ApiProvider.StreamHandler mockStreamHandler = mock(ApiProvider.StreamHandler.class);
        
        // Send the conversation streaming
        apiService.sendConversationStreaming(conversation, mockStreamHandler);
        
        // Verify that the provider method was called
        verify(mockProvider).sendConversationStreaming(eq(conversation), any(ApiProvider.StreamHandler.class));
        
        // Verify that the error handler was called
        verify(mockStreamHandler).onError(any(Exception.class));
    }
    
    @Test
    public void testExecuteToolAndContinue() throws Exception {
        // Create a conversation
        Conversation conversation = Conversation.createEmpty();
        conversation.addUserMessage("Hello, world!");
        
        // Create a tool executor function
        ClineApiService.Function<JsonObject, CompletableFuture<JsonObject>> toolExecutor = input -> {
            JsonObject result = new JsonObject();
            result.addProperty("result", "Tool executed successfully");
            return CompletableFuture.completedFuture(result);
        };
        
        // Create a latch to wait for the async operation
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<Message> result = new AtomicReference<>();
        AtomicBoolean hasError = new AtomicBoolean(false);
        
        // Execute the tool and continue
        apiService.executeToolAndContinue(conversation, "test-tool", new JsonObject(), toolExecutor)
                .thenAccept(response -> {
                    result.set(response);
                    latch.countDown();
                })
                .exceptionally(e -> {
                    hasError.set(true);
                    latch.countDown();
                    return null;
                });
        
        // Wait for the operation to complete (with timeout)
        boolean completed = latch.await(10, TimeUnit.SECONDS);
        
        // Assert that the operation completed
        assertTrue(completed, "Operation timed out");
        
        // Since we're mocking a failure, we expect an error
        assertTrue(hasError.get(), "Expected an error due to mocked failure");
        
        // Verify that the provider method was called
        verify(mockProvider).sendConversation(conversation);
    }
    
    @Test
    public void testProviderSelection() throws Exception {
        // Reset the provider to null to force recreation
        java.lang.reflect.Field providerField = ClineApiService.class.getDeclaredField("apiProvider");
        providerField.setAccessible(true);
        providerField.set(apiService, null);
        
        // Test Anthropic provider
        when(settingsService.getApiProvider()).thenReturn("anthropic");
        
        // Call a method to trigger provider creation
        try {
            apiService.sendMessage("Test", 100);
        } catch (Exception e) {
            // Ignore exception
        }
        
        // Get the created provider
        ApiProvider provider = (ApiProvider) providerField.get(apiService);
        
        // Verify it's an AnthropicProvider
        assertTrue(provider instanceof AnthropicProvider, "Expected AnthropicProvider");
        
        // Reset the provider
        providerField.set(apiService, null);
        
        // Test OpenAI provider
        when(settingsService.getApiProvider()).thenReturn("openai");
        
        // Call a method to trigger provider creation
        try {
            apiService.sendMessage("Test", 100);
        } catch (Exception e) {
            // Ignore exception
        }
        
        // Get the created provider
        provider = (ApiProvider) providerField.get(apiService);
        
        // Verify it's an OpenAiProvider
        assertTrue(provider instanceof OpenAiProvider, "Expected OpenAiProvider");
    }
}