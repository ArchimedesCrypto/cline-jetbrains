package com.cline.ui.settings;

import com.cline.services.ClineSettingsService;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.util.NlsContexts;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

/**
 * Configurable component for Cline plugin settings.
 */
public class ClineSettingsConfigurable implements Configurable {
    private ClineSettingsComponent mySettingsComponent;

    @Override
    public @NlsContexts.ConfigurableName String getDisplayName() {
        return "Cline AI Assistant";
    }

    @Override
    public @Nullable JComponent createComponent() {
        mySettingsComponent = new ClineSettingsComponent();
        return mySettingsComponent.getPanel();
    }

    @Override
    public boolean isModified() {
        ClineSettingsService settings = ClineSettingsService.getInstance();
        boolean modified = !mySettingsComponent.getApiKey().equals(settings.getApiKey());
        modified |= !mySettingsComponent.getApiEndpoint().equals(settings.getApiEndpoint());
        modified |= !mySettingsComponent.getModel().equals(settings.getModel());
        modified |= mySettingsComponent.getMaxTokens() != settings.getMaxTokens();
        modified |= mySettingsComponent.isEnableBrowser() != settings.isEnableBrowser();
        modified |= mySettingsComponent.isEnableTerminal() != settings.isEnableTerminal();
        modified |= mySettingsComponent.isEnableFileAccess() != settings.isEnableFileAccess();
        return modified;
    }

    @Override
    public void apply() throws ConfigurationException {
        ClineSettingsService settings = ClineSettingsService.getInstance();
        settings.setApiKey(mySettingsComponent.getApiKey());
        settings.setApiEndpoint(mySettingsComponent.getApiEndpoint());
        settings.setModel(mySettingsComponent.getModel());
        settings.setMaxTokens(mySettingsComponent.getMaxTokens());
        settings.setEnableBrowser(mySettingsComponent.isEnableBrowser());
        settings.setEnableTerminal(mySettingsComponent.isEnableTerminal());
        settings.setEnableFileAccess(mySettingsComponent.isEnableFileAccess());
    }

    @Override
    public void reset() {
        ClineSettingsService settings = ClineSettingsService.getInstance();
        mySettingsComponent.setApiKey(settings.getApiKey());
        mySettingsComponent.setApiEndpoint(settings.getApiEndpoint());
        mySettingsComponent.setModel(settings.getModel());
        mySettingsComponent.setMaxTokens(settings.getMaxTokens());
        mySettingsComponent.setEnableBrowser(settings.isEnableBrowser());
        mySettingsComponent.setEnableTerminal(settings.isEnableTerminal());
        mySettingsComponent.setEnableFileAccess(settings.isEnableFileAccess());
    }

    @Override
    public void disposeUIResources() {
        mySettingsComponent = null;
    }
}
