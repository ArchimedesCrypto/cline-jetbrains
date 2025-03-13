package com.cline.services.mcp;

import com.google.gson.JsonObject;

/**
 * Represents a tool provided by an MCP server.
 */
public class McpTool {
    private final String name;
    private final String description;
    private final JsonObject inputSchema;
    
    /**
     * Create a new MCP tool.
     *
     * @param name The tool name
     * @param description The tool description
     * @param inputSchema The input schema
     */
    public McpTool(String name, String description, JsonObject inputSchema) {
        this.name = name;
        this.description = description;
        this.inputSchema = inputSchema;
    }
    
    /**
     * Get the tool name.
     *
     * @return The tool name
     */
    public String getName() {
        return name;
    }
    
    /**
     * Get the tool description.
     *
     * @return The tool description
     */
    public String getDescription() {
        return description;
    }
    
    /**
     * Get the input schema.
     *
     * @return The input schema
     */
    public JsonObject getInputSchema() {
        return inputSchema;
    }
}