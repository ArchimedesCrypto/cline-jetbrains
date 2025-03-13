package com.cline.services.mcp;

import java.util.HashMap;
import java.util.Map;

/**
 * Configuration for an MCP server.
 */
public class McpServerConfig {
    private final String command;
    private final String[] args;
    private final Map<String, String> env;
    private final boolean disabled;
    private final String[] alwaysAllow;
    
    /**
     * Create a new MCP server configuration.
     *
     * @param command The command to run
     * @param args The command arguments
     * @param env The environment variables
     * @param disabled Whether the server is disabled
     * @param alwaysAllow The tools to always allow
     */
    public McpServerConfig(String command, String[] args, Map<String, String> env, boolean disabled, String[] alwaysAllow) {
        this.command = command;
        this.args = args;
        this.env = env;
        this.disabled = disabled;
        this.alwaysAllow = alwaysAllow;
    }
    
    /**
     * Get the command to run.
     *
     * @return The command
     */
    public String getCommand() {
        return command;
    }
    
    /**
     * Get the command arguments.
     *
     * @return The arguments
     */
    public String[] getArgs() {
        return args;
    }
    
    /**
     * Get the environment variables.
     *
     * @return The environment variables
     */
    public Map<String, String> getEnv() {
        return env;
    }
    
    /**
     * Check if the server is disabled.
     *
     * @return True if the server is disabled, false otherwise
     */
    public boolean isDisabled() {
        return disabled;
    }
    
    /**
     * Get the tools to always allow.
     *
     * @return The tools to always allow
     */
    public String[] getAlwaysAllow() {
        return alwaysAllow;
    }
    
    /**
     * Create a new builder for MCP server configuration.
     *
     * @return A new builder
     */
    public static Builder builder() {
        return new Builder();
    }
    
    /**
     * Builder for MCP server configuration.
     */
    public static class Builder {
        private String command;
        private String[] args = new String[0];
        private Map<String, String> env = new HashMap<>();
        private boolean disabled = false;
        private String[] alwaysAllow = new String[0];
        
        /**
         * Set the command to run.
         *
         * @param command The command
         * @return This builder
         */
        public Builder command(String command) {
            this.command = command;
            return this;
        }
        
        /**
         * Set the command arguments.
         *
         * @param args The arguments
         * @return This builder
         */
        public Builder args(String... args) {
            this.args = args;
            return this;
        }
        
        /**
         * Set the environment variables.
         *
         * @param env The environment variables
         * @return This builder
         */
        public Builder env(Map<String, String> env) {
            this.env = env;
            return this;
        }
        
        /**
         * Add an environment variable.
         *
         * @param key The key
         * @param value The value
         * @return This builder
         */
        public Builder env(String key, String value) {
            this.env.put(key, value);
            return this;
        }
        
        /**
         * Set whether the server is disabled.
         *
         * @param disabled True if the server is disabled, false otherwise
         * @return This builder
         */
        public Builder disabled(boolean disabled) {
            this.disabled = disabled;
            return this;
        }
        
        /**
         * Set the tools to always allow.
         *
         * @param alwaysAllow The tools to always allow
         * @return This builder
         */
        public Builder alwaysAllow(String... alwaysAllow) {
            this.alwaysAllow = alwaysAllow;
            return this;
        }
        
        /**
         * Build the MCP server configuration.
         *
         * @return The configuration
         */
        public McpServerConfig build() {
            return new McpServerConfig(command, args, env, disabled, alwaysAllow);
        }
    }
}