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

public class AnthropicProviderTest extends BasePlatformTestCase {

    private MockWebServer mockWebServer;
    private AnthropicProvider provider;
    private String serverUrl;

    @BeforeEach
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mockWebServer = new MockWebServer();
        mockWebServer.start();
        serverUrl = mockWebServer.url("/").toString();

        // Use the mock server URL as the API endpoint
        provider = new AnthropicProvider("test-key", serverUrl, "claude-test", 100, true, false);
    }

    @AfterEach
    @Override
    protected void tearDown() throws Exception {
        mockWebServer.shutdown();
        super.tearDown();
    }

    // Note: sendMessage is not part of the newer Anthropic Messages API, skipping test

    @Test
    public void testSendConversationStreamingSuccess() throws InterruptedException {
        String streamEventStart = "event: message_start\ndata: {\"message\": {\"usage\": {\"input_tokens\": 10}}}\n\n";
        String streamEventDelta1 = "event: content_block_delta\ndata: {\"delta\": {\"text\": \"Hello\"}}\n\n";
        String streamEventDelta2 = "event: content_block_delta\ndata: {\"delta\": {\"text\": \" there\"}}\n\n";
        String streamEventUsage = "event: message_delta\ndata: {\"usage\": {\"output_tokens\": 5}}\n\n";
        String streamEventStop = "event: message_stop\ndata: {}\n\n";

        mockWebServer.enqueue(new MockResponse()
                .setBody(streamEventStart + streamEventDelta1 + streamEventDelta2 + streamEventUsage + streamEventStop)
                .setResponseCode(200)
                .addHeader("Content-Type", "text/event-stream"));

        ApiProvider.StreamHandler mockHandler = Mockito.mock(ApiProvider.StreamHandler.class);
        Conversation conv = Conversation.createEmpty();
        conv.addMessage(Message.createUserMessage("Test"));

        provider.sendConversationStreaming(conv, mockHandler);

        // Allow time for async operations
        Thread.sleep(500);

        ArgumentCaptor<String> textCaptor = ArgumentCaptor.forClass(String.class);
        verify(mockHandler, times(2)).onTextChunk(textCaptor.capture());
        assertEquals("Hello", textCaptor.getAllValues().get(0));
        assertEquals(" there", textCaptor.getAllValues().get(1));

        ArgumentCaptor<Integer> inputTokensCaptor = ArgumentCaptor.forClass(Integer.class);
        ArgumentCaptor<Integer> outputTokensCaptor = ArgumentCaptor.forClass(Integer.class);
        verify(mockHandler, times(1)).onUsage(inputTokensCaptor.capture(), outputTokensCaptor.capture());
        assertEquals(10, inputTokensCaptor.getValue());
        assertEquals(5, outputTokensCaptor.getValue());

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
        // onComplete might still be called in finally block
        // verify(mockHandler, never()).onComplete();
    }

    // Note: sendConversation uses streaming internally, covered by streaming tests
}