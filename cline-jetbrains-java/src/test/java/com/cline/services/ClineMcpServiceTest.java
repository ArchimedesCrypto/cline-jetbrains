package com.cline.services;

import com.cline.services.mcp.McpResource;
import com.cline.services.mcp.McpServer;
import com.cline.services.mcp.McpTool;
import com.google.gson.JsonObject;
import com.intellij.openapi.project.Project;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the ClineMcpService class.
 */
public class ClineMcpServiceTest {
    
    private ClineMcpService mcpService;
    
    @Mock
    private Project project;
    
    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        mcpService = new ClineMcpService(project);
    }
    
    @Test
    public void testGetServers() {
        List<McpServer> servers = mcpService.getServers();
        
        assertNotNull(servers);
        assertEquals(1, servers.size());
        assertEquals("example-server", servers.get(0).getName());
    }
    
    @Test
    public void testGetAllTools() {
        List<McpTool> tools = mcpService.getAllTools();
        
        assertNotNull(tools);
        assertEquals(1, tools.size());
        assertEquals("example-tool", tools.get(0).getName());
    }
    
    @Test
    public void testGetAllResources() {
        List<McpResource> resources = mcpService.getAllResources();
        
        assertNotNull(resources);
        assertEquals(1, resources.size());
        assertEquals("example://resource", resources.get(0).getUri());
    }
    
    @Test
    public void testExecuteTool() throws ExecutionException, InterruptedException {
        JsonObject args = new JsonObject();
        args.addProperty("param", "value");
        
        CompletableFuture<JsonObject> result = mcpService.executeTool("example-server", "example-tool", args);
        JsonObject jsonResult = result.get();
        
        assertNotNull(jsonResult);
        assertTrue(jsonResult.has("result"));
        assertEquals("Tool executed: example-tool", jsonResult.get("result").getAsString());
    }
    
    @Test
    public void testAccessResource() throws ExecutionException, InterruptedException {
        CompletableFuture<String> result = mcpService.accessResource("example-server", "example://resource");
        String content = result.get();
        
        assertNotNull(content);
        assertEquals("Resource content: example://resource", content);
    }
    
    @Test
    public void testExecuteToolWithInvalidServer() {
        JsonObject args = new JsonObject();
        args.addProperty("param", "value");
        
        CompletableFuture<JsonObject> result = mcpService.executeTool("invalid-server", "example-tool", args);
        
        assertThrows(ExecutionException.class, () -> result.get());
    }
    
    @Test
    public void testAccessResourceWithInvalidServer() {
        CompletableFuture<String> result = mcpService.accessResource("invalid-server", "example://resource");
        
        assertThrows(ExecutionException.class, () -> result.get());
    }
}