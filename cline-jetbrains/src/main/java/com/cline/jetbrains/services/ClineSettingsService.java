package com.cline.jetbrains.services;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Service for storing and retrieving plugin settings.
 * This class is responsible for persisting settings across IDE restarts.
 */
@Service(Service.Level.APP)
@State(
    name = "com.cline.jetbrains.settings.ClineSettingsService",
    storages = @Storage("ClineSettings.xml")
)
public final class ClineSettingsService implements PersistentStateComponent<ClineSettingsService.State> {
    private static final Logger LOG = Logger.getInstance(ClineSettingsService.class);
    
    private State myState = new State();

    /**
     * Get the instance of the settings service.
     * @return The settings service instance
     */
    public static ClineSettingsService getInstance() {
        return ApplicationManager.getApplication().getService(ClineSettingsService.class);
    }

    /**
     * Get the current state of the settings.
     * @return The current state
     */
    @Nullable
    @Override
    public State getState() {
        return myState;
    }

    /**
     * Load the state from the persisted data.
     * @param state The state to load
     */
    @Override
    public void loadState(@NotNull State state) {
        XmlSerializerUtil.copyBean(state, myState);
    }

    /**
     * State class for storing settings.
     */
    public static class State {
        // API settings
        public String apiProvider = "openai"; // Default provider
        public String apiKey = "";
        public String apiModel = "gpt-4";
        
        // UI settings
        public boolean darkMode = false;
        public int fontSize = 14;
        
        // Feature settings
        public boolean enableBrowser = true;
        public boolean enableTerminal = true;
        public boolean enableFileEditing = true;
        
        // Auto-approval settings
        public boolean enableAutoApproval = false;
        public int maxAutoApprovedRequests = 10;
        
        // TypeScript bridge settings
        public String typescriptBridgePath = "";
    }
    
    /**
     * Get the API provider.
     * @return The API provider
     */
    public String getApiProvider() {
        return myState.apiProvider;
    }
    
    /**
     * Set the API provider.
     * @param apiProvider The API provider
     */
    public void setApiProvider(String apiProvider) {
        myState.apiProvider = apiProvider;
    }
    
    /**
     * Get the API key.
     * @return The API key
     */
    public String getApiKey() {
        return myState.apiKey;
    }
    
    /**
     * Set the API key.
     * @param apiKey The API key
     */
    public void setApiKey(String apiKey) {
        myState.apiKey = apiKey;
    }
    
    /**
     * Get the API model.
     * @return The API model
     */
    public String getApiModel() {
        return myState.apiModel;
    }
    
    /**
     * Set the API model.
     * @param apiModel The API model
     */
    public void setApiModel(String apiModel) {
        myState.apiModel = apiModel;
    }
    
    /**
     * Check if dark mode is enabled.
     * @return True if dark mode is enabled
     */
    public boolean isDarkMode() {
        return myState.darkMode;
    }
    
    /**
     * Set dark mode.
     * @param darkMode True to enable dark mode
     */
    public void setDarkMode(boolean darkMode) {
        myState.darkMode = darkMode;
    }
    
    /**
     * Get the font size.
     * @return The font size
     */
    public int getFontSize() {
        return myState.fontSize;
    }
    
    /**
     * Set the font size.
     * @param fontSize The font size
     */
    public void setFontSize(int fontSize) {
        myState.fontSize = fontSize;
    }
    
    /**
     * Check if browser integration is enabled.
     * @return True if browser integration is enabled
     */
    public boolean isEnableBrowser() {
        return myState.enableBrowser;
    }
    
    /**
     * Set browser integration.
     * @param enableBrowser True to enable browser integration
     */
    public void setEnableBrowser(boolean enableBrowser) {
        myState.enableBrowser = enableBrowser;
    }
    
    /**
     * Check if terminal integration is enabled.
     * @return True if terminal integration is enabled
     */
    public boolean isEnableTerminal() {
        return myState.enableTerminal;
    }
    
    /**
     * Set terminal integration.
     * @param enableTerminal True to enable terminal integration
     */
    public void setEnableTerminal(boolean enableTerminal) {
        myState.enableTerminal = enableTerminal;
    }
    
    /**
     * Check if file editing is enabled.
     * @return True if file editing is enabled
     */
    public boolean isEnableFileEditing() {
        return myState.enableFileEditing;
    }
    
    /**
     * Set file editing.
     * @param enableFileEditing True to enable file editing
     */
    public void setEnableFileEditing(boolean enableFileEditing) {
        myState.enableFileEditing = enableFileEditing;
    }
    
    /**
     * Check if auto-approval is enabled.
     * @return True if auto-approval is enabled
     */
    public boolean isEnableAutoApproval() {
        return myState.enableAutoApproval;
    }
    
    /**
     * Set auto-approval.
     * @param enableAutoApproval True to enable auto-approval
     */
    public void setEnableAutoApproval(boolean enableAutoApproval) {
        myState.enableAutoApproval = enableAutoApproval;
    }
    
    /**
     * Get the maximum number of auto-approved requests.
     * @return The maximum number of auto-approved requests
     */
    public int getMaxAutoApprovedRequests() {
        return myState.maxAutoApprovedRequests;
    }
    
    /**
     * Set the maximum number of auto-approved requests.
     * @param maxAutoApprovedRequests The maximum number of auto-approved requests
     */
    public void setMaxAutoApprovedRequests(int maxAutoApprovedRequests) {
        myState.maxAutoApprovedRequests = maxAutoApprovedRequests;
    }
    
    /**
     * Get the TypeScript bridge path.
     * @return The TypeScript bridge path
     */
    public String getTypescriptBridgePath() {
        return myState.typescriptBridgePath;
    }
    
    /**
     * Set the TypeScript bridge path.
     * @param typescriptBridgePath The TypeScript bridge path
     */
    public void setTypescriptBridgePath(String typescriptBridgePath) {
        myState.typescriptBridgePath = typescriptBridgePath;
    }
}