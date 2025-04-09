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

        // Create Chat settings panel
        JPanel chatPanel = createChatPanel();
        tabbedPane.addTab("Chat", chatPanel);

        // Create Browser settings panel
        JPanel browserPanel = createBrowserPanel();
        tabbedPane.addTab("Browser", browserPanel);

        // Create Auto-Approval settings panel
        JPanel autoApprovalPanel = createAutoApprovalPanel();
        tabbedPane.addTab("Auto-Approval", autoApprovalPanel);
        
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
        apiProviderComboBox = new JComboBox<>(new String[]{"OpenAI", "Anthropic", "Custom"});
        apiKeyField = new JBPasswordField();
        modelComboBox = new JComboBox<>(new String[]{"gpt-4", "gpt-3.5-turbo", "claude-3-opus", "claude-3-sonnet"});

        JPanel formPanel = FormBuilder.createFormBuilder()
                .addLabeledComponent("API Provider:", apiProviderComboBox)
                .addLabeledComponent("API Key:", apiKeyField)
                .addLabeledComponent("Model:", modelComboBox)
                .addComponentFillVertically(new JPanel(), 0)
                .getPanel();

        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(JBUI.Borders.empty(10));
        panel.add(formPanel, BorderLayout.CENTER);
        return panel;
    }
    
    private JSlider temperatureSlider;

    /**
     * Creates the Chat settings panel.
     *
     * @return The Chat settings panel
     */
    private JPanel createChatPanel() {
        temperatureSlider = new JSlider(0, 100, 70); // 0.0 to 1.0, scaled by 100
        temperatureSlider.setMajorTickSpacing(10);
        temperatureSlider.setMinorTickSpacing(5);
        temperatureSlider.setPaintTicks(true);
        temperatureSlider.setPaintLabels(true);

        JPanel formPanel = FormBuilder.createFormBuilder()
                .addLabeledComponent("Temperature:", temperatureSlider)
                .addComponentFillVertically(new JPanel(), 0)
                .getPanel();

        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(JBUI.Borders.empty(10));
        panel.add(formPanel, BorderLayout.CENTER);
        return panel;
    }

    /**
     * Creates the Browser settings panel.
     *
     * @return The Browser settings panel
     */
    private JPanel createBrowserPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(JBUI.Borders.empty(10));
        panel.add(new JLabel("Browser Settings Placeholder"), BorderLayout.CENTER);
        return panel;
    }

    private JCheckBox autoApproveCheckBox;
    private JSpinner autoApproveMaxRequestsSpinner;

    private JCheckBox autoApproveCheckBox;
    private JSpinner autoApproveMaxRequestsSpinner;
    private JList<String> toolApprovalList; // List for per-tool approval
    private DefaultListModel<String> toolApprovalListModel;

    /**
     * Creates the Auto-Approval settings panel.
     *
     * @return The Auto-Approval settings panel
     */
    private JPanel createAutoApprovalPanel() {
        autoApproveCheckBox = new JCheckBox("Enable Auto-Approval");
        autoApproveMaxRequestsSpinner = new JSpinner(new SpinnerNumberModel(10, 1, 100, 1));

        toolApprovalListModel = new DefaultListModel<>();
        toolApprovalList = new JBList<>(toolApprovalListModel);
        toolApprovalList.setCellRenderer(new CheckboxListCellRenderer());
        toolApprovalList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        // Add MouseListener to toggle checkbox on click
        toolApprovalList.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                int index = toolApprovalList.locationToIndex(evt.getPoint());
                if (index >= 0) {
                    CheckedItem item = (CheckedItem) toolApprovalListModel.getElementAt(index);
                    item.setSelected(!item.isSelected());
                    toolApprovalList.repaint(toolApprovalList.getCellBounds(index, index));
                }
            }
        });
        // Load tools into list
        loadToolApprovalList();

        JPanel formPanel = FormBuilder.createFormBuilder()
                .addComponent(autoApproveCheckBox)
                .addLabeledComponent("Max Auto-Approved Requests per Task:", autoApproveMaxRequestsSpinner)
                // .addLabeledComponent("Per-Tool Approval:", new JBScrollPane(toolApprovalList)) // Add list later
                .addComponentFillVertically(new JPanel(), 0)
                .getPanel();

        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(JBUI.Borders.empty(10));
        panel.add(formPanel, BorderLayout.CENTER);
        return panel;
    }
    
    /**
     * Loads settings from the settings service.
     */
    private void loadToolApprovalList() {
        toolApprovalListModel.clear();
        // TODO: Get actual tool list from ToolRegistry
        List<String> tools = List.of("read_file", "write_to_file", "apply_diff", "execute_command");
        for (String toolName : tools) {
            boolean isApproved = settingsService.isToolAutoApproved(toolName);
            toolApprovalListModel.addElement(new CheckedItem(toolName, isApproved));
        }
    }

    private void loadSettings() {
        apiProviderComboBox.setSelectedItem(settingsService.getApiProvider());
        apiKeyField.setText(settingsService.getApiKey());
        modelComboBox.setSelectedItem(settingsService.getModel());
        temperatureSlider.setValue((int)(settingsService.getTemperature() * 100));
        autoApproveCheckBox.setSelected(settingsService.isAutoApproveEnabled());
        autoApproveMaxRequestsSpinner.setValue(settingsService.getAutoApproveMaxRequests());
        loadToolApprovalList(); // Load tool list state
    }
    
    /**
     * Saves settings to the settings service.
     */
    private void saveSettings() {
        settingsService.setApiProvider((String) apiProviderComboBox.getSelectedItem());
        settingsService.setApiKey(new String(apiKeyField.getPassword()));
        settingsService.setModel((String) modelComboBox.getSelectedItem());
        settingsService.setTemperature(temperatureSlider.getValue() / 100.0);
        settingsService.setAutoApproveEnabled(autoApproveCheckBox.isSelected());
        settingsService.setAutoApproveMaxRequests((Integer) autoApproveMaxRequestsSpinner.getValue());

        // Save tool approval list state
        java.util.List<String> approvedTools = new java.util.ArrayList<>();
        for (int i = 0; i < toolApprovalListModel.getSize(); i++) {
            CheckedItem item = (CheckedItem) toolApprovalListModel.getElementAt(i);
            if (item.isSelected()) {
                approvedTools.add(item.getLabel());
            }
        }
        settingsService.setAutoApprovedTools(approvedTools);

        // Show success message
        JOptionPane.showMessageDialog(
                this,
                "Settings saved successfully.",
                "Settings Saved",
                JOptionPane.INFORMATION_MESSAGE
        );
    }
}