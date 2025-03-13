package com.cline.services.api;

/**
 * Class for storing model information.
 */
public class ModelInfo {
    private final String id;
    private final String name;
    private final int maxTokens;
    private final boolean supportsPromptCaching;

    public ModelInfo(String id, String name, int maxTokens, boolean supportsPromptCaching) {
        this.id = id;
        this.name = name;
        this.maxTokens = maxTokens;
        this.supportsPromptCaching = supportsPromptCaching;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getMaxTokens() {
        return maxTokens;
    }

    public boolean supportsPromptCaching() {
        return supportsPromptCaching;
    }
}