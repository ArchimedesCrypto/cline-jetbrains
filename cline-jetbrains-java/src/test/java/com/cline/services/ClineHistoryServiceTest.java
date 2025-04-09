package com.cline.services;

import com.cline.core.model.Conversation;
import com.cline.core.model.Message;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.ExecutionException;

public class ClineHistoryServiceTest extends BasePlatformTestCase {

    private ClineHistoryService historyService;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        historyService = ClineHistoryService.getInstance(getProject());
        // Clear history before each test
        historyService.clearHistory().get();
    }

    @Test
    public void testAddAndGetConversations() throws ExecutionException, InterruptedException {
        assertEquals(0, historyService.getConversations().size());

        Conversation conv1 = Conversation.createEmpty();
        conv1.addMessage(Message.createUserMessage("Test 1"));
        historyService.addConversation(conv1).get();

        Conversation conv2 = Conversation.createEmpty();
        conv2.addMessage(Message.createUserMessage("Test 2"));
        historyService.addConversation(conv2).get();

        List<Conversation> conversations = historyService.getConversations();
        assertEquals(2, conversations.size());
        // Assuming they are returned in reverse order (newest first)
        assertEquals("Test 2", conversations.get(0).getMessages().get(0).getContent());
        assertEquals("Test 1", conversations.get(1).getMessages().get(0).getContent());
    }

    @Test
    public void testUpdateConversation() throws ExecutionException, InterruptedException {
        Conversation conv = Conversation.createEmpty();
        conv.addMessage(Message.createUserMessage("Initial"));
        historyService.addConversation(conv).get();

        conv.addMessage(Message.createAssistantMessage("Updated"));
        historyService.updateConversation(conv).get();

        List<Conversation> conversations = historyService.getConversations();
        assertEquals(1, conversations.size());
        assertEquals(2, conversations.get(0).getMessages().size());
        assertEquals("Updated", conversations.get(0).getLastMessage().getContent());
    }

    @Test
    public void testRemoveConversation() throws ExecutionException, InterruptedException {
        Conversation conv1 = Conversation.createEmpty();
        conv1.addMessage(Message.createUserMessage("Conv 1"));
        historyService.addConversation(conv1).get();

        Conversation conv2 = Conversation.createEmpty();
        conv2.addMessage(Message.createUserMessage("Conv 2"));
        historyService.addConversation(conv2).get();

        assertEquals(2, historyService.getConversations().size());

        historyService.removeConversation(conv1).get();
        assertEquals(1, historyService.getConversations().size());
        assertEquals("Conv 2", historyService.getConversations().get(0).getMessages().get(0).getContent());
    }

    @Test
    public void testClearHistory() throws ExecutionException, InterruptedException {
        historyService.addConversation(Conversation.createEmpty()).get();
        historyService.addConversation(Conversation.createEmpty()).get();
        assertEquals(2, historyService.getConversations().size());

        historyService.clearHistory().get();
        assertEquals(0, historyService.getConversations().size());
    }
}