package com.cline.ui.mcp;

import com.cline.services.mcp.McpHub;
import com.intellij.openapi.project.Project;
import com.intellij.ui.components.JBList;
import com.intellij.ui.components.JBPanel;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.util.ui.JBUI;

import javax.swing.*;
import java.awt.*;

public class McpView extends JBPanel<McpView> {
    private final Project project;
    private final McpHub mcpHub;

    private JBList<String> serverList;
    private DefaultListModel<String> serverListModel;
    private JBList<String> toolResourceList;
    private DefaultListModel<String> toolResourceListModel;

    public McpView(Project project) {
        this.mcpHub = McpHub.getInstance();
        super(new BorderLayout());
        this.project = project;
        setBorder(JBUI.Borders.empty(10));
        createUIComponents();
        loadMcpData(); // Placeholder
    }

    private void createUIComponents() {
        // Server List Panel
        serverListModel = new DefaultListModel<>();
        serverList = new JBList<>(serverListModel);
        serverList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        serverList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                updateToolResourceList(serverList.getSelectedValue());
            }
        });
        JBScrollPane serverScrollPane = new JBScrollPane(serverList);

        JPanel serverPanel = new JPanel(new BorderLayout());
        serverPanel.add(new JLabel("Connected Servers:"), BorderLayout.NORTH);
        serverPanel.add(serverScrollPane, BorderLayout.CENTER);

        JButton connectButton = new JButton("Connect Server"); // Updated label
        connectButton.setToolTipText("Connect to a new MCP server");
        connectButton.addActionListener(e -> {
            String config = JOptionPane.showInputDialog(this, "Enter Server Config/Command:");
            if (config != null && !config.trim().isEmpty()) {
                mcpHub.connectServer(config.trim());
                loadMcpData(); // Refresh list
            }
        });

        JButton disconnectButton = new JButton("Disconnect Selected"); // Updated label
        disconnectButton.setToolTipText("Disconnect the selected MCP server");
        disconnectButton.addActionListener(e -> {
            String selectedServer = serverList.getSelectedValue();
            if (selectedServer != null) {
                int confirm = JOptionPane.showConfirmDialog(
                        this,
                        "Are you sure you want to disconnect from '" + selectedServer + "'?",
                        "Confirm Disconnect",
                        JOptionPane.YES_NO_OPTION);
                if (confirm == JOptionPane.YES_OPTION) {
                    mcpHub.disconnectServer(selectedServer);
                    loadMcpData(); // Refresh list
                }
            } else {
                JOptionPane.showMessageDialog(this, "Please select a server to disconnect.", "No Server Selected", JOptionPane.WARNING_MESSAGE);
            }
        });

        JPanel serverButtons = new JPanel(new FlowLayout(FlowLayout.LEFT));
        serverButtons.add(connectButton);
        serverButtons.add(disconnectButton);

        JButton executeButton = new JButton("Execute Selected");
        executeButton.addActionListener(e -> {
            String selected = toolResourceList.getSelectedValue();
            if (selected != null) {
                boolean success = mcpHub.executeToolOrResource(selected);
                if (success) {
                    JOptionPane.showMessageDialog(this, "Executed: " + selected, "MCP Execution", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to execute: " + selected, "MCP Execution", JOptionPane.ERROR_MESSAGE);
                }
            } else {
                JOptionPane.showMessageDialog(this, "Please select a tool or resource to execute.", "No Selection", JOptionPane.WARNING_MESSAGE);
            }
        });
        serverButtons.add(executeButton);
        // Removed duplicated buttons
        serverPanel.add(serverButtons, BorderLayout.SOUTH);

        // Tool/Resource List Panel
        toolResourceListModel = new DefaultListModel<>();
        toolResourceList = new JBList<>(toolResourceListModel);
        toolResourceList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        toolResourceList.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) {
                    String selected = toolResourceList.getSelectedValue();
                    if (selected != null) {
                        JOptionPane.showMessageDialog(McpView.this, "Execute or view: " + selected, "MCP Action", JOptionPane.INFORMATION_MESSAGE);
                        // TODO: Implement actual execution or detail view
                    }
                }
            }
        });
        JBScrollPane toolResourceScrollPane = new JBScrollPane(toolResourceList);

        JPanel toolResourcePanel = new JPanel(new BorderLayout());
        toolResourcePanel.add(new JLabel("Tools & Resources:"), BorderLayout.NORTH);
        toolResourcePanel.add(toolResourceScrollPane, BorderLayout.CENTER);

        // Split Pane
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, serverPanel, toolResourcePanel);
        splitPane.setDividerLocation(250);
        splitPane.setResizeWeight(0.4);

        add(splitPane, BorderLayout.CENTER);
    }

    private void loadMcpData() {
        serverListModel.clear();
        mcpHub.getConnectedServers().forEach(serverListModel::addElement);
        // Select first server if available
        if (!serverListModel.isEmpty()) {
            serverList.setSelectedIndex(0);
            updateToolResourceList(serverListModel.getElementAt(0));
        } else {
            updateToolResourceList(null);
        }
    }

    private void updateToolResourceList(String serverName) {
        toolResourceListModel.clear();
        if (serverName != null) {
            mcpHub.getTools(serverName).forEach(tool -> toolResourceListModel.addElement("Tool: " + tool));
            mcpHub.getResources(serverName).forEach(res -> toolResourceListModel.addElement("Resource: " + res));
        }
    }
}