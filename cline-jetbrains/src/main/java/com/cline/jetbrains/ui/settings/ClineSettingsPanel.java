package com.cline.jetbrains.ui.settings;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.ui.components.JBCheckBox;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBPanel;
import com.intellij.ui.components.JBPasswordField;
import com.intellij.ui.components.JBTextField;
import com.intellij.util.ui.FormBuilder;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.UI;

import javax.swing.*;
import java.awt.*;

/**
 * Panel for Cline settings.
 * This class is responsible for creating the UI components for the settings.
 */
public class ClineSettingsPanel {
    private static final Logger LOG = Logger.getInstance(ClineSettingsPanel.class);
    
    private final JPanel mainPanel;
    
    // API settings
    private final ComboBox<String> apiProviderComboBox;
    private final JBPasswordField apiKeyField;
    private final ComboBox<String> apiModelComboBox;
    
    // UI settings
    private final JBCheckBox darkModeCheckBox;
    private final JSpinner fontSizeSpinner;
    
    // Feature settings
    private final JBCheckBox enableBrowserCheckBox;
    private final JBCheckBox enableTerminalCheckBox;
    private final JBCheckBox enableFileEditingCheckBox;
    
    // Auto-approval settings
    private final JBCheckBox enableAutoApprovalCheckBox;
    private final JSpinner maxAutoApprovedRequestsSpinner;
    
    // TypeScript bridge settings
    private final TextFieldWithBrowseButton typescriptBridgePathField;

    /**
     * Constructor.
     */
    public ClineSettingsPanel() {
        LOG.info("Creating Cline settings panel");
        
        // Create API settings components
        apiProviderComboBox = new ComboBox<>(new String[]{"openai", "anthropic", "local"});
        apiKeyField = new JBPasswordField();
        apiModelComboBox = new ComboBox<>(new String[]{"gpt-4", "gpt-3.5-turbo", "claude-3-opus", "claude-3-sonnet"});
        
        // Create UI settings components
        darkModeCheckBox = new JBCheckBox("Dark Mode");
        fontSizeSpinner = new JSpinner(new SpinnerNumberModel(14, 8, 24, 1));
        
        // Create feature settings components
        enableBrowserCheckBox = new JBCheckBox("Enable Browser Integration");
        enableTerminalCheckBox = new JBCheckBox("Enable Terminal Integration");
        enableFileEditingCheckBox = new JBCheckBox("Enable File Editing");
        
        // Create auto-approval settings components
        enableAutoApprovalCheckBox = new JBCheckBox("Enable Auto-Approval");
        maxAutoApprovedRequestsSpinner = new JSpinner(new SpinnerNumberModel(10, 1, 100, 1));
        
        // Create TypeScript bridge settings components
        typescriptBridgePathField = new TextFieldWithBrowseButton();
        typescriptBridgePathField.addBrowseFolderListener(
            "Select TypeScript Bridge Path",
            "Select the directory containing the TypeScript bridge code",
            null,
            new FileChooserDescriptor(false, true, false, false, false, false)
        );
        
        // Create the main panel
        mainPanel = FormBuilder.createFormBuilder()
            .addComponent(createTitledPanel("API Settings", createApiSettingsPanel()))
            .addComponent(createTitledPanel("UI Settings", createUiSettingsPanel()))
            .addComponent(createTitledPanel("Feature Settings", createFeatureSettingsPanel()))
            .addComponent(createTitledPanel("Auto-Approval Settings", createAutoApprovalSettingsPanel()))
            .addComponent(createTitledPanel("TypeScript Bridge Settings", createTypescriptBridgeSettingsPanel()))
            .addComponentFillVertically(new JPanel(), 0)
            .getPanel();
        
        // Add listeners
        apiProviderComboBox.addActionListener(e -> updateApiSettingsVisibility());
        enableAutoApprovalCheckBox.addActionListener(e -> updateAutoApprovalSettingsVisibility());
        
        // Initialize visibility
        updateApiSettingsVisibility();
        updateAutoApprovalSettingsVisibility();
    }

