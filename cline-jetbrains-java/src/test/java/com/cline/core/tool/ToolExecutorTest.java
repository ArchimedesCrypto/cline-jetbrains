package com.cline.core.tool;

import com.cline.services.ClineSettingsService;
import com.google.gson.JsonObject;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

public class ToolExecutorTest extends BasePlatformTestCase {

    private ToolExecutor toolExecutor;
    private Tool mockTool;
    private ClineSettingsService mockSettingsService;

    @BeforeEach
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        toolExecutor = ToolExecutor.getInstance(getProject());
        mockTool = Mockito.mock(Tool.class);
        mockSettingsService = Mockito.mock(ClineSettingsService.class);

        // Mock the settings service retrieval if needed (depends on ToolExecutor implementation)
        // For now, assume ToolExecutor gets it directly or it's injected elsewhere

        // Register the mock tool
        when(mockTool.getName()).thenReturn("mockTool");
        when(mockTool.validateArgs(any(JsonObject.class))).thenReturn(true);
        toolExecutor.registerTool(mockTool);

        // Mock settings for auto-approval
        when(mockSettingsService.isAutoApproveEnabled()).thenReturn(false); // Default: manual approval
        when(mockSettingsService.isToolAutoApproved("mockTool")).thenReturn(false);
        // Inject mock settings service if ToolExecutor uses it directly
        // This might require modifying ToolExecutor or using dependency injection framework
    }

    @Test
    public void testExecuteToolSuccess_ManualApproval() throws ExecutionException, InterruptedException {
        // Tool requires manual approval (default mock setup)
        JsonObject args = new JsonObject();
        ToolResult result = toolExecutor.executeTool("mockTool", args).get();

        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().contains("requires manual approval"));
    }

     @Test
    public void testExecuteToolSuccess_AutoApproved() throws ExecutionException, InterruptedException {
        // Configure mock settings for auto-approval
        when(mockSettingsService.isAutoApproveEnabled()).thenReturn(true);
        when(mockSettingsService.isToolAutoApproved("mockTool")).thenReturn(true);

        // Mock tool execution result
        JsonObject resultJson = new JsonObject();
        resultJson.addProperty("data", "Success!");
        when(mockTool.execute(any(JsonObject.class)))
                .thenReturn(CompletableFuture.completedFuture(ToolResult.success("Executed", resultJson)));

        JsonObject args = new JsonObject();
        ToolResult result = toolExecutor.executeTool("mockTool", args).get();

        assertTrue(result.isSuccess());
        assertEquals("Executed", result.getMessage());
        assertEquals("Success!", result.getData().get("data").getAsString());
    }

    @Test
    public void testExecuteToolNotFound() throws ExecutionException, InterruptedException {
        JsonObject args = new JsonObject();
        ToolResult result = toolExecutor.executeTool("nonExistentTool", args).get();

        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().contains("Tool not found"));
    }

    @Test
    public void testExecuteToolInvalidArgs() throws ExecutionException, InterruptedException {
        when(mockTool.validateArgs(any(JsonObject.class))).thenReturn(false);
        when(mockTool.getValidationErrorMessage(any(JsonObject.class))).thenReturn("Invalid args");

        JsonObject args = new JsonObject();
        ToolResult result = toolExecutor.executeTool("mockTool", args).get();

        assertFalse(result.isSuccess());
        assertEquals("Invalid args", result.getMessage());
    }

    @Test
    public void testExecuteToolExecutionError() throws ExecutionException, InterruptedException {
        // Configure mock settings for auto-approval to allow execution attempt
        when(mockSettingsService.isAutoApproveEnabled()).thenReturn(true);
        when(mockSettingsService.isToolAutoApproved("mockTool")).thenReturn(true);

        when(mockTool.execute(any(JsonObject.class)))
                .thenReturn(CompletableFuture.failedFuture(new RuntimeException("Execution failed")));

        JsonObject args = new JsonObject();
        ToolResult result = toolExecutor.executeTool("mockTool", args).get();

        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().contains("Error executing tool"));
        assertTrue(result.getMessage().contains("Execution failed"));
    }
}