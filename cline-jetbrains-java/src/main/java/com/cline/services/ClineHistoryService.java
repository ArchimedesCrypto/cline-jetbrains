package com.cline.services;

import com.cline.core.model.Conversation;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Service for managing conversation history.
 */
@Service
public final class ClineHistoryService {
    private static final Logger LOG = Logger.getInstance(ClineHistoryService.class);
    private static final String HISTORY_FILE_NAME = "cline-history.json";
    
    private final Project project;
    private final Gson gson;
    private final List<Conversation> conversations = new ArrayList<>();
    
    /**
     * Creates a new history service.
     *
     * @param project The project
     */
    public ClineHistoryService(Project project) {
        this.project = project;
        this.gson = new GsonBuilder().setPrettyPrinting().create();
        
        loadConversations();
    }
    
    /**
     * Gets the history service instance.
     *
     * @param project The project
     * @return The history service instance
     */
    public static ClineHistoryService getInstance(Project project) {
        return project.getService(ClineHistoryService.class);
    }
    
    /**
     * Gets all conversations.
     *
     * @return An unmodifiable list of conversations
     */
    public List<Conversation> getConversations() {
        return Collections.unmodifiableList(conversations);
    }
    
    /**
     * Adds a conversation to the history.
     *
     * @param conversation The conversation to add
     * @return A CompletableFuture that completes when the conversation is added
     */
    public CompletableFuture<Void> addConversation(Conversation conversation) {
        return CompletableFuture.runAsync(() -> {
            conversations.add(conversation);
            saveConversations();
        });
    }
    
    /**
     * Updates a conversation in the history.
     *
     * @param conversation The conversation to update
     * @return A CompletableFuture that completes when the conversation is updated
     */
    public CompletableFuture<Void> updateConversation(Conversation conversation) {
        return CompletableFuture.runAsync(() -> {
            // Find the conversation by ID
            for (int i = 0; i < conversations.size(); i++) {
                if (conversations.get(i).getId().equals(conversation.getId())) {
                    conversations.set(i, conversation);
                    saveConversations();
                    return;
                }
            }
            
            // If not found, add it
            conversations.add(conversation);
            saveConversations();
        });
    }
    
    /**
     * Removes a conversation from the history.
     *
     * @param conversation The conversation to remove
     * @return A CompletableFuture that completes when the conversation is removed
     */
    public CompletableFuture<Void> removeConversation(Conversation conversation) {
        return CompletableFuture.runAsync(() -> {
            conversations.removeIf(c -> c.getId().equals(conversation.getId()));
            saveConversations();
        });
    }
    
    /**
     * Clears all conversations from the history.
     *
     * @return A CompletableFuture that completes when the history is cleared
     */
    public CompletableFuture<Void> clearHistory() {
        return CompletableFuture.runAsync(() -> {
            conversations.clear();
            saveConversations();
        });
    }
    
    /**
     * Loads conversations from storage.
     */
    private void loadConversations() {
        File historyFile = getHistoryFile();
        
        if (!historyFile.exists()) {
            LOG.info("History file does not exist: " + historyFile.getAbsolutePath());
            return;
        }
        
        try (FileReader reader = new FileReader(historyFile)) {
            Type listType = new TypeToken<ArrayList<Conversation>>() {}.getType();
            List<Conversation> loadedConversations = gson.fromJson(reader, listType);
            
            if (loadedConversations != null) {
                conversations.clear();
                conversations.addAll(loadedConversations);
                LOG.info("Loaded " + conversations.size() + " conversations from history");
            }
        } catch (IOException e) {
            LOG.error("Error loading conversations from history", e);
        }
    }
    
    /**
     * Saves conversations to storage.
     */
    private void saveConversations() {
        File historyFile = getHistoryFile();
        
        try {
            // Create parent directories if they don't exist
            if (!historyFile.getParentFile().exists()) {
                if (!historyFile.getParentFile().mkdirs()) {
                    LOG.error("Failed to create directories for history file: " + historyFile.getAbsolutePath());
                    return;
                }
            }
            
            // Write conversations to file
            try (FileWriter writer = new FileWriter(historyFile)) {
                gson.toJson(conversations, writer);
                LOG.info("Saved " + conversations.size() + " conversations to history");
            }
        } catch (IOException e) {
            LOG.error("Error saving conversations to history", e);
        }
    }
    
    /**
     * Gets the history file.
     *
     * @return The history file
     */
    private File getHistoryFile() {
        String basePath = project.getBasePath();
        if (basePath == null) {
            basePath = System.getProperty("user.home");
        }
        
        return new File(basePath, ".cline/" + HISTORY_FILE_NAME);
    }
}