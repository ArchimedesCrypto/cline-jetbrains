package com.cline.services;

import com.cline.core.model.Mode;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Service for managing modes.
 */
@Service
public final class ClineModeService {
    private static final Logger LOG = Logger.getInstance(ClineModeService.class);
    private static final String GLOBAL_MODES_FILE = System.getProperty("user.home") + "/.vscode-remote/data/User/globalStorage/rooveterinaryinc.roo-cline/settings/cline_custom_modes.json";
    private static final String WORKSPACE_MODES_FILE = ".roomodes";
    
    private final Project project;
    private final Map<String, Mode> modes = new HashMap<>();
    private Mode currentMode;
    
    /**
     * Creates a new mode service.
     *
     * @param project The project
     */
    public ClineModeService(Project project) {
        this.project = project;
        
        // Load the default modes
        loadDefaultModes();
        
        // Load the global modes
        loadGlobalModes();
        
        // Load the workspace modes
        loadWorkspaceModes();
        
        // Set the default mode
        currentMode = getMode("code");
    }
    
    /**
     * Gets the mode service instance.
     *
     * @param project The project
     * @return The mode service instance
     */
    public static ClineModeService getInstance(@NotNull Project project) {
        return project.getService(ClineModeService.class);
    }
    
    /**
     * Gets a mode by slug.
     *
     * @param slug The mode slug
     * @return The mode, or null if not found
     */
    @Nullable
    public Mode getMode(String slug) {
        return modes.get(slug);
    }
    
    /**
     * Gets all modes.
     *
     * @return The modes
     */
    public List<Mode> getAllModes() {
        return new ArrayList<>(modes.values());
    }
    
    /**
     * Gets the current mode.
     *
     * @return The current mode
     */
    public Mode getCurrentMode() {
        return currentMode;
    }
    
    /**
     * Sets the current mode.
     *
     * @param slug The mode slug
     * @return A CompletableFuture that completes with the new mode
     */
    public CompletableFuture<Mode> setCurrentMode(String slug) {
        CompletableFuture<Mode> future = new CompletableFuture<>();
        
        Mode mode = getMode(slug);
        if (mode == null) {
            future.completeExceptionally(new RuntimeException("Mode not found: " + slug));
            return future;
        }
        
        currentMode = mode;
        future.complete(mode);
        
        return future;
    }
    
    /**
     * Adds a mode.
     *
     * @param mode The mode
     * @return A CompletableFuture that completes when the mode is added
     */
    public CompletableFuture<Void> addMode(Mode mode) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        
        modes.put(mode.getSlug(), mode);
        future.complete(null);
        
        return future;
    }
    
    /**
     * Removes a mode.
     *
     * @param slug The mode slug
     * @return A CompletableFuture that completes when the mode is removed
     */
    public CompletableFuture<Void> removeMode(String slug) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        
        if (modes.remove(slug) == null) {
            future.completeExceptionally(new RuntimeException("Mode not found: " + slug));
            return future;
        }
        
        future.complete(null);
        
        return future;
    }
    
    /**
     * Saves the global modes.
     *
     * @return A CompletableFuture that completes when the modes are saved
     */
    public CompletableFuture<Void> saveGlobalModes() {
        CompletableFuture<Void> future = new CompletableFuture<>();
        
        try {
            // Create the directory if it doesn't exist
            Path path = Paths.get(GLOBAL_MODES_FILE).getParent();
            if (!Files.exists(path)) {
                Files.createDirectories(path);
            }
            
            // Create the JSON object
            JsonObject json = new JsonObject();
            JsonArray modesArray = new JsonArray();
            
            // Add the custom modes
            for (Mode mode : modes.values()) {
                // Skip the default modes
                if (mode.getId().equals("code") || mode.getId().equals("architect") || mode.getId().equals("ask") || mode.getId().equals("debug")) {
                    continue;
                }
                
                modesArray.add(mode.toJson());
            }
            
            json.add("customModes", modesArray);
            
            // Write the JSON to the file
            try (FileWriter writer = new FileWriter(GLOBAL_MODES_FILE)) {
                new Gson().toJson(json, writer);
            }
            
            future.complete(null);
        } catch (IOException e) {
            future.completeExceptionally(e);
        }
        
        return future;
    }
    
    /**
     * Saves the workspace modes.
     *
     * @return A CompletableFuture that completes when the modes are saved
     */
    public CompletableFuture<Void> saveWorkspaceModes() {
        CompletableFuture<Void> future = new CompletableFuture<>();
        
        try {
            // Create the JSON object
            JsonObject json = new JsonObject();
            JsonArray modesArray = new JsonArray();
            
            // Add the custom modes
            for (Mode mode : modes.values()) {
                // Skip the default modes
                if (mode.getId().equals("code") || mode.getId().equals("architect") || mode.getId().equals("ask") || mode.getId().equals("debug")) {
                    continue;
                }
                
                modesArray.add(mode.toJson());
            }
            
            json.add("customModes", modesArray);
            
            // Write the JSON to the file
            try (FileWriter writer = new FileWriter(project.getBasePath() + "/" + WORKSPACE_MODES_FILE)) {
                new Gson().toJson(json, writer);
            }
            
            future.complete(null);
        } catch (IOException e) {
            future.completeExceptionally(e);
        }
        
        return future;
    }
    
    /**
     * Loads the default modes.
     */
    private void loadDefaultModes() {
        modes.put("code", Mode.createCodeMode());
        modes.put("architect", Mode.createArchitectMode());
        modes.put("ask", Mode.createAskMode());
        modes.put("debug", Mode.createDebugMode());
    }
    
    /**
     * Loads the global modes.
     */
    private void loadGlobalModes() {
        try {
            File file = new File(GLOBAL_MODES_FILE);
            if (!file.exists()) {
                return;
            }
            
            // Parse the JSON
            JsonObject json;
            try (FileReader reader = new FileReader(file)) {
                json = JsonParser.parseReader(reader).getAsJsonObject();
            }
            
            // Load the modes
            JsonArray modesArray = json.getAsJsonArray("customModes");
            for (int i = 0; i < modesArray.size(); i++) {
                JsonObject modeJson = modesArray.get(i).getAsJsonObject();
                Mode mode = Mode.fromJson(modeJson);
                modes.put(mode.getSlug(), mode);
            }
        } catch (IOException e) {
            LOG.error("Failed to load global modes", e);
        }
    }
    
    /**
     * Loads the workspace modes.
     */
    private void loadWorkspaceModes() {
        try {
            File file = new File(project.getBasePath() + "/" + WORKSPACE_MODES_FILE);
            if (!file.exists()) {
                return;
            }
            
            // Parse the JSON
            JsonObject json;
            try (FileReader reader = new FileReader(file)) {
                json = JsonParser.parseReader(reader).getAsJsonObject();
            }
            
            // Load the modes
            JsonArray modesArray = json.getAsJsonArray("customModes");
            for (int i = 0; i < modesArray.size(); i++) {
                JsonObject modeJson = modesArray.get(i).getAsJsonObject();
                Mode mode = Mode.fromJson(modeJson);
                modes.put(mode.getSlug(), mode);
            }
        } catch (IOException e) {
            LOG.error("Failed to load workspace modes", e);
        }
    }
}