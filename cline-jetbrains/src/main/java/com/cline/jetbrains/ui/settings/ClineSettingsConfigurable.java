package com.cline.jetbrains.ui.settings;

import com.cline.jetbrains.services.ClineSettingsService;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.util.NlsContexts;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

/**
 * Configurable for Cline settings.
 * This class is responsible for creating the settings UI in the IDE preferences.
 */
public class ClineSettingsConfigurable implements Configurable {
    private static final Logger LOG = Logger.getInstance(ClineSettingsConfigurable.class);
    
    private ClineSettingsPanel settingsPanel;

    /**
     * Get the display name of the configurable.
     * @return The display name
     */
    @Override
    public @NlsContexts.ConfigurableName String getDisplayName() {
        return "Cline";
    }

    /**
     * Get the help topic.
     * @return The help topic
     */
    @Override
    public @Nullable String getHelpTopic() {
        return "Cline.Settings";
    }

    /**
     * Create the settings component.
     * @return The settings component
     */
    @Override
    public @Nullable JComponent createComponent() {
        LOG.info("Creating Cline settings component");
        settingsPanel = new ClineSettingsPanel();
        return settingsPanel.getPanel();
    }

    /**
     * Check if the settings have been modified.
     * @return True if the settings have been modified
     */
    @Override
    public boolean isModified() {
        if (settingsPanel == null) {
            return false;
        }
        
        ClineSettingsService settings = ClineSettingsService.getInstance();
        
        // Check if any settings have been modified
        return !settingsPanel.getApiProvider().equals(settings.getApiProvider()) ||
               !settingsPanel.getApiKey().equals(settings.getApiKey()) ||
               !settingsPanel.getApiModel().equals(settings.getApiModel()) ||
               settingsPanel.isDarkMode() != settings.isDarkMode() ||
               settingsPanel.getFontSize() != settings.getFontSize() ||
               settingsPanel.isEnableBrowser() != settings.isEnableBrowser() ||
               settingsPanel.isEnableTerminal() != settings.isEnableTerminal() ||
               settingsPanel.isEnableFileEditing() != settings.isEnableFileEditing() ||
               settingsPanel.isEnableAutoApproval() != settings.isEnableAutoApproval() ||
               settingsPanel.getMaxAutoApprovedRequests() != settings.getMaxAutoApprovedRequests() ||
               !settingsPanel.getTypescriptBridgePath().equals(settings.getTypescriptBridgePath());
    }

    /**
     * Apply the settings.
     * @throws ConfigurationException If the settings are invalid
     */
    @Override
    public void apply() throws ConfigurationException {
        if (settingsPanel == null) {
            return;
        }
        
        LOG.info("Applying Cline settings");
        
        ClineSettingsService settings = ClineSettingsService.getInstance();
        
        // Validate settings
        if (settingsPanel.getApiKey().isEmpty() && !settingsPanel.getApiProvider().equals("local")) {
            throw new ConfigurationException("API key is required for non-local providers");
        }
        
        // Apply settings
        settings.setApiProvider(settingsPanel.getApiProvider());
        settings.setApiKey(settingsPanel.getApiKey());
        settings.setApiModel(settingsPanel.getApiModel());
        settings.setDarkMode(settingsPanel.isDarkMode());
        settings.setFontSize(settingsPanel.getFontSize());
        settings.setEnableBrowser(settingsPanel.isEnableBrowser());
        settings.setEnableTerminal(settingsPanel.isEnableTerminal());
        settings.setEnableFileEditing(settingsPanel.isEnableFileEditing());
        settings.setEnableAutoApproval(settingsPanel.isEnableAutoApproval());
        settings.setMaxAutoApprovedRequests(settingsPanel.getMaxAutoApprovedRequests());
        settings.setTypescriptBridgePath(settingsPanel.getTypescriptBridgePath());
    }

    /**
     * Reset the settings.
     */
    @Override
    public void reset() {
        if (settingsPanel == null) {
            return;
        }
        
        LOG.info("Resetting Cline settings");
        
        ClineSettingsService settings = ClineSettingsService.getInstance();
        
        // Reset settings
        settingsPanel.setApiProvider(settings.getApiProvider());
        settingsPanel.setApiKey(settings.getApiKey());
        settingsPanel.setApiModel(settings.getApiModel());
        settingsPanel.setDarkMode(settings.isDarkMode());
        settingsPanel.setFontSize(settings.getFontSize());
        settingsPanel.setEnableBrowser(settings.isEnableBrowser());
        settingsPanel.setEnableTerminal(settings.isEnableTerminal());
        settingsPanel.setEnableFileEditing(settings.isEnableFileEditing());
        settingsPanel.setEnableAutoApproval(settings.isEnableAutoApproval());
        settingsPanel.setMaxAutoApprovedRequests(settings.getMaxAutoApprovedRequests());
        settingsPanel.setTypescriptBridgePath(settings.getTypescriptBridgePath());
    }

    /**
     * Dispose the settings component.
     */
    @Override
    public void disposeUIResources() {
        settingsPanel = null;
    }
}