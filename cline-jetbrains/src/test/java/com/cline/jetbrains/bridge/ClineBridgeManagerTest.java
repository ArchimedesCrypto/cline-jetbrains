package com.cline.jetbrains.bridge;

import com.cline.jetbrains.services.ClineSettingsService;
import com.intellij.openapi.project.Project;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.concurrent.CompletableFuture;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * Tests for the ClineBridgeManager class.
 */
public class ClineBridgeManagerTest extends BasePlatformTestCase {
    
    private ClineBridgeManager bridgeManager;
    private Project mockProject;
    private JavaScriptBridge mockJsBridge;
    private ClineSettingsService mockSettingsService;
    
    @Override
    public void setUp() throws Exception {
        super.setUp();
        
        // Create mock objects
        mockProject = Mockito.mock(Project.class);
        mockJsBridge = Mockito.mock(JavaScriptBridge.class);
        mockSettingsService = Mockito.mock(ClineSettingsService.class);
        
        // Set up the mock project
        when(mockProject.getName()).thenReturn("Test Project");
        when(mockProject.getService(ClineSettingsService.class)).thenReturn(mockSettingsService);
        
        // Set up the mock JavaScript bridge
        when(mockJsBridge.initialize()).thenReturn(CompletableFuture.completedFuture(null));
        when(mockJsBridge.executeTask(any(), any())).thenReturn(CompletableFuture.completedFuture("Task result"));
        when(mockJsBridge.cancelTask()).thenReturn(CompletableFuture.completedFuture(null));
        when(mockJsBridge.getTaskStatus()).thenReturn(CompletableFuture.completedFuture("Task status"));
        
        // Create the bridge manager
        bridgeManager = new ClineBridgeManager(mockProject);
        
        // Use reflection to set the JavaScript bridge
        java.lang.reflect.Field jsBridgeField = ClineBridgeManager.class.getDeclaredField("jsBridge");
        jsBridgeField.setAccessible(true);
        jsBridgeField.set(bridgeManager, mockJsBridge);
        
        // Use reflection to set the initialized flag
        java.lang.reflect.Field initializedField = ClineBridgeManager.class.getDeclaredField("initialized");
        initializedField.setAccessible(true);
        initializedField.set(bridgeManager, new java.util.concurrent.atomic.AtomicBoolean(true));
    }
    
    @Test
    public void testGetInstance() {
        // Get the instance
        ClineBridgeManager instance = ClineBridgeManager.getInstance(mockProject);
        
        // Verify that the instance is not null
        assertNotNull("Instance should not be null", instance);
    }
    
    @Test
    public void testInitialize() throws Exception {
        // Initialize the bridge
        CompletableFuture<Void> future = bridgeManager.initialize();
        
        // Wait for the future to complete
        future.get();
        
        // Verify that the JavaScript bridge was initialized
        Mockito.verify(mockJsBridge).initialize();
    }
    
    @Test
    public void testExecuteTask() throws Exception {
        // Execute a task
        CompletableFuture<String> future = bridgeManager.executeTask("Test task", "{}");
        
        // Wait for the future to complete
        String result = future.get();
        
        // Verify that the JavaScript bridge was called
        Mockito.verify(mockJsBridge).executeTask("Test task", "{}");
        
        // Verify that the result is correct
        assertEquals("Task result", result);
    }
    
    @Test
    public void testCancelTask() throws Exception {
        // Cancel the task
        CompletableFuture<Void> future = bridgeManager.cancelTask();
        
        // Wait for the future to complete
        future.get();
        
        // Verify that the JavaScript bridge was called
        Mockito.verify(mockJsBridge).cancelTask();
    }
    
    @Test
    public void testGetTaskStatus() throws Exception {
        // Get the task status
        CompletableFuture<String> future = bridgeManager.getTaskStatus();
        
        // Wait for the future to complete
        String status = future.get();
        
        // Verify that the JavaScript bridge was called
        Mockito.verify(mockJsBridge).getTaskStatus();
        
        // Verify that the status is correct
        assertEquals("Task status", status);
    }
}