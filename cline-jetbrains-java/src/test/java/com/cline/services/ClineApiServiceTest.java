package com.cline.services;

import com.cline.core.model.Conversation;
import com.cline.core.model.Message;
import com.cline.services.api.ApiProvider;
import com.google.gson.JsonObject;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.concurrent.CompletableFuture;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ClineApiServiceTest extends BasePlatformTestCase {

    private ClineApiService apiService;
    private ApiProvider mockProvider;
    private ClineSettingsService mockSettingsService;

    @BeforeEach
    @Override
    protected void setUp() throws Exception {
        super.setUp();

        mockProvider = Mockito.mock(ApiProvider.class);
        mockSettingsService = Mockito.mock(ClineSettingsService.class);

        // Mock settings to return a provider type
        when(mockSettingsService.getApiProvider()).thenReturn("mock");

        apiService = new ClineApiService() {
            @Override
            protected ApiProvider getApiProvider() {
                return mockProvider; // Inject mock provider
            }
            @Override
            protected ClineSettingsService getSettingsService() {
                return mockSettingsService; // Inject mock settings
            }
        };
        apiService.setTestMode(true); // Ensure test mode is on
    }

    @Test
    public void testSendMessage() throws Exception {
        String expectedResponse = "Test response";
        when(mockProvider.sendMessage(any(String.class), any(Integer.class)))
                .thenReturn(CompletableFuture.completedFuture(expectedResponse));

        String response = apiService.sendMessage("Test prompt", 100).get();

        assertEquals(expectedResponse, response);
        verify(mockProvider).sendMessage("Test prompt", 100);
    }

    @Test
    public void testSendConversation() throws Exception {
        Message expectedMessage = Message.createAssistantMessage("Test response");
        when(mockProvider.sendConversation(any(Conversation.class)))
                .thenReturn(CompletableFuture.completedFuture(expectedMessage));

        Conversation conv = Conversation.createEmpty();
        conv.addMessage(Message.createUserMessage("Test prompt"));
        Message response = apiService.sendConversation(conv).get();

        assertEquals(expectedMessage, response);
        verify(mockProvider).sendConversation(conv);
    }

    @Test
    public void testSendConversationStreaming() {
        ApiProvider.StreamHandler mockHandler = Mockito.mock(ApiProvider.StreamHandler.class);
        Conversation conv = Conversation.createEmpty();
        conv.addMessage(Message.createUserMessage("Test prompt"));

        apiService.sendConversationStreaming(conv, mockHandler);

        verify(mockProvider).sendConversationStreaming(conv, mockHandler);
    }

    @Test
    public void testExecuteToolAndContinue() throws Exception {
        Message expectedMessage = Message.createAssistantMessage("Tool executed response");
        when(mockProvider.sendConversation(any(Conversation.class)))
                .thenReturn(CompletableFuture.completedFuture(expectedMessage));

        Conversation conv = Conversation.createEmpty();
        conv.addMessage(Message.createUserMessage("Use tool"));

        String toolName = "testTool";
        JsonObject toolInput = new JsonObject();
        JsonObject toolResult = new JsonObject();
        toolResult.addProperty("status", "success");

        ClineApiService.Function<JsonObject, CompletableFuture<JsonObject>> mockExecutor =
                input -> CompletableFuture.completedFuture(toolResult);

        Message response = apiService.executeToolAndContinue(conv, toolName, toolInput, mockExecutor).get();

        assertEquals(expectedMessage, response);
        // Verify tool result message was added
        assertEquals(3, conv.getMessages().size()); // User, Tool Result, Assistant
        assertEquals(MessageRole.TOOL, conv.getMessages().get(1).getRole());
        assertEquals(toolResult.toString(), conv.getMessages().get(1).getContent());
        // Verify final API call was made
        verify(mockProvider).sendConversation(conv);
    }
}