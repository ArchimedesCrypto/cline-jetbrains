package com.cline.jetbrains.services;

import com.cline.jetbrains.bridge.ClineBridgeManager;
import com.cline.jetbrains.ui.components.ClineTaskListPanel;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Service(Service.Level.PROJECT)
public final class ClineTaskExecutionService {
    private static final Logger LOG = Logger.getInstance(ClineTaskExecutionService.class);
    
    private final Project project;
    private final ClineBridgeManager bridgeManager;
    private final ClineSettingsService settingsService;
    
    public ClineTaskExecutionService(@NotNull Project project) {
        this.project = project;
        this.bridgeManager = ClineBridgeManager.getInstance(project);
        this.settingsService = ClineSettingsService.getInstance();
        
        LOG.info("ClineTaskExecutionService created for project: " + project.getName());
    }
    
    public static ClineTaskExecutionService getInstance(@NotNull Project project) {
        return project.getService(ClineTaskExecutionService.class);
    }
    
    public CompletableFuture<String> executeTask(String taskInput, String model) {
        LOG.info("Executing task: " + taskInput);
        
        String taskId = generateTaskId();
        
        return bridgeManager.initialize()
            .thenCompose(v -> {
                Map<String, Object> options = new HashMap<>();
                options.put("model", model);
                return bridgeManager.executeTask(taskInput, options.toString());
            })
            .thenApply(result -> {
                return taskId;
            })
            .exceptionally(e -> {
                LOG.error("Failed to execute task", e);
                return taskId;
            });
    }
    
    public CompletableFuture<Void> cancelTask(String taskId) {
        LOG.info("Cancelling task: " + taskId);
        
        return bridgeManager.cancelTask()
            .exceptionally(e -> {
                LOG.error("Failed to cancel task", e);
                return null;
            });
    }
    
    public ClineTaskListPanel.TaskItem createTaskItem(String taskId, String description) {
        LOG.info("Creating task item: " + taskId);
        
        return new ClineTaskListPanel.TaskItem(
            taskId,
            description,
            java.time.LocalDateTime.now().toString()
        );
    }
    
    private String generateTaskId() {
        return "task_" + UUID.randomUUID().toString().substring(0, 8);
    }
}