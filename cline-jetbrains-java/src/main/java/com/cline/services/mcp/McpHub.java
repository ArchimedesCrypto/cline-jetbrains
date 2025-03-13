package com.cline.services.mcp;

import com.google.gson.JsonObject;
import com.intellij.openapi.diagnostic.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * Hub for managing MCP servers.
 */
public class McpHub {
    private static final Logger LOG = Logger.getInstance(McpHub.class);
    
    private final Map<String, McpServer> servers = new HashMap<>();
    private final Map<String, McpServerConfig> configs = new HashMap<>();
    
    /**
     * Register a server.
     *
     * @param name The server name
     * @param config The server configuration
     * @return A CompletableFuture that completes when the server is registered
     */
    public CompletableFuture<Void> registerServer(String name, McpServerConfig config) {
        LOG.info("Registering MCP server: " + name);
        configs.put(name, config);
        
        if (config.isDisabled()) {
            LOG.info("MCP server is disabled: " + name);
            return CompletableFuture.completedFuture(null);
        }
        
        // In a real implementation, we would create a real MCP server here
        // For now, we'll just create a stub implementation
        McpServer server = new StubMcpServer(name);
        servers.put(name, server);
        
        return server.start();
    }
    
    /**
     * Unregister a server.
     *
     * @param name The server name
     * @return A CompletableFuture that completes when the server is unregistered
     */
    public CompletableFuture<Void> unregisterServer(String name) {
        LOG.info("Unregistering MCP server: " + name);
        configs.remove(name);
        
        McpServer server = servers.remove(name);
        if (server != null) {
            return server.stop();
        }
        
        return CompletableFuture.completedFuture(null);
    }
    
    /**
     * Get a server by name.
     *
     * @param name The server name
     * @return The server, or null if not found
     */
    public McpServer getServer(String name) {
        return servers.get(name);
    }
    
    /**
     * Get all servers.
     *
     * @return The servers
     */
    public List<McpServer> getServers() {
        return new ArrayList<>(servers.values());
    }
    
    /**
     * Get all tools from all servers.
     *
     * @return The tools
     */
    public List<McpTool> getAllTools() {
        return servers.values().stream()
                .flatMap(server -> server.getTools().stream())
                .collect(Collectors.toList());
    }
    
    /**
     * Get all resources from all servers.
     *
     * @return The resources
     */
    public List<McpResource> getAllResources() {
        return servers.values().stream()
                .flatMap(server -> server.getResources().stream())
                .collect(Collectors.toList());
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
        McpServer server = getServer(serverName);
        if (server == null) {
            return CompletableFuture.failedFuture(new IllegalArgumentException("Server not found: " + serverName));
        }
        
        return server.executeTool(toolName, args);
    }
    
    /**
     * Access a resource.
     *
     * @param serverName The server name
     * @param uri The resource URI
     * @return A CompletableFuture containing the resource content
     */
    public CompletableFuture<String> accessResource(String serverName, String uri) {
        McpServer server = getServer(serverName);
        if (server == null) {
            return CompletableFuture.failedFuture(new IllegalArgumentException("Server not found: " + serverName));
        }
        
        return server.accessResource(uri);
    }
    
    /**
     * Stub implementation of McpServer for testing.
     */
    private static class StubMcpServer implements McpServer {
        private final String name;
        private final List<McpTool> tools = new ArrayList<>();
        private final List<McpResource> resources = new ArrayList<>();
        private boolean running = false;
        
        public StubMcpServer(String name) {
            this.name = name;
            
            // Add some stub tools and resources
            tools.add(new McpTool(
                "example-tool",
                "An example tool",
                new JsonObject()
            ));
            
            resources.add(new McpResource(
                "example://resource",
                "Example Resource",
                "text/plain",
                "An example resource"
            ));
        }
        
        @Override
        public String getName() {
            return name;
        }
        
        @Override
        public List<McpTool> getTools() {
            return tools;
        }
        
        @Override
        public List<McpResource> getResources() {
            return resources;
        }
        
        @Override
        public CompletableFuture<JsonObject> executeTool(String toolName, JsonObject args) {
            JsonObject result = new JsonObject();
            result.addProperty("result", "Tool executed: " + toolName);
            return CompletableFuture.completedFuture(result);
        }
        
        @Override
        public CompletableFuture<String> accessResource(String uri) {
            return CompletableFuture.completedFuture("Resource content: " + uri);
        }
        
        @Override
        public boolean isRunning() {
            return running;
        }
        
        @Override
        public CompletableFuture<Void> start() {
            running = true;
            return CompletableFuture.completedFuture(null);
        }
        
        @Override
        public CompletableFuture<Void> stop() {
            running = false;
            return CompletableFuture.completedFuture(null);
        }
    }
}