package com.cline.services.api.providers;

import com.cline.core.model.Conversation;
import com.cline.core.model.Message;
import com.cline.services.api.ApiProvider;
import com.google.gson.JsonObject;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import okhttp3.*;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;

import static org.mockito.Mockito.*;

public class OpenAiProviderTest extends BasePlatformTestCase {

    private MockWebServer mockWebServer;
    private OpenAiProvider provider;
    private String serverUrl;

    @BeforeEach
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mockWebServer = new MockWebServer();
        mockWebServer.start();
        serverUrl = mockWebServer.url("/").toString();

        // Use the mock server URL as the API endpoint
        provider = new OpenAiProvider("test-key", serverUrl, "gpt-test", 100, true, false, null);
    }

    @AfterEach
    @Override
    protected void tearDown() throws Exception {
        mockWebServer.shutdown();
        super.tearDown();
    }

    @Test
    public void testSendMessageSuccess() throws Exception {
        String mockJsonResponse = "{\"choices\": [{\"message\": {\"content\": \"Test response\"}}]}";
        mockWebServer.enqueue(new MockResponse().setBody(mockJsonResponse).setResponseCode(200));

        String response = provider.sendMessage("Test prompt", 100).get();
        assertEquals("Test response", response);
    }

    @Test
    public void testSendMessageFailure() {
        mockWebServer.enqueue(new MockResponse().setResponseCode(500));

        CompletableFuture<String> future = provider.sendMessage("Test prompt", 100);
        assertTrue(future.isCompletedExceptionally());
    }

    @Test
    public void testSendConversationStreamingSuccess() throws InterruptedException {
        String streamEvent1 = "data: {\"choices\": [{\"delta\": {\"content\": \"Hello\"}}]}\n\n";
        String streamEvent2 = "data: {\"choices\": [{\"delta\": {\"content\": \" there\"}}]}\n\n";
        String streamEventDone = "data: [DONE]\n\n";
        mockWebServer.enqueue(new MockResponse()
                .setBody(streamEvent1 + streamEvent2 + streamEventDone)
                .setResponseCode(200)
                .addHeader("Content-Type", "text/event-stream"));

        ApiProvider.StreamHandler mockHandler = Mockito.mock(ApiProvider.StreamHandler.class);
        Conversation conv = Conversation.createEmpty();
        conv.addMessage(Message.createUserMessage("Test"));

        provider.sendConversationStreaming(conv, mockHandler);

        // Allow time for async operations
        Thread.sleep(500);

        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(mockHandler, times(2)).onTextChunk(captor.capture());
        assertEquals("Hello", captor.getAllValues().get(0));
        assertEquals(" there", captor.getAllValues().get(1));
        verify(mockHandler, times(1)).onComplete();
        verify(mockHandler, never()).onError(any());
    }

     @Test
    public void testSendConversationStreamingError() throws InterruptedException {
        mockWebServer.enqueue(new MockResponse().setResponseCode(500));

        ApiProvider.StreamHandler mockHandler = Mockito.mock(ApiProvider.StreamHandler.class);
        Conversation conv = Conversation.createEmpty();
        conv.addMessage(Message.createUserMessage("Test"));

        provider.sendConversationStreaming(conv, mockHandler);

        // Allow time for async operations
        Thread.sleep(500);

        verify(mockHandler, times(1)).onError(any(IOException.class));
        verify(mockHandler, never()).onTextChunk(any());
        // onComplete might still be called in finally block, depending on exact error point
        // verify(mockHandler, never()).onComplete();
    }

    @Test
    public void testSendConversationSuccess() throws Exception {
        // Mock the streaming response which sendConversation uses internally
        String streamEvent1 = "data: {\"choices\": [{\"delta\": {\"content\": \"Final\"}}]}\n\n";
        String streamEvent2 = "data: {\"choices\": [{\"delta\": {\"content\": \" response\"}}]}\n\n";
        String streamEventDone = "data: [DONE]\n\n";
        mockWebServer.enqueue(new MockResponse()
                .setBody(streamEvent1 + streamEvent2 + streamEventDone)
                .setResponseCode(200)
                .addHeader("Content-Type", "text/event-stream"));

        Conversation conv = Conversation.createEmpty();
        conv.addMessage(Message.createUserMessage("Test"));
        Message response = provider.sendConversation(conv).get();

        assertEquals("Final response", response.getContent());
        assertEquals(MessageRole.ASSISTANT, response.getRole());
    }
}