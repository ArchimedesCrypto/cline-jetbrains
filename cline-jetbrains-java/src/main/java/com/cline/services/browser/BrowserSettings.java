package com.cline.services.browser;

/**
 * Settings for the browser.
 */
public class BrowserSettings {
    private final int width;
    private final int height;
    private final boolean headless;
    private final String userAgent;
    private final int timeout;
    
    /**
     * Create browser settings with default values.
     */
    public BrowserSettings() {
        this(900, 600, false, null, 30000);
    }
    
    /**
     * Create browser settings with the specified values.
     *
     * @param width The browser width
     * @param height The browser height
     * @param headless Whether to run in headless mode
     * @param userAgent The user agent string
     * @param timeout The timeout in milliseconds
     */
    public BrowserSettings(int width, int height, boolean headless, String userAgent, int timeout) {
        this.width = width;
        this.height = height;
        this.headless = headless;
        this.userAgent = userAgent;
        this.timeout = timeout;
    }
    
    /**
     * Get the browser width.
     *
     * @return The browser width
     */
    public int getWidth() {
        return width;
    }
    
    /**
     * Get the browser height.
     *
     * @return The browser height
     */
    public int getHeight() {
        return height;
    }
    
    /**
     * Check if the browser should run in headless mode.
     *
     * @return True if the browser should run in headless mode, false otherwise
     */
    public boolean isHeadless() {
        return headless;
    }
    
    /**
     * Get the user agent string.
     *
     * @return The user agent string
     */
    public String getUserAgent() {
        return userAgent;
    }
    
    /**
     * Get the timeout in milliseconds.
     *
     * @return The timeout in milliseconds
     */
    public int getTimeout() {
        return timeout;
    }
    
    /**
     * Create a new builder for browser settings.
     *
     * @return A new builder
     */
    public static Builder builder() {
        return new Builder();
    }
    
    /**
     * Builder for browser settings.
     */
    public static class Builder {
        private int width = 900;
        private int height = 600;
        private boolean headless = false;
        private String userAgent = null;
        private int timeout = 30000;
        
        /**
         * Set the browser width.
         *
         * @param width The browser width
         * @return This builder
         */
        public Builder width(int width) {
            this.width = width;
            return this;
        }
        
        /**
         * Set the browser height.
         *
         * @param height The browser height
         * @return This builder
         */
        public Builder height(int height) {
            this.height = height;
            return this;
        }
        
        /**
         * Set whether the browser should run in headless mode.
         *
         * @param headless True if the browser should run in headless mode, false otherwise
         * @return This builder
         */
        public Builder headless(boolean headless) {
            this.headless = headless;
            return this;
        }
        
        /**
         * Set the user agent string.
         *
         * @param userAgent The user agent string
         * @return This builder
         */
        public Builder userAgent(String userAgent) {
            this.userAgent = userAgent;
            return this;
        }
        
        /**
         * Set the timeout in milliseconds.
         *
         * @param timeout The timeout in milliseconds
         * @return This builder
         */
        public Builder timeout(int timeout) {
            this.timeout = timeout;
            return this;
        }
        
        /**
         * Build the browser settings.
         *
         * @return The browser settings
         */
        public BrowserSettings build() {
            return new BrowserSettings(width, height, headless, userAgent, timeout);
        }
    }
}