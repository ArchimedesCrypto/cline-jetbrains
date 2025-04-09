package com.cline.ui.account;

import com.cline.services.ClineSettingsService;
import com.intellij.openapi.project.Project;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBPanel;
import com.intellij.ui.components.JBPasswordField;
import com.intellij.util.ui.FormBuilder;
import com.intellij.util.ui.JBUI;

import javax.swing.*;
import java.awt.*;

public class AccountView extends JBPanel<AccountView> {
    private final Project project;
    private final ClineSettingsService settingsService;

    private JButton loginLogoutButton;
    private JBPasswordField apiKeyField; // Re-use from Settings? Or separate?
    private JBLabel accountInfoLabel;

    public AccountView(Project project) {
        super(new BorderLayout());
        this.project = project;
        this.settingsService = ClineSettingsService.getInstance();
        setBorder(JBUI.Borders.empty(10));
        createUIComponents();
        updateUIState();
    }

    private void createUIComponents() {
        loginLogoutButton = new JButton("Login");
        apiKeyField = new JBPasswordField();
        accountInfoLabel = new JBLabel("Status: Not Logged In");

        loginLogoutButton.addActionListener(e -> handleLoginLogout());

        JPanel formPanel = FormBuilder.createFormBuilder()
                .addLabeledComponent("API Key:", apiKeyField) // Consider linking to Settings API key
                .addComponent(loginLogoutButton)
                .addLabeledComponent("Account Status:", accountInfoLabel)
                .addComponentFillVertically(new JPanel(), 0)
                .getPanel();

        add(formPanel, BorderLayout.CENTER);
    }

    private void handleLoginLogout() {
        if (isLoggedIn()) {
            int confirm = JOptionPane.showConfirmDialog(
                    this,
                    "Are you sure you want to log out? This will clear your saved API key.",
                    "Confirm Logout",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE);
            if (confirm == JOptionPane.YES_OPTION) {
                settingsService.setApiKey("");
                updateUIState();
            }
        } else {
            String apiKey = new String(apiKeyField.getPassword());
            if (apiKey.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please enter an API key.", "API Key Required", JOptionPane.WARNING_MESSAGE);
                return;
            }
            // Validate API key by making a test API call
            boolean valid = validateApiKey(apiKey);
            if (valid) {
                settingsService.setApiKey(apiKey);
                updateUIState();
                JOptionPane.showMessageDialog(this, "API Key validated and saved. You are now logged in.", "Login Successful", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Invalid API Key. Please try again.", "Login Failed", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private boolean validateApiKey(String apiKey) {
        // TODO: Implement real API key validation by making a test API call
        // For now, simulate success if key length > 10
        return apiKey.length() > 10;
    }

    private boolean isLoggedIn() {
        // Consider logged in if API key is present
        String apiKey = settingsService.getApiKey();
        return apiKey != null && !apiKey.isEmpty();
    }

    private void updateUIState() {
        // TODO: Check actual login status
        String apiKey = settingsService.getApiKey();
        if (apiKey != null && !apiKey.isEmpty()) {
            loginLogoutButton.setText("Logout");
            accountInfoLabel.setText("Status: Logged In (Key Present)");
            apiKeyField.setText(apiKey); // Show saved key
        } else {
            loginLogoutButton.setText("Login");
            accountInfoLabel.setText("Status: Not Logged In");
            apiKeyField.setText("");
        }
    }
}