package com.cline.ui;

import com.cline.core.model.Conversation;
import com.cline.services.ClineApiService;
import com.cline.services.ClineSettingsService;
import com.cline.ui.chat.ChatView;
import com.cline.ui.history.HistoryView;
import com.cline.ui.settings.SettingsView;
import com.cline.ui.tool.ToolTestView;
import com.cline.ui.account.AccountView;
import com.cline.ui.mcp.McpView;
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
    private AccountView accountView;
    private McpView mcpView;
    
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

        // Create layered pane for overlays
        JLayeredPane layeredPane = new JLayeredPane();
        layeredPane.setLayout(null);

        // Create chat view
        chatView = new ChatView(project);
        chatView.setBounds(0, 0, 1000, 800); // Will be resized dynamically
        layeredPane.add(chatView, JLayeredPane.DEFAULT_LAYER);

        // Create overlays
        historyView = new HistoryView(project);
        historyView.setBounds(0, 0, 1000, 800);
        historyView.setVisible(false);
        layeredPane.add(historyView, JLayeredPane.PALETTE_LAYER);

        settingsView = new SettingsView(project);
        settingsView.setBounds(0, 0, 1000, 800);
        settingsView.setVisible(false);
        layeredPane.add(settingsView, JLayeredPane.PALETTE_LAYER);

        toolTestView = new ToolTestView(project);
        toolTestView.setBounds(0, 0, 1000, 800);
        toolTestView.setVisible(false);
        layeredPane.add(toolTestView, JLayeredPane.PALETTE_LAYER);

        accountView = new AccountView(project);
        accountView.setBounds(0, 0, 1000, 800);
        accountView.setVisible(false);
        layeredPane.add(accountView, JLayeredPane.PALETTE_LAYER);

        mcpView = new McpView(project);
        mcpView.setBounds(0, 0, 1000, 800);
        mcpView.setVisible(false);
        layeredPane.add(mcpView, JLayeredPane.PALETTE_LAYER);

        // Resize overlays with main panel
        layeredPane.addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentResized(java.awt.event.ComponentEvent e) {
                Dimension size = layeredPane.getSize();
                chatView.setSize(size);
                historyView.setSize(size);
                settingsView.setSize(size);
                toolTestView.setSize(size);
                accountView.setSize(size);
                mcpView.setSize(size);
            }
        });

        // Add toolbar
        JToolBar toolbar = createToolbar();

        // Add components to main panel
        mainPanel.add(toolbar, BorderLayout.NORTH);
        mainPanel.add(layeredPane, BorderLayout.CENTER);

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

        JButton newTaskButton = new JButton("New Task", com.intellij.icons.AllIcons.General.Add);
        newTaskButton.setToolTipText("Start a new conversation");
        newTaskButton.setFocusPainted(false);
        newTaskButton.setBorderPainted(false);
        newTaskButton.setContentAreaFilled(false);
        newTaskButton.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseEntered(java.awt.event.MouseEvent evt) { newTaskButton.setContentAreaFilled(true); }
            @Override public void mouseExited(java.awt.event.MouseEvent evt) { newTaskButton.setContentAreaFilled(false); }
        });
        newTaskButton.addActionListener(e -> {
            currentConversation = Conversation.createEmpty();
            chatView.setConversation(currentConversation);
            hideAllOverlays();
        });

        JButton historyButton = new JButton("History", com.intellij.icons.AllIcons.Actions.Search);
        historyButton.setToolTipText("View conversation history");
        historyButton.setFocusPainted(false);
        historyButton.setBorderPainted(false);
        historyButton.setContentAreaFilled(false);
        historyButton.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseEntered(java.awt.event.MouseEvent evt) { historyButton.setContentAreaFilled(true); }
            @Override public void mouseExited(java.awt.event.MouseEvent evt) { historyButton.setContentAreaFilled(false); }
        });
        historyButton.addActionListener(e -> {
            boolean visible = !historyView.isVisible();
            hideAllOverlays();
            if (visible) fadeIn(historyView);
        });

        JButton settingsButton = new JButton("Settings", com.intellij.icons.AllIcons.General.Settings);
        settingsButton.setToolTipText("Open plugin settings");
        settingsButton.setFocusPainted(false);
        settingsButton.setBorderPainted(false);
        settingsButton.setContentAreaFilled(false);
        settingsButton.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseEntered(java.awt.event.MouseEvent evt) { settingsButton.setContentAreaFilled(true); }
            @Override public void mouseExited(java.awt.event.MouseEvent evt) { settingsButton.setContentAreaFilled(false); }
        });
        settingsButton.addActionListener(e -> {
            boolean visible = !settingsView.isVisible();
            hideAllOverlays();
            if (visible) fadeIn(settingsView);
        });

        JButton toolTestButton = new JButton("Tools", com.intellij.icons.AllIcons.Nodes.Plugin);
        toolTestButton.setToolTipText("Test available tools");
        toolTestButton.setFocusPainted(false);
        toolTestButton.setBorderPainted(false);
        toolTestButton.setContentAreaFilled(false);
        toolTestButton.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseEntered(java.awt.event.MouseEvent evt) { toolTestButton.setContentAreaFilled(true); }
            @Override public void mouseExited(java.awt.event.MouseEvent evt) { toolTestButton.setContentAreaFilled(false); }
        });
        toolTestButton.addActionListener(e -> {
            boolean visible = !toolTestView.isVisible();
            hideAllOverlays();
            if (visible) fadeIn(toolTestView);
        });

        toolbar.add(newTaskButton);
        toolbar.add(historyButton);
        toolbar.add(settingsButton);
        toolbar.add(toolTestButton);

        JButton accountButton = new JButton("Account", com.intellij.icons.AllIcons.General.User);
        accountButton.setToolTipText("Manage account and API keys");
        accountButton.setFocusPainted(false);
        accountButton.setBorderPainted(false);
        accountButton.setContentAreaFilled(false);
        accountButton.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseEntered(java.awt.event.MouseEvent evt) { accountButton.setContentAreaFilled(true); }
            @Override public void mouseExited(java.awt.event.MouseEvent evt) { accountButton.setContentAreaFilled(false); }
        });
        accountButton.addActionListener(e -> {
            boolean visible = !accountView.isVisible();
            hideAllOverlays();
            if (visible) fadeIn(accountView);
        });
        toolbar.add(accountButton);

        JButton mcpButton = new JButton("MCP", com.intellij.icons.AllIcons.Nodes.Plugin);
        mcpButton.setToolTipText("Manage MCP servers and tools");
        mcpButton.setFocusPainted(false);
        mcpButton.setBorderPainted(false);
        mcpButton.setContentAreaFilled(false);
        mcpButton.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseEntered(java.awt.event.MouseEvent evt) { mcpButton.setContentAreaFilled(true); }
            @Override public void mouseExited(java.awt.event.MouseEvent evt) { mcpButton.setContentAreaFilled(false); }
        });
        mcpButton.addActionListener(e -> {
            boolean visible = !mcpView.isVisible();
            hideAllOverlays();
            if (visible) fadeIn(mcpView);
        });
        toolbar.add(mcpButton);

        return toolbar;
    }

    private void fadeIn(JComponent component) {
        component.setVisible(true);
        component.setOpaque(false);
        component.setAlpha(0f);
        Timer timer = new Timer(15, null);
        timer.addActionListener(e -> {
            float alpha = component.getAlpha();
            alpha += 0.05f;
            if (alpha >= 1f) {
                alpha = 1f;
                timer.stop();
            }
            component.setAlpha(alpha);
            component.repaint();
        });
        timer.start();
    }

    private void fadeOut(JComponent component) {
        Timer timer = new Timer(15, null);
        timer.addActionListener(e -> {
            float alpha = component.getAlpha();
            alpha -= 0.05f;
            if (alpha <= 0f) {
                alpha = 0f;
                component.setVisible(false);
                timer.stop();
            }
            component.setAlpha(alpha);
            component.repaint();
        });
        timer.start();
        private void hideAllOverlays() {
            historyView.setVisible(false);
            settingsView.setVisible(false);
            toolTestView.setVisible(false);
            // Future: accountView, mcpView overlays
            private void fadeIn(JComponent component) {
                component.setVisible(true);
                Timer timer = new Timer(15, null);
                timer.addActionListener(e -> {
                    float alpha = component.getAlpha();
                    alpha += 0.05f;
                    if (alpha >= 1f) {
                        alpha = 1f;
                        timer.stop();
                    }
                    component.setAlpha(alpha);
                    component.repaint();
                });
                component.setAlpha(0f);
                timer.start();
            }
        
            private void fadeOut(JComponent component) {
                Timer timer = new Timer(15, null);
                timer.addActionListener(e -> {
                    float alpha = component.getAlpha();
                    alpha -= 0.05f;
                    if (alpha <= 0f) {
                        alpha = 0f;
                        component.setVisible(false);
                        timer.stop();
                    }
                    component.setAlpha(alpha);
                    component.repaint();
                });
                timer.start();
            }
        
            private void hideAllOverlays() {
                fadeOut(historyView);
                fadeOut(settingsView);
                fadeOut(toolTestView);
                fadeOut(accountView);
                fadeOut(mcpView);
            }
        }
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