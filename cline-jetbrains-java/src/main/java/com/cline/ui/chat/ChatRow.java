package com.cline.ui.chat;

import com.cline.core.model.Message;
import com.cline.core.model.MessageRole;
import com.cline.ui.code.CodeBlockRenderer;
import com.cline.ui.image.ImageRenderer;
import com.cline.ui.markdown.MarkdownRenderer;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.project.Project;
import com.intellij.ui.JBColor;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBPanel;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.util.ui.JBUI;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Component for displaying a single message in the chat.
 * This is the Java equivalent of the ChatRow.tsx component in the TypeScript version.
 */
public class ChatRow extends JBPanel<ChatRow> {
    private static final Pattern CODE_BLOCK_PATTERN = Pattern.compile("```(\\w*)\\s*([\\s\\S]*?)```");
    private static final Pattern IMAGE_PATTERN = Pattern.compile("!\\[(.*?)\\]\\((.*?)\\)");
    
    private final Message message;
    private final Project project;
    private final MarkdownRenderer markdownRenderer;
    private final CodeBlockRenderer codeBlockRenderer;
    
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
        this.markdownRenderer = new MarkdownRenderer();
        this.codeBlockRenderer = new CodeBlockRenderer(project);
        
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
        contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
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
        String content = message.getContent();
        
