package com.cline.jetbrains.bridge;

import com.cline.jetbrains.services.ClineSettingsService;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Manager for the Cline bridge.
 * This class is responsible for initializing and managing the bridge between Java and TypeScript.
 */
@Service(Service.Level.PROJECT)
public final class ClineBridgeManager {
    private static final Logger LOG = Logger.getInstance(ClineBridgeManager.class);
    
    private final Project project;
    private final ClineSettingsService settingsService;
    private final AtomicBoolean initialized = new AtomicBoolean(false);
    private final AtomicBoolean initializing = new AtomicBoolean(false);
    
    private JavaScriptBridge jsBridge;
    
    /**
     * Constructor.
     * @param project The current project
     */
    public ClineBridgeManager(@NotNull Project project) {
        this.project = project;
        this.settingsService = ClineSettingsService.getInstance();
        
        LOG.info("ClineBridgeManager created for project: " + project.getName());
        
        // Register a disposable to clean up resources when the project is closed
        Disposer.register(project, () -> {
            LOG.info("Disposing ClineBridgeManager for project: " + project.getName());
            dispose();
        });
    }
    
    /**
     * Get the instance of the bridge manager.
     * @param project The current project
     * @return The bridge manager instance
     */
    public static ClineBridgeManager getInstance(@NotNull Project project) {
        return project.getService(ClineBridgeManager.class);
    }
    
    /**
     * Initialize the bridge.
     * @return A future that completes when the bridge is initialized
     */
    public CompletableFuture<Void> initialize() {
        if (initialized.get()) {
            LOG.info("Bridge already initialized");
            return CompletableFuture.completedFuture(null);
        }
        
        if (initializing.getAndSet(true)) {
            LOG.info("Bridge initialization already in progress");
            return CompletableFuture.completedFuture(null);
        }
        
        LOG.info("Initializing bridge");
        
        CompletableFuture<Void> future = new CompletableFuture<>();
        
        try {
            // Create the JavaScript bridge
            jsBridge = new JavaScriptBridge(project);
            
            // Initialize the JavaScript bridge
            jsBridge.initialize()
                .thenAccept(v -> {
                    LOG.info("Bridge initialized successfully");
                    initialized.set(true);
                    initializing.set(false);
                    future.complete(null);
                })
                .exceptionally(e -> {
                    LOG.error("Failed to initialize bridge", e);
                    initializing.set(false);
                    future.completeExceptionally(e);
                    return null;
                });
        } catch (Exception e) {
            LOG.error("Failed to create JavaScript bridge", e);
            initializing.set(false);
            future.completeExceptionally(e);
        }
        
        return future;
    }
    
    /**
     * Execute a task.
     * @param taskInput The task input
     * @param options The options
     * @return A future that completes with the task result
     */
    public CompletableFuture<String> executeTask(String taskInput, String options) {
        LOG.info("Executing task: " + taskInput);
        
        if (!initialized.get()) {
            LOG.warn("Bridge not initialized");
            return CompletableFuture.failedFuture(new IllegalStateException("Bridge not initialized"));
        }
        
        return jsBridge.executeTask(taskInput, options);
    }
    
    /**
     * Cancel the current task.
     * @return A future that completes when the task is cancelled
     */
    public CompletableFuture<Void> cancelTask() {
        LOG.info("Cancelling task");
        
        if (!initialized.get()) {
            LOG.warn("Bridge not initialized");
            return CompletableFuture.failedFuture(new IllegalStateException("Bridge not initialized"));
        }
        
        return jsBridge.cancelTask();
    }
    
    /**
     * Get the status of the current task.
     * @return A future that completes with the task status
     */
    public CompletableFuture<String> getTaskStatus() {
        LOG.info("Getting task status");
        
        if (!initialized.get()) {
            LOG.warn("Bridge not initialized");
            return CompletableFuture.failedFuture(new IllegalStateException("Bridge not initialized"));
        }
        
        return jsBridge.getTaskStatus();
    }
    
    /**
     * Dispose the bridge.
     */
    private void dispose() {
        LOG.info("Disposing bridge");
        
        if (jsBridge != null) {
            jsBridge.dispose();
            jsBridge = null;
        }
        
        initialized.set(false);
        initializing.set(false);
    }
}