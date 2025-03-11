package com.cline.ui.tool;

import com.cline.core.tool.Tool;
import com.cline.core.tool.ToolExecutor;
import com.cline.core.tool.ToolRegistry;
import com.cline.core.tool.ToolResult;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.util.ui.JBUI;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.concurrent.CompletableFuture;

/**
 * View for testing tools.
 * This is the Java equivalent of the tool testing functionality in the TypeScript version.
 */
public class ToolTestView extends JPanel {
    private static final Logger LOG = Logger.getInstance(ToolTestView.class);
    
    private final Project project;
    private final ToolRegistry toolRegistry;
    private final ToolExecutor toolExecutor;
    private final Gson gson;
    
    private JComboBox<String> toolComboBox;
    private JTextArea argsArea;
    private JTextArea outputArea;
    
    /**
     * Creates a new tool test view.
     *
     * @param project The project
     */
    public ToolTestView(Project project) {
        super(new BorderLayout());
        this.project = project;
        this.toolRegistry = ToolRegistry.getInstance(project);
        this.toolExecutor = ToolExecutor.getInstance(project);
        this.gson = new GsonBuilder().setPrettyPrinting().create();
        
        setBorder(JBUI.Borders.empty(10));
        
        createUIComponents();
    }
    
    /**
     * Creates the UI components.
     */
    private void createUIComponents() {
        // Tool selection
        JPanel toolSelectionPanel = new JPanel(new BorderLayout());
        toolSelectionPanel.setBorder(JBUI.Borders.emptyBottom(10));
        
        JBLabel toolLabel = new JBLabel("Select Tool:");
        toolComboBox = new JComboBox<>();
        
        // Add available tools to combo box
        toolRegistry.getAllTools().forEach(tool -> toolComboBox.addItem(tool.getName()));
        
        toolSelectionPanel.add(toolLabel, BorderLayout.WEST);
        toolSelectionPanel.add(toolComboBox, BorderLayout.CENTER);
        
        // Arguments input
        JPanel argsPanel = new JPanel(new BorderLayout());
        argsPanel.setBorder(JBUI.Borders.emptyBottom(10));
        
        JBLabel argsLabel = new JBLabel("Arguments (JSON):");
        argsArea = new JTextArea(5, 20);
        argsArea.setText("{\n  \"path\": \"example/file.txt\"\n}");
        JBScrollPane argsScrollPane = new JBScrollPane(argsArea);
        
        argsPanel.add(argsLabel, BorderLayout.NORTH);
        argsPanel.add(argsScrollPane, BorderLayout.CENTER);
        
        // Output area
        JPanel outputPanel = new JPanel(new BorderLayout());
        outputPanel.setBorder(JBUI.Borders.emptyTop(10));
        
        JBLabel outputLabel = new JBLabel("Output:");
        outputArea = new JTextArea(10, 20);
        outputArea.setEditable(false);
        JBScrollPane outputScrollPane = new JBScrollPane(outputArea);
        
        outputPanel.add(outputLabel, BorderLayout.NORTH);
        outputPanel.add(outputScrollPane, BorderLayout.CENTER);
        
        // Execute button
        JButton executeButton = new JButton("Execute Tool");
        executeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                executeTool();
            }
        });
        
        // Layout
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(toolSelectionPanel, BorderLayout.NORTH);
        topPanel.add(argsPanel, BorderLayout.CENTER);
        
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.add(topPanel, BorderLayout.NORTH);
        centerPanel.add(executeButton, BorderLayout.CENTER);
        centerPanel.add(outputPanel, BorderLayout.SOUTH);
        
        add(centerPanel, BorderLayout.CENTER);
        
        // Update args area when tool selection changes
        toolComboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                updateArgsArea();
            }
        });
        
        // Initialize args area
        updateArgsArea();
    }
    
    /**
     * Updates the arguments area with a template for the selected tool.
     */
    private void updateArgsArea() {
        String selectedToolName = (String) toolComboBox.getSelectedItem();
        if (selectedToolName == null) return;
        
        Tool tool = toolRegistry.getTool(selectedToolName);
        if (tool == null) return;
        
        // Get the input schema for the tool
        JsonObject inputSchema = tool.getInputSchema();
        
        // Create a template JSON object based on the schema
        JsonObject template = new JsonObject();
        
        // Add properties from the schema
        if (inputSchema.has("properties")) {
            JsonObject properties = inputSchema.getAsJsonObject("properties");
            for (String propertyName : properties.keySet()) {
                JsonObject property = properties.getAsJsonObject(propertyName);
                String type = property.has("type") ? property.get("type").getAsString() : "string";
                
                switch (type) {
                    case "string":
                        template.addProperty(propertyName, "");
                        break;
                    case "number":
                    case "integer":
                        template.addProperty(propertyName, 0);
                        break;
                    case "boolean":
                        template.addProperty(propertyName, false);
                        break;
                    case "object":
                        template.add(propertyName, new JsonObject());
                        break;
                    case "array":
                        template.add(propertyName, new com.google.gson.JsonArray());
                        break;
                }
            }
        }
        
        // Set the template in the args area
        argsArea.setText(gson.toJson(template));
    }
    
    /**
     * Executes the selected tool with the provided arguments.
     */
    private void executeTool() {
        String selectedToolName = (String) toolComboBox.getSelectedItem();
        if (selectedToolName == null) {
            outputArea.setText("No tool selected.");
            return;
        }
        
        String argsJson = argsArea.getText().trim();
        if (argsJson.isEmpty()) {
            outputArea.setText("Arguments cannot be empty.");
            return;
        }
        
        outputArea.setText("Executing tool: " + selectedToolName + "...\n");
        
        try {
            JsonObject args = JsonParser.parseString(argsJson).getAsJsonObject();
            CompletableFuture<ToolResult> future = toolExecutor.executeTool(selectedToolName, args);
            
            future.thenAccept(result -> {
                SwingUtilities.invokeLater(() -> {
                    outputArea.append("\nResult:\n");
                    outputArea.append(gson.toJson(result.toJson()));
                });
            }).exceptionally(ex -> {
                SwingUtilities.invokeLater(() -> {
                    outputArea.append("\nError:\n");
                    outputArea.append(ex.getMessage());
                });
                return null;
            });
        } catch (Exception ex) {
            outputArea.append("\nError parsing arguments:\n");
            outputArea.append(ex.getMessage());
        }
    }
}