package com.cline.jetbrains.services;

import com.cline.jetbrains.bridge.ClineBridgeManager;
import com.cline.jetbrains.ui.components.ClineTaskListPanel;
import com.intellij.openapi.project.Project;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.concurrent.CompletableFuture;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

public class ClineTaskExecutionServiceTest extends BasePlatformTestCase {
    
    private ClineTaskExecutionService taskExecutionService;
    private ClineBridgeManager mockBridgeManager;
    private Project mockProject;
    
    @Override
    public void setUp() throws Exception {
        super.setUp();
        
        // Create mock objects
        mockProject = Mockito.mock(Project.class);
        mockBridgeManager = Mockito.mock(ClineBridgeManager.class);
        
        // Set up the mock bridge manager
        when(mockBridgeManager.initialize()).thenReturn(CompletableFuture.completedFuture(null));
        when(mockBridgeManager.executeTask(anyString(), anyString())).thenReturn(CompletableFuture.completedFuture("Task result"));
        when(mockBridgeManager.cancelTask()).thenReturn(CompletableFuture.completedFuture(null));
        when(mockBridgeManager.getTaskStatus()).thenReturn(CompletableFuture.completedFuture("completed"));
        
        // Set up the mock project
        when(mockProject.getService(ClineBridgeManager.class)).thenReturn(mockBridgeManager);
        when(mockProject.getName()).thenReturn("Test Project");
        
        // Create the task execution service
        taskExecutionService = new ClineTaskExecutionService(mockProject);
    }
    
    @Test
    public void testExecuteTask() throws Exception {
        // Execute a task
        CompletableFuture<String> future = taskExecutionService.executeTask("Test task", "gpt-4");
        
        // Wait for the task to complete
        String taskId = future.get();
        
        // Verify that the task ID is not null
        assertNotNull("Task ID should not be null", taskId);
        
        // Verify that the bridge manager was called
        Mockito.verify(mockBridgeManager).initialize();
        Mockito.verify(mockBridgeManager).executeTask(anyString(), anyString());
    }
    
    @Test
    public void testCancelTask() throws Exception {
        // Execute a task
        CompletableFuture<String> future = taskExecutionService.executeTask("Test task", "gpt-4");
        
        // Wait for the task to complete
        String taskId = future.get();
        
        // Cancel the task
        CompletableFuture<Void> cancelFuture = taskExecutionService.cancelTask(taskId);
        
        // Wait for the cancellation to complete
        cancelFuture.get();
        
        // Verify that the bridge manager was called
        Mockito.verify(mockBridgeManager).cancelTask();
    }
    
    @Test
    public void testCreateTaskItem() throws Exception {
        // Execute a task
        CompletableFuture<String> future = taskExecutionService.executeTask("Test task", "gpt-4");
        
        // Wait for the task to complete
        String taskId = future.get();
        
        // Create a task item
        ClineTaskListPanel.TaskItem taskItem = taskExecutionService.createTaskItem(taskId, "Test task");
        
        // Verify that the task item is not null
        assertNotNull("Task item should not be null", taskItem);
        
        // Verify that the task item has the correct ID and description
        assertEquals("Task ID should match", taskId, taskItem.getId());
        assertEquals("Task description should match", "Test task", taskItem.getDescription());
    }
}