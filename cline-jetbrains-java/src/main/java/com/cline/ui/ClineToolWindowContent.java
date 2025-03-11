package com.cline.ui;

import com.cline.core.model.Conversation;
import com.cline.services.ClineApiService;
import com.cline.services.ClineSettingsService;
import com.cline.ui.chat.ChatView;
import com.cline.ui.history.HistoryView;
import com.cline.ui.settings.SettingsView;
import com.cline.ui.tool.ToolTestView;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.ui.components.JBTabbedPane;
import com.intellij.util.ui.JBUI;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Main content for the Cline tool window.
 * This class creates and manages the UI components for the Cline tool window.
 */
public class ClineToolWindowContent {
    private static final Logger LOG = Logger.getInstance(ClineToolWindowContent.class);
    
    private final Project project;
    private final ToolWindow toolWindow;
    private final ClineSettingsService settingsService;
    private final ClineApiService apiService;
    
    private JPanel mainPanel;
    private JBTabbedPane tabbedPane;
    
    private ChatView chatView;
    private HistoryView historyView;
    private SettingsView settingsView;
    private ToolTestView toolTestView;
    
    private Conversation currentConversation;

    /**
     * Creates a new tool window content.
     *
     * @param project The project
     * @param toolWindow The tool window
     */
    public ClineToolWindowContent(Project project, ToolWindow toolWindow) {
        this.project = project;
        this.toolWindow = toolWindow;
        this.settingsService = ClineSettingsService.getInstance();
        this.apiService = ClineApiService.getInstance();
        
        createUIComponents();
    }

    /**
     * Creates the UI components.
     */
    private void createUIComponents() {
        mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(JBUI.Borders.empty(5));
        
        // Create tabbed pane for different views
        tabbedPane = new JBTabbedPane();
        
        // Create chat view
        chatView = new ChatView(project);
        chatView.setOnShowHistoryView(show -> {
            if (show) {
                tabbedPane.setSelectedComponent(historyView);
            }
        });
        
        // Create history view
        historyView = new HistoryView(project);
        historyView.setOnSelectConversation(conversation -> {
            currentConversation = conversation;
            chatView.setConversation(conversation);
            tabbedPane.setSelectedComponent(chatView);
        });
        
        // Create settings view
        settingsView = new SettingsView(project);
        
        // Create tool test view
        toolTestView = new ToolTestView(project);
        
        // Add views to tabbed pane
        tabbedPane.addTab("Chat", chatView);
        tabbedPane.addTab("History", historyView);
        tabbedPane.addTab("Settings", settingsView);
        tabbedPane.addTab("Tool Test", toolTestView);
        
        // Add toolbar
        JToolBar toolbar = createToolbar();
        
        // Add components to main panel
        mainPanel.add(toolbar, BorderLayout.NORTH);
        mainPanel.add(tabbedPane, BorderLayout.CENTER);
        
        // Initialize with empty conversation
        currentConversation = Conversation.createEmpty();
        chatView.setConversation(currentConversation);
    }
    
    /**
     * Creates the toolbar.
     *
     * @return The toolbar
     */
    private JToolBar createToolbar() {
        JToolBar toolbar = new JToolBar();
        toolbar.setFloatable(false);
        toolbar.setBorder(JBUI.Borders.empty(2, 5));
        
        JButton newTaskButton = new JButton("New Task");
        newTaskButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                currentConversation = Conversation.createEmpty();
                chatView.setConversation(currentConversation);
                tabbedPane.setSelectedComponent(chatView);
            }
        });
        
        toolbar.add(newTaskButton);
        
        return toolbar;
    }

    /**
     * Gets the content component.
     *
     * @return The content component
     */
    public JComponent getContent() {
        return mainPanel;
    }
}