package com.cline.services;

import com.cline.core.model.Conversation;
import com.cline.core.model.Message;
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
 * Tests for the ClineApiService class.
 */
public class ClineApiServiceTest {
    
    private ClineApiService apiService;
    
    @Mock
    private ClineSettingsService settingsService;
    
    @Mock
    private OkHttpClient mockClient;
    
    @Mock
    private Call mockCall;
    
    @BeforeEach
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        
        // Mock settings
        when(settingsService.getApiKey()).thenReturn("test-api-key");
        when(settingsService.getApiEndpoint()).thenReturn("https://api.example.com");
        when(settingsService.getModel()).thenReturn("test-model");
        when(settingsService.getMaxTokens()).thenReturn(1000);
        
        // Mock OkHttpClient
        when(mockClient.newCall(any(Request.class))).thenReturn(mockCall);
        
        // Set up the mock call to immediately invoke the failure callback
        doAnswer(invocation -> {
            Callback callback = invocation.getArgument(0);
            callback.onFailure(mockCall, new IOException("Test exception"));
            return null;
        }).when(mockCall).enqueue(any(Callback.class));
        
        // Create API service
        apiService = new ClineApiService();
        
        // Enable test mode to prevent logging errors
        apiService.setTestMode(true);
        
        // Inject mocked dependencies using reflection
        Field settingsServiceField = ClineApiService.class.getDeclaredField("settingsService");
        settingsServiceField.setAccessible(true);
        settingsServiceField.set(apiService, settingsService);
        
        Field clientField = ClineApiService.class.getDeclaredField("client");
        clientField.setAccessible(true);
        clientField.set(apiService, mockClient);
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
        
        // Verify that the client was called with the correct request
        verify(mockClient).newCall(any(Request.class));
        verify(mockCall).enqueue(any(Callback.class));
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
        
        // Verify that the client was called with the correct request
        verify(mockClient).newCall(any(Request.class));
        verify(mockCall).enqueue(any(Callback.class));
    }
}