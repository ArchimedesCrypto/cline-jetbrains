package com.cline.services;

import com.cline.services.mcp.McpHub;
import com.cline.services.mcp.McpResource;
import com.cline.services.mcp.McpServer;
import com.cline.services.mcp.McpServerConfig;
import com.cline.services.mcp.McpTool;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Service for MCP operations in the Cline plugin.
 */
@Service
public final class ClineMcpService {
    private static final Logger LOG = Logger.getInstance(ClineMcpService.class);
    private static final String MCP_SETTINGS_FILE = "cline_mcp_settings.json";
    
    private final Project project;
    private final McpHub mcpHub;
    private final Gson gson;
    
    /**
     * Creates a new MCP service.
     *
     * @param project The project
     */
    public ClineMcpService(Project project) {
        this.project = project;
        this.mcpHub = new McpHub();
        this.gson = new Gson();
        
        // Load MCP servers from settings
        loadMcpServers();
    }
    
    /**
     * Gets the MCP service instance.
     *
     * @param project The project
     * @return The MCP service instance
     */
    public static ClineMcpService getInstance(@NotNull Project project) {
        return project.getService(ClineMcpService.class);
    }
    
    /**
     * Load MCP servers from settings.
     */
    private void loadMcpServers() {
        try {
            // In a real implementation, we would load the settings from the user's settings directory
            // For now, we'll just create a stub implementation
            
            // Register a stub server
            mcpHub.registerServer("example-server", McpServerConfig.builder()
                    .command("node")
                    .args("example-server.js")
                    .env("API_KEY", "test-api-key")
                    .build());
        } catch (Exception e) {
            LOG.error("Error loading MCP servers", e);
        }
    }
    
    /**
     * Get all servers.
     *
     * @return The servers
     */
    public List<McpServer> getServers() {
        return mcpHub.getServers();
    }
    
    /**
     * Get all tools from all servers.
     *
     * @return The tools
     */
    public List<McpTool> getAllTools() {
        return mcpHub.getAllTools();
    }
    
    /**
     * Get all resources from all servers.
     *
     * @return The resources
     */
    public List<McpResource> getAllResources() {
        return mcpHub.getAllResources();
    }
    
    /**
     * Execute a tool.
     *
     * @param serverName The server name
     * @param toolName The tool name
     * @param args The tool arguments
     * @return A CompletableFuture containing the tool result
     */
    public CompletableFuture<JsonObject> executeTool(String serverName, String toolName, JsonObject args) {
        LOG.info("Executing MCP tool: " + serverName + "/" + toolName);
        return mcpHub.executeTool(serverName, toolName, args);
    }
    
    /**
     * Access a resource.
     *
     * @param serverName The server name
     * @param uri The resource URI
     * @return A CompletableFuture containing the resource content
     */
    public CompletableFuture<String> accessResource(String serverName, String uri) {
        LOG.info("Accessing MCP resource: " + serverName + "/" + uri);
        return mcpHub.accessResource(serverName, uri);
    }
}