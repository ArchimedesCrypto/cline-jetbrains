package com.cline.services.tools;

import java.util.HashMap;
import java.util.Map;

/**
 * Settings for auto-approval of tools.
 */
public class AutoApprovalSettings {
    private final boolean enabled;
    private final int maxConsecutiveRequests;
    private final Map<String, Boolean> toolSettings;
    
    /**
     * Create auto-approval settings with default values.
     */
    public AutoApprovalSettings() {
        this(false, 3, new HashMap<>());
    }
    
    /**
     * Create auto-approval settings with the specified values.
     *
     * @param enabled Whether auto-approval is enabled
     * @param maxConsecutiveRequests The maximum number of consecutive auto-approved requests
     * @param toolSettings The tool-specific settings
     */
    public AutoApprovalSettings(boolean enabled, int maxConsecutiveRequests, Map<String, Boolean> toolSettings) {
        this.enabled = enabled;
        this.maxConsecutiveRequests = maxConsecutiveRequests;
        this.toolSettings = toolSettings;
    }
    
    /**
     * Check if auto-approval is enabled.
     *
     * @return True if auto-approval is enabled, false otherwise
     */
    public boolean isEnabled() {
        return enabled;
    }
    
    /**
     * Get the maximum number of consecutive auto-approved requests.
     *
     * @return The maximum number of consecutive auto-approved requests
     */
    public int getMaxConsecutiveRequests() {
        return maxConsecutiveRequests;
    }
    
    /**
     * Get the tool-specific settings.
     *
     * @return The tool-specific settings
     */
    public Map<String, Boolean> getToolSettings() {
        return toolSettings;
    }
    
    /**
     * Check if a tool should be auto-approved.
     *
     * @param toolName The tool name
     * @return True if the tool should be auto-approved, false otherwise
     */
    public boolean shouldAutoApprove(String toolName) {
        return enabled && toolSettings.getOrDefault(toolName, false);
    }
    
    /**
     * Create a new builder for auto-approval settings.
     *
     * @return A new builder
     */
    public static Builder builder() {
        return new Builder();
    }
    
    /**
     * Builder for auto-approval settings.
     */
    public static class Builder {
        private boolean enabled = false;
        private int maxConsecutiveRequests = 3;
        private Map<String, Boolean> toolSettings = new HashMap<>();
        
        /**
         * Set whether auto-approval is enabled.
         *
         * @param enabled True if auto-approval is enabled, false otherwise
         * @return This builder
         */
        public Builder enabled(boolean enabled) {
            this.enabled = enabled;
            return this;
        }
        
        /**
         * Set the maximum number of consecutive auto-approved requests.
         *
         * @param maxConsecutiveRequests The maximum number of consecutive auto-approved requests
         * @return This builder
         */
        public Builder maxConsecutiveRequests(int maxConsecutiveRequests) {
            this.maxConsecutiveRequests = maxConsecutiveRequests;
            return this;
        }
        
        /**
         * Set the tool-specific settings.
         *
         * @param toolSettings The tool-specific settings
         * @return This builder
         */
        public Builder toolSettings(Map<String, Boolean> toolSettings) {
            this.toolSettings = toolSettings;
            return this;
        }
        
        /**
         * Set whether a tool should be auto-approved.
         *
         * @param toolName The tool name
         * @param autoApprove True if the tool should be auto-approved, false otherwise
         * @return This builder
         */
        public Builder toolSetting(String toolName, boolean autoApprove) {
            this.toolSettings.put(toolName, autoApprove);
            return this;
        }
        
        /**
         * Build the auto-approval settings.
         *
         * @return The auto-approval settings
         */
        public AutoApprovalSettings build() {
            return new AutoApprovalSettings(enabled, maxConsecutiveRequests, toolSettings);
        }
    }
}