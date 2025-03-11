package com.cline.ui.settings;

import com.intellij.ui.components.JBCheckBox;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBPasswordField;
import com.intellij.ui.components.JBTextField;
import com.intellij.util.ui.FormBuilder;
import com.intellij.util.ui.UI;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;

/**
 * Component for Cline plugin settings UI.
 */
public class ClineSettingsComponent {
    private final JPanel myMainPanel;
    private final JBTextField apiEndpointField = new JBTextField();
    private final JBPasswordField apiKeyField = new JBPasswordField();
    private final JBTextField modelField = new JBTextField();
    private final JSpinner maxTokensSpinner = new JSpinner(new SpinnerNumberModel(4000, 100, 100000, 100));
    private final JBCheckBox enableBrowserCheckbox = new JBCheckBox("Enable browser integration");
    private final JBCheckBox enableTerminalCheckbox = new JBCheckBox("Enable terminal integration");
    private final JBCheckBox enableFileAccessCheckbox = new JBCheckBox("Enable file access");

    public ClineSettingsComponent() {
        JPanel apiPanel = FormBuilder.createFormBuilder()
                .addLabeledComponent(new JBLabel("API Endpoint:"), apiEndpointField, 1, false)
                .addLabeledComponent(new JBLabel("API Key:"), apiKeyField, 1, false)
                .addLabeledComponent(new JBLabel("Model:"), modelField, 1, false)
                .addLabeledComponent(new JBLabel("Max Tokens:"), maxTokensSpinner, 1, false)
                .getPanel();

        JPanel permissionsPanel = FormBuilder.createFormBuilder()
                .addComponent(enableBrowserCheckbox)
                .addComponent(enableTerminalCheckbox)
                .addComponent(enableFileAccessCheckbox)
                .getPanel();

        myMainPanel = UI.PanelFactory.grid()
                .add(UI.PanelFactory.panel(apiPanel)
                        .withLabel("API Settings"))
                .add(UI.PanelFactory.panel(permissionsPanel)
                        .withLabel("Permissions"))
                .createPanel();
    }

    public JPanel getPanel() {
        return myMainPanel;
    }

    public JComponent getPreferredFocusedComponent() {
        return apiKeyField;
    }

    @NotNull
    public String getApiEndpoint() {
        return apiEndpointField.getText();
    }

    public void setApiEndpoint(@NotNull String newText) {
        apiEndpointField.setText(newText);
    }

    @NotNull
    public String getApiKey() {
        return new String(apiKeyField.getPassword());
    }

    public void setApiKey(@NotNull String newText) {
        apiKeyField.setText(newText);
    }

    @NotNull
    public String getModel() {
        return modelField.getText();
    }

    public void setModel(@NotNull String newText) {
        modelField.setText(newText);
    }

    public int getMaxTokens() {
        return (Integer) maxTokensSpinner.getValue();
    }

    public void setMaxTokens(int value) {
        maxTokensSpinner.setValue(value);
    }

    public boolean isEnableBrowser() {
        return enableBrowserCheckbox.isSelected();
    }

    public void setEnableBrowser(boolean selected) {
        enableBrowserCheckbox.setSelected(selected);
    }

    public boolean isEnableTerminal() {
        return enableTerminalCheckbox.isSelected();
    }

    public void setEnableTerminal(boolean selected) {
        enableTerminalCheckbox.setSelected(selected);
    }

    public boolean isEnableFileAccess() {
        return enableFileAccessCheckbox.isSelected();
    }

    public void setEnableFileAccess(boolean selected) {
        enableFileAccessCheckbox.setSelected(selected);
    }
}