package com.cline.services;

import com.cline.services.browser.BrowserSettings;
import com.cline.services.tools.AutoApprovalSettings;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.*;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

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
    // Anthropic settings
    private String apiKey = "";
    private String apiEndpoint = "https://api.anthropic.com/v1";
    private String model = "claude-3-opus-20240229";
    
    // OpenAI settings
    private String openAiApiKey = "";
    private String openAiApiEndpoint = "https://api.openai.com/v1";
    private String openAiModel = "gpt-4";
    private boolean azureOpenAi = false;
    private String azureApiVersion = "2023-05-15";
    
    // Browser settings
    private int browserWidth = 900;
    private int browserHeight = 600;
    private boolean browserHeadless = false;
    private String browserUserAgent = "";
    private int browserTimeout = 30000;
    
    // Auto-approval settings
    private boolean autoApprovalEnabled = false;
    private int autoApprovalMaxConsecutiveRequests = 3;
    private Map<String, Boolean> autoApprovalToolSettings = new HashMap<>();
    
    // General settings
    private String apiProvider = "anthropic"; // Default to Anthropic
    private int maxTokens = 4000;
    private boolean enableBrowser = true;
    private boolean enableTerminal = true;
    private boolean enableFileAccess = true;
    private boolean enablePromptCaching = true;

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

    // Anthropic getters and setters
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

    // OpenAI getters and setters
    public String getOpenAiApiKey() {
        return openAiApiKey;
    }

    public void setOpenAiApiKey(String openAiApiKey) {
        this.openAiApiKey = openAiApiKey;
    }

    public String getOpenAiApiEndpoint() {
        return openAiApiEndpoint;
    }

    public void setOpenAiApiEndpoint(String openAiApiEndpoint) {
        this.openAiApiEndpoint = openAiApiEndpoint;
    }

    public String getOpenAiModel() {
        return openAiModel;
    }

    public void setOpenAiModel(String openAiModel) {
        this.openAiModel = openAiModel;
    }

    public boolean isAzureOpenAi() {
        return azureOpenAi;
    }

    public void setAzureOpenAi(boolean azureOpenAi) {
        this.azureOpenAi = azureOpenAi;
    }

    public String getAzureApiVersion() {
        return azureApiVersion;
    }

    public void setAzureApiVersion(String azureApiVersion) {
        this.azureApiVersion = azureApiVersion;
    }

    // Browser settings getters and setters
    public int getBrowserWidth() {
        return browserWidth;
    }

    public void setBrowserWidth(int browserWidth) {
        this.browserWidth = browserWidth;
    }

    public int getBrowserHeight() {
        return browserHeight;
    }

    public void setBrowserHeight(int browserHeight) {
        this.browserHeight = browserHeight;
    }

    public boolean isBrowserHeadless() {
        return browserHeadless;
    }

    public void setBrowserHeadless(boolean browserHeadless) {
        this.browserHeadless = browserHeadless;
    }

    public String getBrowserUserAgent() {
        return browserUserAgent;
    }

    public void setBrowserUserAgent(String browserUserAgent) {
        this.browserUserAgent = browserUserAgent;
    }

    public int getBrowserTimeout() {
        return browserTimeout;
    }

    public void setBrowserTimeout(int browserTimeout) {
        this.browserTimeout = browserTimeout;
    }

    /**
     * Get the browser settings.
     *
     * @return The browser settings
     */
    public BrowserSettings getBrowserSettings() {
        return BrowserSettings.builder()
                .width(browserWidth)
                .height(browserHeight)
                .headless(browserHeadless)
                .userAgent(browserUserAgent.isEmpty() ? null : browserUserAgent)
                .timeout(browserTimeout)
                .build();
    }
    
    // Auto-approval settings getters and setters
    public boolean isAutoApprovalEnabled() {
        return autoApprovalEnabled;
    }
    
    public void setAutoApprovalEnabled(boolean autoApprovalEnabled) {
        this.autoApprovalEnabled = autoApprovalEnabled;
    }
    
    public int getAutoApprovalMaxConsecutiveRequests() {
        return autoApprovalMaxConsecutiveRequests;
    }
    
    public void setAutoApprovalMaxConsecutiveRequests(int autoApprovalMaxConsecutiveRequests) {
        this.autoApprovalMaxConsecutiveRequests = autoApprovalMaxConsecutiveRequests;
    }
    
    public Map<String, Boolean> getAutoApprovalToolSettings() {
        return autoApprovalToolSettings;
    }
    
    public void setAutoApprovalToolSettings(Map<String, Boolean> autoApprovalToolSettings) {
        this.autoApprovalToolSettings = autoApprovalToolSettings;
    }
    
    /**
     * Get the auto-approval settings.
     *
     * @return The auto-approval settings
     */
    public AutoApprovalSettings getAutoApprovalSettings() {
        return AutoApprovalSettings.builder()
                .enabled(autoApprovalEnabled)
                .maxConsecutiveRequests(autoApprovalMaxConsecutiveRequests)
                .toolSettings(autoApprovalToolSettings)
                .build();
    }

    // General getters and setters
    public String getApiProvider() {
        return apiProvider;
    }

    public void setApiProvider(String apiProvider) {
        this.apiProvider = apiProvider;
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

    public boolean isEnablePromptCaching() {
        return enablePromptCaching;
    }

    public void setEnablePromptCaching(boolean enablePromptCaching) {
        this.enablePromptCaching = enablePromptCaching;
    }
}