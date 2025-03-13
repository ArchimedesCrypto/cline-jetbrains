package com.cline.services.mcp;

import com.google.gson.JsonObject;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Interface for MCP servers.
 */
public interface McpServer {
    /**
     * Get the name of the server.
     *
     * @return The server name
     */
    String getName();
    
    /**
     * Get the tools provided by the server.
     *
     * @return The tools
     */
    List<McpTool> getTools();
    
    /**
     * Get the resources provided by the server.
     *
     * @return The resources
     */
    List<McpResource> getResources();
    
    /**
     * Execute a tool.
     *
     * @param toolName The name of the tool
     * @param args The tool arguments
     * @return A CompletableFuture containing the tool result
     */
    CompletableFuture<JsonObject> executeTool(String toolName, JsonObject args);
    
    /**
     * Access a resource.
     *
     * @param uri The resource URI
     * @return A CompletableFuture containing the resource content
     */
    CompletableFuture<String> accessResource(String uri);
    
    /**
     * Check if the server is running.
     *
     * @return True if the server is running, false otherwise
     */
    boolean isRunning();
    
    /**
     * Start the server.
     *
     * @return A CompletableFuture that completes when the server is started
     */
    CompletableFuture<Void> start();
    
    /**
     * Stop the server.
     *
     * @return A CompletableFuture that completes when the server is stopped
     */
    CompletableFuture<Void> stop();
}