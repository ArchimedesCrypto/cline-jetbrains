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
 * Tests for the OpenAiProvider class.
 */
public class OpenAiProviderTest {
    
    private OpenAiProvider provider;
    
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
        provider = new OpenAiProvider(
            "test-api-key",
            "https://api.openai.com",
            "gpt-4",
            1000,
            true, // testMode
            false, // isAzure
            "2023-05-15" // azureApiVersion
        );
        
        // Inject mocked client using reflection
        Field clientField = OpenAiProvider.class.getDeclaredField("client");
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
        // Test with GPT-4
        provider = new OpenAiProvider(
            "test-api-key",
            "https://api.openai.com",
            "gpt-4",
            1000,
            true,
            false,
            "2023-05-15"
        );
        
        ModelInfo modelInfo = provider.getModel();
        assertEquals("gpt-4", modelInfo.getId());
        assertEquals("GPT-4", modelInfo.getName());
        assertEquals(1000, modelInfo.getMaxTokens());
        assertFalse(modelInfo.supportsPromptCaching());
        
        // Test with GPT-4 Turbo
        provider = new OpenAiProvider(
            "test-api-key",
            "https://api.openai.com",
            "gpt-4-turbo",
            1000,
            true,
            false,
            "2023-05-15"
        );
        
        modelInfo = provider.getModel();
        assertEquals("gpt-4-turbo", modelInfo.getId());
        assertEquals("GPT-4 Turbo", modelInfo.getName());
        assertEquals(1000, modelInfo.getMaxTokens());
        assertFalse(modelInfo.supportsPromptCaching());
        
        // Test with GPT-3.5 Turbo
        provider = new OpenAiProvider(
            "test-api-key",
            "https://api.openai.com",
            "gpt-3.5-turbo",
            1000,
            true,
            false,
            "2023-05-15"
        );
        
        modelInfo = provider.getModel();
        assertEquals("gpt-3.5-turbo", modelInfo.getId());
        assertEquals("GPT-3.5 Turbo", modelInfo.getName());
        assertEquals(1000, modelInfo.getMaxTokens());
        assertFalse(modelInfo.supportsPromptCaching());
        
        // Test with a custom model
        provider = new OpenAiProvider(
            "test-api-key",
            "https://api.openai.com",
            "custom-model",
            1000,
            true,
            false,
            "2023-05-15"
        );
        
        modelInfo = provider.getModel();
        assertEquals("custom-model", modelInfo.getId());
        assertEquals("custom-model", modelInfo.getName());
        assertEquals(1000, modelInfo.getMaxTokens());
        assertFalse(modelInfo.supportsPromptCaching());
    }
    
    @Test
    public void testAzureEndpoint() throws Exception {
        // Create Azure provider
        provider = new OpenAiProvider(
            "test-api-key",
            "https://example.azure.com",
            "gpt-4",
            1000,
            true,
            true,
            "2023-05-15"
        );
        
        // Inject mocked client using reflection
        Field clientField = OpenAiProvider.class.getDeclaredField("client");
        clientField.setAccessible(true);
        clientField.set(provider, mockClient);
        
        // Create a latch to wait for the async operation
        CountDownLatch latch = new CountDownLatch(1);
        AtomicBoolean hasError = new AtomicBoolean(false);
        
        // Send a message
        provider.sendMessage("Hello, world!", 100)
                .exceptionally(e -> {
                    hasError.set(true);
                    latch.countDown();
                    return null;
                });
        
        // Wait for the operation to complete (with timeout)
        boolean completed = latch.await(10, TimeUnit.SECONDS);
        
        // Assert that the operation completed
        assertTrue(completed, "Operation timed out");
        
        // Verify that the client was called with the correct request
        verify(mockClient).newCall(argThat(request -> 
            request.url().toString().contains("deployments") && 
            request.url().toString().contains("api-version=2023-05-15")
        ));
    }
}