        // Create a panel for the tool use content
        JPanel toolUsePanel = new JPanel(new BorderLayout());
        toolUsePanel.setOpaque(true);
        toolUsePanel.setBackground(JBColor.namedColor("ToolWindow.background", JBColor.background().brighter()));
        toolUsePanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(JBColor.border()),
                JBUI.Borders.empty(10)
        ));
        
        // Create a text pane for the content
        JTextPane textPane = new JTextPane();
        textPane.setEditable(false);
        textPane.setOpaque(false);
        
        // Render the content as markdown
        markdownRenderer.render(content, textPane);
        
        // Add the text pane to the tool use panel
        toolUsePanel.add(textPane, BorderLayout.CENTER);
        
        // Add buttons for tool approval/rejection
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setOpaque(false);
        
        JButton approveButton = new JButton("Approve", AllIcons.Actions.Commit);
        JButton rejectButton = new JButton("Reject", AllIcons.Actions.Cancel);
        
        buttonPanel.add(approveButton);
        buttonPanel.add(rejectButton);
        
        toolUsePanel.add(buttonPanel, BorderLayout.SOUTH);
        
        // Add the tool use panel to the content panel
        contentPanel.add(toolUsePanel);
    }
    
    /**
     * Adds command content to the content panel.
     */
    private void addCommandContent() {
        String content = message.getContent();
        
        // Create a panel for the command content
        JPanel commandPanel = new JPanel(new BorderLayout());
        commandPanel.setOpaque(true);
        commandPanel.setBackground(JBColor.namedColor("ToolWindow.background", JBColor.background().brighter()));
        commandPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(JBColor.border()),
                JBUI.Borders.empty(10)
        ));
        
        // Create a component for the command
        JComponent codeComponent = codeBlockRenderer.createCodeComponent(content, "bash");
        
        // Add the code component to the command panel
        commandPanel.add(codeComponent, BorderLayout.CENTER);
        
        // Add buttons for command approval/rejection
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setOpaque(false);
        
        JButton approveButton = new JButton("Approve", AllIcons.Actions.Commit);
        JButton rejectButton = new JButton("Reject", AllIcons.Actions.Cancel);
        
        buttonPanel.add(approveButton);
        buttonPanel.add(rejectButton);
        
        commandPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        // Add the command panel to the content panel
        contentPanel.add(commandPanel);
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
        
        contentPanel.add(textArea);
    }
    
    /**
     * Adds text content to the content panel.
     */
    private void addTextContent() {
        String content = message.getContent();
        
        // Process the content to extract code blocks and images
        processContent(content);
    }
    
    /**
     * Processes the content to extract code blocks and images.
     *
     * @param content The content to process
     */
    private void processContent(String content) {
        // Create a text pane for the markdown content
        JTextPane textPane = new JTextPane();
        textPane.setEditable(false);
        textPane.setOpaque(false);
        
        // Find all code blocks
        Matcher codeBlockMatcher = CODE_BLOCK_PATTERN.matcher(content);
        StringBuffer processedContent = new StringBuffer();
        
        while (codeBlockMatcher.find()) {
            String language = codeBlockMatcher.group(1);
            String code = codeBlockMatcher.group(2);
            
            // Replace the code block with a placeholder
            codeBlockMatcher.appendReplacement(processedContent, "");
            
            // Add the content before the code block
            if (processedContent.length() > 0) {
                JTextPane contentPane = new JTextPane();
                contentPane.setEditable(false);
                contentPane.setOpaque(false);
                markdownRenderer.render(processedContent.toString(), contentPane);
                contentPanel.add(contentPane);
                processedContent.setLength(0);
            }
            
            // Add the code block
            JComponent codeComponent = codeBlockRenderer.createCodeComponent(code, language);
            JPanel codePanel = new JPanel(new BorderLayout());
            codePanel.setBorder(JBUI.Borders.empty(5, 0));
            codePanel.add(codeComponent, BorderLayout.CENTER);
            contentPanel.add(codePanel);
        }
        
        // Add any remaining content
        codeBlockMatcher.appendTail(processedContent);
        
        // Process images in the remaining content
        Matcher imageMatcher = IMAGE_PATTERN.matcher(processedContent.toString());
        StringBuffer contentWithoutImages = new StringBuffer();
        
        while (imageMatcher.find()) {
            String altText = imageMatcher.group(1);
            String imageUrl = imageMatcher.group(2);
            
            // Replace the image with a placeholder
            imageMatcher.appendReplacement(contentWithoutImages, "");
            
            // Add the content before the image
            if (contentWithoutImages.length() > 0) {
                JTextPane contentPane = new JTextPane();
                contentPane.setEditable(false);
                contentPane.setOpaque(false);
                markdownRenderer.render(contentWithoutImages.toString(), contentPane);
                contentPanel.add(contentPane);
                contentWithoutImages.setLength(0);
            }
            
            // Add the image
            JComponent imageComponent = ImageRenderer.createImageComponent(imageUrl);
            if (imageComponent != null) {
                JPanel imagePanel = new JPanel(new BorderLayout());
                imagePanel.setBorder(JBUI.Borders.empty(5, 0));
                imagePanel.add(imageComponent, BorderLayout.CENTER);
                contentPanel.add(imagePanel);
            }
        }
        
        // Add any remaining content
        imageMatcher.appendTail(contentWithoutImages);
        
        // Render the remaining content as markdown
        if (contentWithoutImages.length() > 0) {
            JTextPane contentPane = new JTextPane();
            contentPane.setEditable(false);
            contentPane.setOpaque(false);
            markdownRenderer.render(contentWithoutImages.toString(), contentPane);
            contentPanel.add(contentPane);
        }
    }
    
    /**
     * Adds user content to the content panel.
     */
    private void addUserContent() {
        JPanel userPanel = new JPanel();
        userPanel.setLayout(new BoxLayout(userPanel, BoxLayout.Y_AXIS));
        userPanel.setOpaque(true);
        userPanel.setBackground(JBColor.namedColor("Badge.backgroundColor", new JBColor(0x3D6185, 0x3D5A80)));
        userPanel.setBorder(JBUI.Borders.empty(9));
        
        // Process the content to extract images
        String content = message.getContent();
        Matcher imageMatcher = IMAGE_PATTERN.matcher(content);
        StringBuffer contentWithoutImages = new StringBuffer();
        
        while (imageMatcher.find()) {
            String altText = imageMatcher.group(1);
            String imageUrl = imageMatcher.group(2);
            
            // Replace the image with a placeholder
            imageMatcher.appendReplacement(contentWithoutImages, "");
            
            // Add the content before the image
            if (contentWithoutImages.length() > 0) {
                JTextArea textArea = new JTextArea(contentWithoutImages.toString());
                textArea.setEditable(false);
                textArea.setWrapStyleWord(true);
                textArea.setLineWrap(true);
                textArea.setOpaque(false);
                textArea.setForeground(JBColor.namedColor("Badge.foregroundColor", JBColor.WHITE));
                textArea.setBorder(JBUI.Borders.empty());
                userPanel.add(textArea);
                contentWithoutImages.setLength(0);
            }
            
            // Add the image
            JComponent imageComponent = ImageRenderer.createImageComponent(imageUrl);
            if (imageComponent != null) {
                JPanel imagePanel = new JPanel(new BorderLayout());
                imagePanel.setOpaque(false);
                imagePanel.setBorder(JBUI.Borders.empty(5, 0));
                imagePanel.add(imageComponent, BorderLayout.CENTER);
                userPanel.add(imagePanel);
            }
        }
        
        // Add any remaining content
        imageMatcher.appendTail(contentWithoutImages);
        
        if (contentWithoutImages.length() > 0) {
            JTextArea textArea = new JTextArea(contentWithoutImages.toString());
            textArea.setEditable(false);
            textArea.setWrapStyleWord(true);
            textArea.setLineWrap(true);
            textArea.setOpaque(false);
            textArea.setForeground(JBColor.namedColor("Badge.foregroundColor", JBColor.WHITE));
            textArea.setBorder(JBUI.Borders.empty());
            userPanel.add(textArea);
        }
        
        contentPanel.add(userPanel);
    }
    
    /**
     * Adds tool result content to the content panel.
     */
    private void addToolResultContent() {
        String content = message.getContent();
        
        // Create a panel for the tool result content
        JPanel toolResultPanel = new JPanel(new BorderLayout());
        toolResultPanel.setOpaque(true);
        toolResultPanel.setBackground(JBColor.namedColor("ToolWindow.background", JBColor.background().brighter()));
        toolResultPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(JBColor.border()),
                JBUI.Borders.empty(10)
        ));
        
        // Create a text pane for the content
        JTextPane textPane = new JTextPane();
        textPane.setEditable(false);
        textPane.setOpaque(false);
        
        // Render the content as markdown
        markdownRenderer.render(content, textPane);
        
        // Add the text pane to the tool result panel
        toolResultPanel.add(textPane, BorderLayout.CENTER);
        
        // Add the tool result panel to the content panel
        contentPanel.add(toolResultPanel);
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
        return message.isToolUse() || message.isCommand();
    }
}