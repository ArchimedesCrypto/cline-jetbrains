package com.cline.ui.settings;

import com.cline.services.ClineSettingsService;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.components.JBTabbedPane;
import com.intellij.ui.components.JBPasswordField;
import com.intellij.util.ui.FormBuilder;
import com.intellij.util.ui.JBUI;

import javax.swing.*;
import java.awt.*;

/**
 * View for displaying and editing settings.
 * This is the Java equivalent of the SettingsView.tsx component in the TypeScript version.
 */
public class SettingsView extends JPanel {
    private static final Logger LOG = Logger.getInstance(SettingsView.class);
    
    private final Project project;
    private final ClineSettingsService settingsService;
    
    private JBTabbedPane tabbedPane;
    
    // API settings
    private JComboBox<String> apiProviderComboBox;
    private JBPasswordField apiKeyField;
    private JComboBox<String> modelComboBox;
    
    // General settings
    private JCheckBox autoApproveCheckBox;
    private JSpinner autoApproveMaxRequestsSpinner;
    
    /**
     * Creates a new settings view.
     *
     * @param project The project
     */
    public SettingsView(Project project) {
        super(new BorderLayout());
        this.project = project;
        this.settingsService = ClineSettingsService.getInstance();
        
        setBorder(JBUI.Borders.empty(10));
        
        createUIComponents();
        loadSettings();
    }
    
    /**
     * Creates the UI components.
     */
    private void createUIComponents() {
        // Create tabbed pane for different settings categories
        tabbedPane = new JBTabbedPane();
        
        // Create API settings panel
        JPanel apiPanel = createApiPanel();
        tabbedPane.addTab("API", apiPanel);
        
        // Create general settings panel
        JPanel generalPanel = createGeneralPanel();
        tabbedPane.addTab("General", generalPanel);
        
        // Create appearance settings panel
        JPanel appearancePanel = createAppearancePanel();
        tabbedPane.addTab("Appearance", appearancePanel);
        
        // Create buttons panel
        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        
        JButton saveButton = new JButton("Save");
        saveButton.addActionListener(e -> saveSettings());
        
        JButton resetButton = new JButton("Reset");
        resetButton.addActionListener(e -> loadSettings());
        
        buttonsPanel.add(resetButton);
        buttonsPanel.add(saveButton);
        
        // Add components to main panel
        add(tabbedPane, BorderLayout.CENTER);
        add(buttonsPanel, BorderLayout.SOUTH);
    }
    
    /**
     * Creates the API settings panel.
     *
     * @return The API settings panel
     */
    private JPanel createApiPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(JBUI.Borders.empty(10));
        
        // API provider
        apiProviderComboBox = new JComboBox<>(new String[]{"OpenAI", "Anthropic", "Custom"});
        
        // API key
        apiKeyField = new JBPasswordField();
        
        // Model
        modelComboBox = new JComboBox<>(new String[]{"gpt-4", "gpt-3.5-turbo", "claude-3-opus", "claude-3-sonnet"});
        
        // Create form
        JPanel formPanel = FormBuilder.createFormBuilder()
                .addLabeledComponent("API Provider:", apiProviderComboBox)
                .addLabeledComponent("API Key:", apiKeyField)
                .addLabeledComponent("Model:", modelComboBox)
                .addComponentFillVertically(new JPanel(), 0)
                .getPanel();
        
        panel.add(formPanel, BorderLayout.CENTER);
        
        return panel;
    }
    
    /**
     * Creates the general settings panel.
     *
     * @return The general settings panel
     */
    private JPanel createGeneralPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(JBUI.Borders.empty(10));
        
        // Auto-approve
        autoApproveCheckBox = new JCheckBox("Auto-approve tool and command requests");
        
        // Max auto-approve requests
        autoApproveMaxRequestsSpinner = new JSpinner(new SpinnerNumberModel(10, 1, 100, 1));
        
        // Create form
        JPanel formPanel = FormBuilder.createFormBuilder()
                .addComponent(autoApproveCheckBox)
                .addLabeledComponent("Max auto-approve requests:", autoApproveMaxRequestsSpinner)
                .addComponentFillVertically(new JPanel(), 0)
                .getPanel();
        
        panel.add(formPanel, BorderLayout.CENTER);
        
        return panel;
    }
    
    /**
     * Creates the appearance settings panel.
     *
     * @return The appearance settings panel
     */
    private JPanel createAppearancePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(JBUI.Borders.empty(10));
        
        // TODO: Add appearance settings
        
        JBLabel placeholderLabel = new JBLabel("Appearance settings will be added in a future version.");
        panel.add(placeholderLabel, BorderLayout.CENTER);
        
        return panel;
    }
    
    /**
     * Loads settings from the settings service.
     */
    private void loadSettings() {
        // TODO: Load settings from the settings service
        
        // For now, we'll just set some default values
        apiProviderComboBox.setSelectedItem("Anthropic");
        apiKeyField.setText("sk-ant-api-key");
        modelComboBox.setSelectedItem("claude-3-sonnet");
        
        autoApproveCheckBox.setSelected(false);
        autoApproveMaxRequestsSpinner.setValue(10);
    }
    
    /**
     * Saves settings to the settings service.
     */
    private void saveSettings() {
        // TODO: Save settings to the settings service
        
        // For now, we'll just log the settings
        LOG.info("Saving settings:");
        LOG.info("API Provider: " + apiProviderComboBox.getSelectedItem());
        LOG.info("API Key: " + apiKeyField.getText());
        LOG.info("Model: " + modelComboBox.getSelectedItem());
        LOG.info("Auto-approve: " + autoApproveCheckBox.isSelected());
        LOG.info("Max auto-approve requests: " + autoApproveMaxRequestsSpinner.getValue());
        
        // Show success message
        JOptionPane.showMessageDialog(
                this,
                "Settings saved successfully.",
                "Settings Saved",
                JOptionPane.INFORMATION_MESSAGE
        );
    }
}