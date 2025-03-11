package com.cline.ui.chat;

import com.cline.core.model.Message;
import com.cline.core.model.MessageRole;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.project.Project;
import com.intellij.ui.JBColor;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBPanel;
import com.intellij.util.ui.JBUI;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Component for displaying a single message in the chat.
 * This is the Java equivalent of the ChatRow.tsx component in the TypeScript version.
 */
public class ChatRow extends JBPanel<ChatRow> {
    private final Message message;
    private final Project project;
    
    private boolean isExpanded = false;
    private JPanel contentPanel;
    
    /**
     * Creates a new chat row.
     *
     * @param message The message to display
     * @param project The project
     */
    public ChatRow(@NotNull Message message, @NotNull Project project) {
        super(new BorderLayout());
        this.message = message;
        this.project = project;
        
        setBorder(JBUI.Borders.empty(10, 6, 10, 15));
        setOpaque(false);
        
        createUIComponents();
    }
    
    /**
     * Creates the UI components.
     */
    private void createUIComponents() {
        // Create header panel with icon and title
        JPanel headerPanel = createHeaderPanel();
        
        // Create content panel
        contentPanel = new JPanel(new BorderLayout());
        contentPanel.setOpaque(false);
        contentPanel.setBorder(JBUI.Borders.emptyTop(10));
        
        // Add content based on message type
        addMessageContent();
        
        // Add components to main panel
        add(headerPanel, BorderLayout.NORTH);
        add(contentPanel, BorderLayout.CENTER);
    }
    
    /**
     * Creates the header panel with icon and title.
     *
     * @return The header panel
     */
    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        
        // Create icon and title based on message type
        Icon icon = null;
        String title = null;
        Color titleColor = JBColor.foreground();
        
        if (message.getRole() == MessageRole.ASSISTANT) {
            // Assistant message
            if (message.isToolUse()) {
                // Tool use message
                icon = AllIcons.Nodes.Plugin;
                title = "Cline wants to use a tool:";
            } else if (message.isCommand()) {
                // Command message
                icon = AllIcons.Nodes.Console;
                title = "Cline wants to execute this command:";
            } else if (message.isError()) {
                // Error message
                icon = AllIcons.General.Error;
                title = "Error";
                titleColor = JBColor.RED;
            } else {
                // Regular assistant message
                icon = null;
                title = null;
            }
        } else if (message.getRole() == MessageRole.USER) {
            // User message
            icon = null;
            title = null;
        } else if (message.getRole() == MessageRole.TOOL) {
            // Tool message
            icon = AllIcons.Nodes.Plugin;
            title = "Tool Result:";
        }
        
