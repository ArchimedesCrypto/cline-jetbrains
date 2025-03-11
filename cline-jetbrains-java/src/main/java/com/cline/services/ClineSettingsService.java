package com.cline.services;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.*;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Service for storing and retrieving Cline plugin settings.
 */
@State(
    name = "ClineSettings",
    storages = {
        @Storage("cline-settings.xml")
    }
)
public class ClineSettingsService implements PersistentStateComponent<ClineSettingsService> {
    private String apiKey = "";
    private String apiEndpoint = "https://api.anthropic.com/v1";
    private String model = "claude-3-opus-20240229";
    private int maxTokens = 4000;
    private boolean enableBrowser = true;
    private boolean enableTerminal = true;
    private boolean enableFileAccess = true;

    public static ClineSettingsService getInstance() {
        return ApplicationManager.getApplication().getService(ClineSettingsService.class);
    }

    @Nullable
    @Override
    public ClineSettingsService getState() {
        return this;
    }

    @Override
    public void loadState(@NotNull ClineSettingsService state) {
        XmlSerializerUtil.copyBean(state, this);
    }

    // Getters and setters
    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public String getApiEndpoint() {
        return apiEndpoint;
    }

    public void setApiEndpoint(String apiEndpoint) {
        this.apiEndpoint = apiEndpoint;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public int getMaxTokens() {
        return maxTokens;
    }

    public void setMaxTokens(int maxTokens) {
        this.maxTokens = maxTokens;
    }

    public boolean isEnableBrowser() {
        return enableBrowser;
    }

    public void setEnableBrowser(boolean enableBrowser) {
        this.enableBrowser = enableBrowser;
    }

    public boolean isEnableTerminal() {
        return enableTerminal;
    }

    public void setEnableTerminal(boolean enableTerminal) {
        this.enableTerminal = enableTerminal;
    }

    public boolean isEnableFileAccess() {
        return enableFileAccess;
    }

    public void setEnableFileAccess(boolean enableFileAccess) {
        this.enableFileAccess = enableFileAccess;
    }
}