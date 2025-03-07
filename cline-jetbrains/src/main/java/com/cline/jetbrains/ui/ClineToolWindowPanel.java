package com.cline.jetbrains.ui;

import com.cline.jetbrains.services.ClineProjectService;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBPanel;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.components.JBTextArea;
import com.intellij.util.ui.JBUI;

import javax.swing.*;
import java.awt.*;

/**
 * Main panel for the Cline tool window.
 * This class is responsible for creating the UI components of the plugin.
 */
public class ClineToolWindowPanel extends JBPanel<ClineToolWindowPanel> {
    private static final Logger LOG = Logger.getInstance(ClineToolWindowPanel.class);
    
    private final Project project;
    private final ToolWindow toolWindow;
    private final ClineProjectService projectService;
    
    private JBTextArea inputArea;
    private JBTextArea outputArea;
    private JButton submitButton;

    /**
     * Constructor.
     * @param project The current project
     * @param toolWindow The tool window
     */
    public ClineToolWindowPanel(Project project, ToolWindow toolWindow) {
        super(new BorderLayout());
        this.project = project;
        this.toolWindow = toolWindow;
        this.projectService = project.getService(ClineProjectService.class);
        
        // Initialize UI components
        initializeUI();
    }

    /**
     * Initialize the UI components.
     */
    private void initializeUI() {
        LOG.info("Initializing Cline tool window UI");
        
        // Create the header panel
        JBPanel<?> headerPanel = createHeaderPanel();
        add(headerPanel, BorderLayout.NORTH);
        
        // Create the content panel
        JBPanel<?> contentPanel = createContentPanel();
        add(contentPanel, BorderLayout.CENTER);
        
        // Create the footer panel
        JBPanel<?> footerPanel = createFooterPanel();
        add(footerPanel, BorderLayout.SOUTH);
    }

    /**
     * Create the header panel.
     * @return The header panel
     */
    private JBPanel<?> createHeaderPanel() {
        JBPanel<?> panel = new JBPanel<>(new BorderLayout());
        panel.setBorder(JBUI.Borders.empty(10));
        
        JBLabel titleLabel = new JBLabel("Cline - AI Coding Assistant");
        titleLabel.setFont(JBUI.Fonts.label().biggerOn(4));
        panel.add(titleLabel, BorderLayout.CENTER);
        
        return panel;
    }

    /**
     * Create the content panel.
     * @return The content panel
     */
    private JBPanel<?> createContentPanel() {
        JBPanel<?> panel = new JBPanel<>(new BorderLayout());
        panel.setBorder(JBUI.Borders.empty(10));
        
        // Create the output area
        outputArea = new JBTextArea();
        outputArea.setEditable(false);
        outputArea.setLineWrap(true);
        outputArea.setWrapStyleWord(true);
        outputArea.setText("Welcome to Cline for JetBrains IDEs!\n\nEnter your task below and click 'Submit' to get started.");
        
        JBScrollPane outputScrollPane = new JBScrollPane(outputArea);
        outputScrollPane.setPreferredSize(JBUI.size(400, 300));
        panel.add(outputScrollPane, BorderLayout.CENTER);
        
        return panel;
    }

    /**
     * Create the footer panel.
     * @return The footer panel
     */
    private JBPanel<?> createFooterPanel() {
        JBPanel<?> panel = new JBPanel<>(new BorderLayout());
        panel.setBorder(JBUI.Borders.empty(10));
        
        // Create the input area
        inputArea = new JBTextArea();
        inputArea.setLineWrap(true);
        inputArea.setWrapStyleWord(true);
        inputArea.setRows(5);
        
        JBScrollPane inputScrollPane = new JBScrollPane(inputArea);
        panel.add(inputScrollPane, BorderLayout.CENTER);
        
        // Create the submit button
        submitButton = new JButton("Submit");
        submitButton.addActionListener(e -> onSubmit());
        
        JBPanel<?> buttonPanel = new JBPanel<>(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(submitButton);
        panel.add(buttonPanel, BorderLayout.SOUTH);
        
        return panel;
    }

    /**
     * Handle the submit button click.
     */
    private void onSubmit() {
        String input = inputArea.getText().trim();
        if (input.isEmpty()) {
            return;
        }
        
        LOG.info("Submitting task: " + input);
        
        // Clear the input area
        inputArea.setText("");
        
        // Append the input to the output area
        outputArea.append("\n\nYou: " + input);
        
        // TODO: Process the input using the TypeScript bridge
        outputArea.append("\n\nCline: Processing your request...");
        
        // For now, just simulate a response
        SwingUtilities.invokeLater(() -> {
            try {
                Thread.sleep(1000);
                outputArea.append("\n\nThis is a placeholder response. The TypeScript bridge integration is not yet implemented.");
            } catch (InterruptedException ex) {
                LOG.error("Error while simulating response", ex);
            }
        });
    }
}