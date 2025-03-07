package com.cline.jetbrains.ui.components;

import com.cline.jetbrains.services.ClineProjectService;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.ui.components.JBList;
import com.intellij.ui.components.JBPanel;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.util.ui.JBUI;

import javax.swing.*;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class ClineTaskListPanel extends JBPanel<ClineTaskListPanel> {
    private static final Logger LOG = Logger.getInstance(ClineTaskListPanel.class);
    
    private final Project project;
    private final ClineProjectService projectService;
    
    private JBList<TaskItem> taskList;
    private DefaultListModel<TaskItem> taskListModel;
    
    public ClineTaskListPanel(Project project) {
        super(new BorderLayout());
        this.project = project;
        this.projectService = ClineProjectService.getInstance(project);
        
        initializeUI();
    }
    
    private void initializeUI() {
        LOG.info("Initializing Cline task list UI");
        
        taskListModel = new DefaultListModel<>();
        
        taskList = new JBList<>(taskListModel);
        taskList.setCellRenderer(new TaskItemRenderer());
        taskList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        taskList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                TaskItem selectedTask = taskList.getSelectedValue();
                if (selectedTask != null) {
                    onTaskSelected(selectedTask);
                }
            }
        });
        
        JBScrollPane scrollPane = new JBScrollPane(taskList);
        scrollPane.setBorder(JBUI.Borders.empty());
        
        add(scrollPane, BorderLayout.CENTER);
        
        loadTasks();
    }
    
    public void addListSelectionListener(ListSelectionListener listener) {
        taskList.addListSelectionListener(listener);
    }
    
    public void removeListSelectionListener(ListSelectionListener listener) {
        taskList.removeListSelectionListener(listener);
    }
    
    private void loadTasks() {
        LOG.info("Loading Cline tasks");
        
        taskListModel.clear();
        
        taskListModel.addElement(new TaskItem("Task 1", "Create a new file", "2023-03-07 10:00:00"));
        taskListModel.addElement(new TaskItem("Task 2", "Fix a bug", "2023-03-07 11:00:00"));
        taskListModel.addElement(new TaskItem("Task 3", "Implement a feature", "2023-03-07 12:00:00"));
    }
    
    private void onTaskSelected(TaskItem task) {
        LOG.info("Task selected: " + task.getId());
    }
    
    public void addTask(TaskItem task) {
        LOG.info("Adding task: " + task.getId());
        
        taskListModel.addElement(task);
        
        taskList.setSelectedValue(task, true);
    }
    
    public TaskItem getSelectedTask() {
        return taskList.getSelectedValue();
    }
    
    public List<TaskItem> getAllTasks() {
        List<TaskItem> tasks = new ArrayList<>();
        for (int i = 0; i < taskListModel.getSize(); i++) {
            tasks.add(taskListModel.getElementAt(i));
        }
        return tasks;
    }
    
    public void clearTasks() {
        LOG.info("Clearing tasks");
        
        taskListModel.clear();
    }
    
    public void refresh() {
        LOG.info("Refreshing task list");
        
        loadTasks();
    }
    
    public static class TaskItem {
        private final String id;
        private final String description;
        private final String timestamp;
        
        public TaskItem(String id, String description, String timestamp) {
            this.id = id;
            this.description = description;
            this.timestamp = timestamp;
        }
        
        public String getId() {
            return id;
        }
        
        public String getDescription() {
            return description;
        }
        
        public String getTimestamp() {
            return timestamp;
        }
        
        @Override
        public String toString() {
            return description;
        }
    }
    
    private static class TaskItemRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            
            if (value instanceof TaskItem) {
                TaskItem task = (TaskItem) value;
                
                label.setText("<html><b>" + task.getDescription() + "</b><br><small>" + task.getTimestamp() + "</small></html>");
                
                label.setToolTipText(task.getDescription());
            }
            
            return label;
        }
    }
}