package com.cline.services.mcp;

/**
 * Represents a resource provided by an MCP server.
 */
public class McpResource {
    private final String uri;
    private final String name;
    private final String mimeType;
    private final String description;
    
    /**
     * Create a new MCP resource.
     *
     * @param uri The resource URI
     * @param name The resource name
     * @param mimeType The MIME type
     * @param description The resource description
     */
    public McpResource(String uri, String name, String mimeType, String description) {
        this.uri = uri;
        this.name = name;
        this.mimeType = mimeType;
        this.description = description;
    }
    
    /**
     * Get the resource URI.
     *
     * @return The resource URI
     */
    public String getUri() {
        return uri;
    }
    
    /**
     * Get the resource name.
     *
     * @return The resource name
     */
    public String getName() {
        return name;
    }
    
    /**
     * Get the MIME type.
     *
     * @return The MIME type
     */
    public String getMimeType() {
        return mimeType;
    }
    
    /**
     * Get the resource description.
     *
     * @return The resource description
     */
    public String getDescription() {
        return description;
    }
}