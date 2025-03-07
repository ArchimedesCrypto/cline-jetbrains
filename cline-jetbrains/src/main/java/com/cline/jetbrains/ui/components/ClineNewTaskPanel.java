package com.cline.jetbrains.ui.components;

import com.cline.jetbrains.services.ClineProjectService;
import com.cline.jetbrains.services.ClineTaskExecutionService;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBPanel;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.components.JBTextArea;
import com.intellij.util.ui.JBUI;

import javax.swing.*;
import java.awt.*;
import java.util.function.Consumer;

public class ClineNewTaskPanel extends JBPanel<ClineNewTaskPanel> {
    private static final Logger LOG = Logger.getInstance(ClineNewTaskPanel.class);
    
    private final Project project;
    private final ClineProjectService projectService;
    private final ClineTaskExecutionService taskExecutionService;
    private final Consumer<ClineTaskListPanel.TaskItem> onTaskCreated;
    
    private JBTextArea taskInputArea;
    private JComboBox<String> modelComboBox;
    private JButton submitButton;
    
    public ClineNewTaskPanel(Project project, Consumer<ClineTaskListPanel.TaskItem> onTaskCreated) {
        super(new BorderLayout());
        this.project = project;
        this.projectService = ClineProjectService.getInstance(project);
        this.taskExecutionService = ClineTaskExecutionService.getInstance(project);
        this.onTaskCreated = onTaskCreated;
        
        initializeUI();
    }
    
    private void initializeUI() {
        LOG.info("Initializing Cline new task UI");
        
        JBPanel<?> headerPanel = createHeaderPanel();
        add(headerPanel, BorderLayout.NORTH);
        
        JBPanel<?> contentPanel = createContentPanel();
        add(contentPanel, BorderLayout.CENTER);
        
        JBPanel<?> footerPanel = createFooterPanel();
        add(footerPanel, BorderLayout.SOUTH);
    }
    
    private JBPanel<?> createHeaderPanel() {
        JBPanel<?> panel = new JBPanel<>(new BorderLayout());
        panel.setBorder(JBUI.Borders.empty(10));
        
        JBLabel titleLabel = new JBLabel("New Task");
        titleLabel.setFont(JBUI.Fonts.label().biggerOn(4));
        panel.add(titleLabel, BorderLayout.WEST);
        
        JBPanel<?> modelPanel = new JBPanel<>(new FlowLayout(FlowLayout.RIGHT));
        JBLabel modelLabel = new JBLabel("Model:");
        modelComboBox = new JComboBox<>(new String[]{"gpt-4", "claude-3-opus", "claude-3-sonnet"});
        modelPanel.add(modelLabel);
        modelPanel.add(modelComboBox);
        panel.add(modelPanel, BorderLayout.EAST);
        
        return panel;
    }
    
    private JBPanel<?> createContentPanel() {
        JBPanel<?> panel = new JBPanel<>(new BorderLayout());
        panel.setBorder(JBUI.Borders.empty(10));
        
        taskInputArea = new JBTextArea();
        taskInputArea.setLineWrap(true);
        taskInputArea.setWrapStyleWord(true);
        taskInputArea.setRows(10);
        taskInputArea.setFont(JBUI.Fonts.create("Monospaced", 14));
        
        JBScrollPane taskInputScrollPane = new JBScrollPane(taskInputArea);
        panel.add(taskInputScrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JBPanel<?> createFooterPanel() {
        JBPanel<?> panel = new JBPanel<>(new FlowLayout(FlowLayout.RIGHT));
        panel.setBorder(JBUI.Borders.empty(10));
        
        submitButton = new JButton("Submit");
        submitButton.addActionListener(e -> onSubmit());
        panel.add(submitButton);
        
        return panel;
    }
    
    private void onSubmit() {
        LOG.info("Submitting new task");
        
        String taskInput = taskInputArea.getText().trim();
        if (taskInput.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter a task description.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        String model = (String) modelComboBox.getSelectedItem();
        
        createTask(taskInput, model);
    }
    
    private void createTask(String taskInput, String model) {
        LOG.info("Creating new task: " + taskInput);
        
        try {
            // Disable the submit button
            submitButton.setEnabled(false);
            
            // Execute the task
            taskExecutionService.executeTask(taskInput, model)
                .thenAccept(taskId -> {
                    // Create a task item
                    ClineTaskListPanel.TaskItem task = taskExecutionService.createTaskItem(taskId, taskInput);
                    
                    // Clear the task input
                    taskInputArea.setText("");
                    
                    // Re-enable the submit button
                    SwingUtilities.invokeLater(() -> submitButton.setEnabled(true));
                    
                    // Call the callback
                    if (onTaskCreated != null) {
                        SwingUtilities.invokeLater(() -> onTaskCreated.accept(task));
                    }
                })
                .exceptionally(e -> {
                    LOG.error("Failed to create task", e);
                    
                    // Show an error message
                    SwingUtilities.invokeLater(() -> {
                        JOptionPane.showMessageDialog(this, "Failed to create task: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                        submitButton.setEnabled(true);
                    });
                    
                    return null;
                });
        } catch (Exception e) {
            LOG.error("Failed to create task", e);
            
            // Show an error message
            JOptionPane.showMessageDialog(this, "Failed to create task: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            
            // Re-enable the submit button
            submitButton.setEnabled(true);
        }
    }
    
    public void clearTaskInput() {
        LOG.info("Clearing task input");
        taskInputArea.setText("");
    }
    
    public void setTaskInput(String taskInput) {
        LOG.info("Setting task input");
        taskInputArea.setText(taskInput);
    }
    
    public String getTaskInput() {
        return taskInputArea.getText();
    }
    
    public void setSelectedModel(String model) {
        LOG.info("Setting selected model: " + model);
        modelComboBox.setSelectedItem(model);
    }
    
    public String getSelectedModel() {
        return (String) modelComboBox.getSelectedItem();
    }
}