    /**
     * Create a titled panel.
     * @param title The title
     * @param content The content
     * @return The titled panel
     */
    private JPanel createTitledPanel(String title, JPanel content) {
        JBPanel<JBPanel<?>> panel = new JBPanel<>(new BorderLayout());
        panel.setBorder(JBUI.Borders.empty(10));
        
        JBLabel titleLabel = new JBLabel(title);
        titleLabel.setFont(JBUI.Fonts.label().biggerOn(2));
        titleLabel.setBorder(JBUI.Borders.empty(0, 0, 5, 0));
        
        panel.add(titleLabel, BorderLayout.NORTH);
        panel.add(content, BorderLayout.CENTER);
        
        return panel;
    }

    /**
     * Create the API settings panel.
     * @return The API settings panel
     */
    private JPanel createApiSettingsPanel() {
        return FormBuilder.createFormBuilder()
            .addLabeledComponent("API Provider:", apiProviderComboBox)
            .addLabeledComponent("API Key:", apiKeyField)
            .addLabeledComponent("API Model:", apiModelComboBox)
            .getPanel();
    }

    /**
     * Create the UI settings panel.
     * @return The UI settings panel
     */
    private JPanel createUiSettingsPanel() {
        return FormBuilder.createFormBuilder()
            .addComponent(darkModeCheckBox)
            .addLabeledComponent("Font Size:", fontSizeSpinner)
            .getPanel();
    }

    /**
     * Create the feature settings panel.
     * @return The feature settings panel
     */
    private JPanel createFeatureSettingsPanel() {
        return FormBuilder.createFormBuilder()
            .addComponent(enableBrowserCheckBox)
            .addComponent(enableTerminalCheckBox)
            .addComponent(enableFileEditingCheckBox)
            .getPanel();
    }

    /**
     * Create the auto-approval settings panel.
     * @return The auto-approval settings panel
     */
    private JPanel createAutoApprovalSettingsPanel() {
        return FormBuilder.createFormBuilder()
            .addComponent(enableAutoApprovalCheckBox)
            .addLabeledComponent("Max Auto-Approved Requests:", maxAutoApprovedRequestsSpinner)
            .getPanel();
    }

    /**
     * Create the TypeScript bridge settings panel.
     * @return The TypeScript bridge settings panel
     */
    private JPanel createTypescriptBridgeSettingsPanel() {
        return FormBuilder.createFormBuilder()
            .addLabeledComponent("TypeScript Bridge Path:", typescriptBridgePathField)
            .getPanel();
    }

    /**
     * Update the visibility of API settings based on the selected provider.
     */
    private void updateApiSettingsVisibility() {
        String provider = (String) apiProviderComboBox.getSelectedItem();
        boolean isLocal = "local".equals(provider);
        
        apiKeyField.setEnabled(!isLocal);
    }

    /**
     * Update the visibility of auto-approval settings based on the checkbox.
     */
    private void updateAutoApprovalSettingsVisibility() {
        boolean enabled = enableAutoApprovalCheckBox.isSelected();
        
        maxAutoApprovedRequestsSpinner.setEnabled(enabled);
    }

    /**
     * Get the main panel.
     * @return The main panel
     */
    public JPanel getPanel() {
        return mainPanel;
    }

    /**
     * Get the API provider.
     * @return The API provider
     */
    public String getApiProvider() {
        return (String) apiProviderComboBox.getSelectedItem();
    }

    /**
     * Set the API provider.
     * @param apiProvider The API provider
     */
    public void setApiProvider(String apiProvider) {
        apiProviderComboBox.setSelectedItem(apiProvider);
    }

    /**
     * Get the API key.
     * @return The API key
     */
    public String getApiKey() {
        return new String(apiKeyField.getPassword());
    }

