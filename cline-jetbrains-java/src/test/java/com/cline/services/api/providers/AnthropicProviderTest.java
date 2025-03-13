package com.cline.services.api.providers;

import com.cline.core.model.Conversation;
import com.cline.core.model.Message;
import com.cline.services.api.ApiProvider;
import com.cline.services.api.ModelInfo;
import com.google.gson.JsonObject;
import okhttp3.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Tests for the AnthropicProvider class.
 */
public class AnthropicProviderTest {
    
    private AnthropicProvider provider;
    
    @Mock
    private OkHttpClient mockClient;
    
    @Mock
    private Call mockCall;
    
    @BeforeEach
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        
        // Mock OkHttpClient
        when(mockClient.newCall(any(Request.class))).thenReturn(mockCall);
        
        // Set up the mock call to immediately invoke the failure callback
        doAnswer(invocation -> {
            Callback callback = invocation.getArgument(0);
            callback.onFailure(mockCall, new IOException("Test exception"));
            return null;
        }).when(mockCall).enqueue(any(Callback.class));
        
        // Create provider
        provider = new AnthropicProvider(
            "test-api-key",
            "https://api.example.com",
            "claude-3-opus-20240229",
            1000,
            true, // testMode
            true  // enablePromptCaching
        );
        
        // Inject mocked client using reflection
        Field clientField = AnthropicProvider.class.getDeclaredField("client");
        clientField.setAccessible(true);
        clientField.set(provider, mockClient);
    }
    
    @Test
    public void testSendMessage() throws Exception {
        // Create a latch to wait for the async operation
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<String> result = new AtomicReference<>();
        AtomicBoolean hasError = new AtomicBoolean(false);
        
        // Send a message
        provider.sendMessage("Hello, world!", 100)
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
        
        // Verify that the client was called with the correct request
        verify(mockClient).newCall(any(Request.class));
        verify(mockCall).enqueue(any(Callback.class));
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
        provider.sendConversation(conversation)
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
        
        // Verify that the client was called with the correct request
        verify(mockClient).newCall(any(Request.class));
        verify(mockCall).enqueue(any(Callback.class));
    }
    
    @Test
    public void testSendConversationStreaming() throws Exception {
        // Create a conversation
        Conversation conversation = Conversation.createEmpty();
        conversation.addUserMessage("Hello, world!");
        
        // Create a mock stream handler
        ApiProvider.StreamHandler mockStreamHandler = mock(ApiProvider.StreamHandler.class);
        
        // Send the conversation streaming
        provider.sendConversationStreaming(conversation, mockStreamHandler);
        
        // Verify that the client was called with the correct request
        verify(mockClient).newCall(any(Request.class));
        verify(mockCall).enqueue(any(Callback.class));
        
        // Verify that the error handler was called
        verify(mockStreamHandler).onError(any(IOException.class));
    }
    
    @Test
    public void testGetModel() {
        // Test with Claude 3 Opus
        provider = new AnthropicProvider(
            "test-api-key",
            "https://api.example.com",
            "claude-3-opus-20240229",
            1000,
            true,
            true
        );
        
        ModelInfo modelInfo = provider.getModel();
        assertEquals("claude-3-opus-20240229", modelInfo.getId());
        assertEquals("Claude 3 Opus", modelInfo.getName());
        assertEquals(1000, modelInfo.getMaxTokens());
        assertTrue(modelInfo.supportsPromptCaching());
        
        // Test with Claude 3.5 Sonnet
        provider = new AnthropicProvider(
            "test-api-key",
            "https://api.example.com",
            "claude-3-5-sonnet-20241022",
            1000,
            true,
            true
        );
        
        modelInfo = provider.getModel();
        assertEquals("claude-3-5-sonnet-20241022", modelInfo.getId());
        assertEquals("Claude 3.5 Sonnet", modelInfo.getName());
        assertEquals(1000, modelInfo.getMaxTokens());
        assertTrue(modelInfo.supportsPromptCaching());
        
        // Test with a model that doesn't support prompt caching
        provider = new AnthropicProvider(
            "test-api-key",
            "https://api.example.com",
            "claude-2",
            1000,
            true,
            true
        );
        
        modelInfo = provider.getModel();
        assertEquals("claude-2", modelInfo.getId());
        assertEquals("claude-2", modelInfo.getName());
        assertEquals(1000, modelInfo.getMaxTokens());
        assertFalse(modelInfo.supportsPromptCaching());
    }
}