        // If we have an icon and title, add them to the header panel
        if (icon != null && title != null) {
            JPanel iconTitlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
            iconTitlePanel.setOpaque(false);
            
            JBLabel iconLabel = new JBLabel(icon);
            JBLabel titleLabel = new JBLabel(title);
            titleLabel.setForeground(titleColor);
            titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD));
            
            iconTitlePanel.add(iconLabel);
            iconTitlePanel.add(titleLabel);
            
            headerPanel.add(iconTitlePanel, BorderLayout.WEST);
            
            // Add expand/collapse button if the message is expandable
            if (isExpandable()) {
                JButton expandButton = new JButton(isExpanded ? AllIcons.General.ArrowUp : AllIcons.General.ArrowDown);
                expandButton.setBorderPainted(false);
                expandButton.setContentAreaFilled(false);
                expandButton.setFocusPainted(false);
                expandButton.addActionListener(e -> toggleExpanded());
                
                headerPanel.add(expandButton, BorderLayout.EAST);
            }
        }
        
        return headerPanel;
    }
    
    /**
     * Adds the message content to the content panel.
     */
    private void addMessageContent() {
        if (message.getRole() == MessageRole.ASSISTANT) {
            // Assistant message
            if (message.isToolUse()) {
                // Tool use message
                addToolUseContent();
            } else if (message.isCommand()) {
                // Command message
                addCommandContent();
            } else if (message.isError()) {
                // Error message
                addErrorContent();
            } else {
                // Regular assistant message
                addTextContent();
            }
        } else if (message.getRole() == MessageRole.USER) {
            // User message
            addUserContent();
        } else if (message.getRole() == MessageRole.TOOL) {
            // Tool message
            addToolResultContent();
        }
    }
    
    /**
     * Adds tool use content to the content panel.
     */
    private void addToolUseContent() {
        // TODO: Implement tool use content
        JTextArea textArea = new JTextArea("Tool use content: " + message.getContent());
        textArea.setEditable(false);
        textArea.setWrapStyleWord(true);
        textArea.setLineWrap(true);
        textArea.setOpaque(false);
        textArea.setBorder(JBUI.Borders.empty());
        
        contentPanel.add(textArea, BorderLayout.CENTER);
    }
    
    /**
     * Adds command content to the content panel.
     */
    private void addCommandContent() {
        // TODO: Implement command content
        JTextArea textArea = new JTextArea("Command content: " + message.getContent());
        textArea.setEditable(false);
        textArea.setWrapStyleWord(true);
        textArea.setLineWrap(true);
        textArea.setOpaque(false);
        textArea.setBorder(JBUI.Borders.empty());
        
        contentPanel.add(textArea, BorderLayout.CENTER);
    }
    
    /**
     * Adds error content to the content panel.
     */
    private void addErrorContent() {
        JTextArea textArea = new JTextArea(message.getContent());
        textArea.setEditable(false);
        textArea.setWrapStyleWord(true);
        textArea.setLineWrap(true);
        textArea.setOpaque(false);
        textArea.setForeground(JBColor.RED);
        textArea.setBorder(JBUI.Borders.empty());
        
        contentPanel.add(textArea, BorderLayout.CENTER);
    }
    
    /**
     * Adds text content to the content panel.
     */
    private void addTextContent() {
        // TODO: Implement markdown rendering
        JTextArea textArea = new JTextArea(message.getContent());
        textArea.setEditable(false);
        textArea.setWrapStyleWord(true);
        textArea.setLineWrap(true);
        textArea.setOpaque(false);
        textArea.setBorder(JBUI.Borders.empty());
        
        contentPanel.add(textArea, BorderLayout.CENTER);
    }
    
    /**
     * Adds user content to the content panel.
     */
    private void addUserContent() {
        JPanel userPanel = new JPanel(new BorderLayout());
        userPanel.setOpaque(true);
        userPanel.setBackground(JBColor.namedColor("Badge.backgroundColor", new JBColor(0x3D6185, 0x3D5A80)));
        userPanel.setBorder(JBUI.Borders.empty(9));
        
        JTextArea textArea = new JTextArea(message.getContent());
        textArea.setEditable(false);
        textArea.setWrapStyleWord(true);
        textArea.setLineWrap(true);
        textArea.setOpaque(false);
        textArea.setForeground(JBColor.namedColor("Badge.foregroundColor", JBColor.WHITE));
        textArea.setBorder(JBUI.Borders.empty());
        
        userPanel.add(textArea, BorderLayout.CENTER);
        
        // TODO: Add support for images
        
        contentPanel.add(userPanel, BorderLayout.CENTER);
    }
    
    /**
     * Adds tool result content to the content panel.
     */
    private void addToolResultContent() {
        // TODO: Implement tool result content
        JTextArea textArea = new JTextArea("Tool result content: " + message.getContent());
        textArea.setEditable(false);
        textArea.setWrapStyleWord(true);
        textArea.setLineWrap(true);
        textArea.setOpaque(false);
        textArea.setBorder(JBUI.Borders.empty());
        
        contentPanel.add(textArea, BorderLayout.CENTER);
    }
    
    /**
     * Toggles whether the message is expanded.
     */
    private void toggleExpanded() {
        isExpanded = !isExpanded;
        
        // Refresh the UI
        removeAll();
        createUIComponents();
        revalidate();
        repaint();
    }
    
    /**
     * Checks if the message is expandable.
     *
     * @return Whether the message is expandable
     */
    private boolean isExpandable() {
        // TODO: Implement expandable check based on message type
        return message.isToolUse() || message.isCommand();
    }
}