    /**
     * Set the API key.
     * @param apiKey The API key
     */
    public void setApiKey(String apiKey) {
        apiKeyField.setText(apiKey);
    }

    /**
     * Get the API model.
     * @return The API model
     */
    public String getApiModel() {
        return (String) apiModelComboBox.getSelectedItem();
    }

    /**
     * Set the API model.
     * @param apiModel The API model
     */
    public void setApiModel(String apiModel) {
        apiModelComboBox.setSelectedItem(apiModel);
    }

    /**
     * Check if dark mode is enabled.
     * @return True if dark mode is enabled
     */
    public boolean isDarkMode() {
        return darkModeCheckBox.isSelected();
    }

    /**
     * Set dark mode.
     * @param darkMode True to enable dark mode
     */
    public void setDarkMode(boolean darkMode) {
        darkModeCheckBox.setSelected(darkMode);
    }

    /**
     * Get the font size.
     * @return The font size
     */
    public int getFontSize() {
        return (int) fontSizeSpinner.getValue();
    }

    /**
     * Set the font size.
     * @param fontSize The font size
     */
    public void setFontSize(int fontSize) {
        fontSizeSpinner.setValue(fontSize);
    }

    /**
     * Check if browser integration is enabled.
     * @return True if browser integration is enabled
     */
    public boolean isEnableBrowser() {
        return enableBrowserCheckBox.isSelected();
    }

    /**
     * Set browser integration.
     * @param enableBrowser True to enable browser integration
     */
    public void setEnableBrowser(boolean enableBrowser) {
        enableBrowserCheckBox.setSelected(enableBrowser);
    }

    /**
     * Check if terminal integration is enabled.
     * @return True if terminal integration is enabled
     */
    public boolean isEnableTerminal() {
        return enableTerminalCheckBox.isSelected();
    }

    /**
     * Set terminal integration.
     * @param enableTerminal True to enable terminal integration
     */
    public void setEnableTerminal(boolean enableTerminal) {
        enableTerminalCheckBox.setSelected(enableTerminal);
    }

    /**
     * Check if file editing is enabled.
     * @return True if file editing is enabled
     */
    public boolean isEnableFileEditing() {
        return enableFileEditingCheckBox.isSelected();
    }

    /**
     * Set file editing.
     * @param enableFileEditing True to enable file editing
     */
    public void setEnableFileEditing(boolean enableFileEditing) {
        enableFileEditingCheckBox.setSelected(enableFileEditing);
    }

    /**
     * Check if auto-approval is enabled.
     * @return True if auto-approval is enabled
     */
    public boolean isEnableAutoApproval() {
        return enableAutoApprovalCheckBox.isSelected();
    }

    /**
     * Set auto-approval.
     * @param enableAutoApproval True to enable auto-approval
     */
    public void setEnableAutoApproval(boolean enableAutoApproval) {
        enableAutoApprovalCheckBox.setSelected(enableAutoApproval);
    }

    /**
     * Get the maximum number of auto-approved requests.
     * @return The maximum number of auto-approved requests
     */
    public int getMaxAutoApprovedRequests() {
        return (int) maxAutoApprovedRequestsSpinner.getValue();
    }

    /**
     * Set the maximum number of auto-approved requests.
     * @param maxAutoApprovedRequests The maximum number of auto-approved requests
     */
    public void setMaxAutoApprovedRequests(int maxAutoApprovedRequests) {
        maxAutoApprovedRequestsSpinner.setValue(maxAutoApprovedRequests);
    }

    /**
     * Get the TypeScript bridge path.
     * @return The TypeScript bridge path
     */
    public String getTypescriptBridgePath() {
        return typescriptBridgePathField.getText();
    }

    /**
     * Set the TypeScript bridge path.
     * @param typescriptBridgePath The TypeScript bridge path
     */
    public void setTypescriptBridgePath(String typescriptBridgePath) {
        typescriptBridgePathField.setText(typescriptBridgePath);
    }
}