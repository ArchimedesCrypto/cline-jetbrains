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

public class ClineTaskDetailsPanel extends JBPanel<ClineTaskDetailsPanel> {
    private static final Logger LOG = Logger.getInstance(ClineTaskDetailsPanel.class);
    
    private final Project project;
    private final ClineProjectService projectService;
    private final ClineTaskExecutionService taskExecutionService;
    
    private JBLabel titleLabel;
    private JBLabel statusLabel;
    private JBTextArea outputArea;
    private JButton cancelButton;
    private JButton rerunButton;
    
    private ClineTaskListPanel.TaskItem currentTask;
    
    public ClineTaskDetailsPanel(Project project) {
        super(new BorderLayout());
        this.project = project;
        this.projectService = ClineProjectService.getInstance(project);
        this.taskExecutionService = ClineTaskExecutionService.getInstance(project);
        
        initializeUI();
    }
    
    private void initializeUI() {
        LOG.info("Initializing Cline task details UI");
        
        JBPanel<?> headerPanel = createHeaderPanel();
        add(headerPanel, BorderLayout.NORTH);
        
        JBPanel<?> contentPanel = createContentPanel();
        add(contentPanel, BorderLayout.CENTER);
        
        JBPanel<?> footerPanel = createFooterPanel();
        add(footerPanel, BorderLayout.SOUTH);
        
        setNoTaskSelected();
    }
    
    private JBPanel<?> createHeaderPanel() {
        JBPanel<?> panel = new JBPanel<>(new BorderLayout());
        panel.setBorder(JBUI.Borders.empty(10));
        
        titleLabel = new JBLabel("No Task Selected");
        titleLabel.setFont(JBUI.Fonts.label().biggerOn(4));
        panel.add(titleLabel, BorderLayout.WEST);
        
        statusLabel = new JBLabel("Status: N/A");
        statusLabel.setFont(JBUI.Fonts.label());
        panel.add(statusLabel, BorderLayout.EAST);
        
        return panel;
    }
    
    private JBPanel<?> createContentPanel() {
        JBPanel<?> panel = new JBPanel<>(new BorderLayout());
        panel.setBorder(JBUI.Borders.empty(10));
        
        outputArea = new JBTextArea();
        outputArea.setEditable(false);
        outputArea.setLineWrap(true);
        outputArea.setWrapStyleWord(true);
        outputArea.setText("Select a task to view its details.");
        
        JBScrollPane outputScrollPane = new JBScrollPane(outputArea);
        outputScrollPane.setPreferredSize(JBUI.size(400, 300));
        panel.add(outputScrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JBPanel<?> createFooterPanel() {
        JBPanel<?> panel = new JBPanel<>(new FlowLayout(FlowLayout.RIGHT));
        panel.setBorder(JBUI.Borders.empty(10));
        
        cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(e -> onCancelTask());
        cancelButton.setEnabled(false);
        panel.add(cancelButton);
        
        rerunButton = new JButton("Rerun");
        rerunButton.addActionListener(e -> onRerunTask());
        rerunButton.setEnabled(false);
        panel.add(rerunButton);
        
        return panel;
    }
    
    private void setNoTaskSelected() {
        LOG.info("Setting no task selected state");
        
        currentTask = null;
        
        titleLabel.setText("No Task Selected");
        statusLabel.setText("Status: N/A");
        outputArea.setText("Select a task to view its details.");
        cancelButton.setEnabled(false);
        rerunButton.setEnabled(false);
    }
    
    public void setTask(ClineTaskListPanel.TaskItem task) {
        LOG.info("Setting task: " + task.getId());
        
        currentTask = task;
        
        titleLabel.setText(task.getDescription());
        statusLabel.setText("Status: Completed"); // TODO: Get the actual status
        
        // TODO: Get the actual output from the task execution service
        outputArea.setText("Task: " + task.getDescription() + "\n\n" +
                "Timestamp: " + task.getTimestamp() + "\n\n" +
                "Output: This is a placeholder for the task output.");
        
        // Enable/disable buttons based on the task status
        cancelButton.setEnabled(false); // TODO: Enable if the task is running
        rerunButton.setEnabled(true);
    }
    
    private void onCancelTask() {
        LOG.info("Cancelling task: " + currentTask.getId());
        
        // Disable the cancel button
        cancelButton.setEnabled(false);
        
        // Cancel the task
        taskExecutionService.cancelTask(currentTask.getId())
            .thenRun(() -> {
                // Update the UI
                SwingUtilities.invokeLater(() -> {
                    statusLabel.setText("Status: Cancelled");
                    cancelButton.setEnabled(false);
                    rerunButton.setEnabled(true);
                });
            })
            .exceptionally(e -> {
                LOG.error("Failed to cancel task", e);
                
                // Re-enable the cancel button
                SwingUtilities.invokeLater(() -> cancelButton.setEnabled(true));
                
                return null;
            });
    }
    
    private void onRerunTask() {
        LOG.info("Rerunning task: " + currentTask.getId());
        
        // Disable the rerun button
        rerunButton.setEnabled(false);
        
        // Get the task description
        String taskInput = currentTask.getDescription();
        
        // Execute the task
        taskExecutionService.executeTask(taskInput, "gpt-4") // TODO: Get the model from the task
            .thenAccept(taskId -> {
                // Update the UI
                SwingUtilities.invokeLater(() -> {
                    // Create a new task item
                    ClineTaskListPanel.TaskItem newTask = taskExecutionService.createTaskItem(taskId, taskInput);
                    
                    // Set the new task
                    setTask(newTask);
                });
            })
            .exceptionally(e -> {
                LOG.error("Failed to rerun task", e);
                
                // Re-enable the rerun button
                SwingUtilities.invokeLater(() -> rerunButton.setEnabled(true));
                
                return null;
            });
    }
    
    public void clearTask() {
        LOG.info("Clearing task");
        setNoTaskSelected();
    }
    
    public void refresh() {
        LOG.info("Refreshing task details");
        
        if (currentTask != null) {
            setTask(currentTask);
        }
    }
}