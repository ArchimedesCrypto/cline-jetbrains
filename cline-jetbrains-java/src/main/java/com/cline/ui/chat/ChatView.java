package com.cline.ui.chat;

import com.cline.core.model.Conversation;
import com.cline.core.model.Message;
import com.cline.core.model.MessageRole;
import com.cline.services.ClineApiService;
import com.cline.services.ClineSettingsService;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.ui.JBColor;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.util.ui.JBUI;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Main chat view component for the Cline plugin.
 * This is the Java equivalent of the ChatView.tsx component in the TypeScript version.
 */
public class ChatView extends JPanel {
    private static final Logger LOG = Logger.getInstance(ChatView.class);
    
    private final Project project;
    private final ClineSettingsService settingsService;
    private final ClineApiService apiService;
    
    private JPanel messagesPanel;
    private JTextArea inputArea;
    private JButton sendButton;
    private JButton primaryButton;
    private JButton secondaryButton;
    private JScrollPane scrollPane;
    
    private Conversation conversation;
    private boolean isInputDisabled = false;
    private boolean isStreaming = false;
    private List<String> selectedImages = new ArrayList<>();
    
    private Consumer<Boolean> onShowHistoryView;
    private boolean isHidden = false;
    
    /**
     * Creates a new chat view.
     *
     * @param project The project
     */
    public ChatView(@NotNull Project project) {
        this.project = project;
        this.settingsService = ClineSettingsService.getInstance();
        this.apiService = ClineApiService.getInstance();
        
        setLayout(new BorderLayout());
        setBorder(JBUI.Borders.empty());
        
        createUIComponents();
    }
    
    /**
     * Creates the UI components.
     */
    private void createUIComponents() {
        // Messages panel
        messagesPanel = new JPanel();
        messagesPanel.setLayout(new BoxLayout(messagesPanel, BoxLayout.Y_AXIS));
        messagesPanel.setBorder(JBUI.Borders.empty(10));
        
        // Scroll pane for messages
        scrollPane = new JBScrollPane(messagesPanel);
        scrollPane.setBorder(JBUI.Borders.empty());
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        
        // Input panel
        JPanel inputPanel = new JPanel(new BorderLayout());
        inputPanel.setBorder(JBUI.Borders.empty(5, 10, 10, 10));
        
        inputArea = new JTextArea(3, 20);
        inputArea.setLineWrap(true);
        inputArea.setWrapStyleWord(true);
        inputArea.setBorder(JBUI.Borders.empty(5));
        inputArea.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER && e.isControlDown()) {
                    sendMessage();
                    e.consume();
                }
            }
        });
        
        JScrollPane inputScrollPane = new JBScrollPane(inputArea);
        inputScrollPane.setBorder(BorderFactory.createLineBorder(JBColor.border()));
        
        sendButton = new JButton("Send");
        sendButton.addActionListener(e -> sendMessage());
        
        inputPanel.add(inputScrollPane, BorderLayout.CENTER);
        inputPanel.add(sendButton, BorderLayout.EAST);
        
        // Buttons panel
        JPanel buttonsPanel = new JPanel(new BorderLayout());
        buttonsPanel.setBorder(JBUI.Borders.empty(5, 10, 0, 10));
        
        JPanel buttonsPanelInner = new JPanel(new GridLayout(1, 2, 10, 0));
        
        primaryButton = new JButton("Approve");
        primaryButton.addActionListener(e -> handlePrimaryButtonClick());
        primaryButton.setVisible(false);
        
        secondaryButton = new JButton("Reject");
        secondaryButton.addActionListener(e -> handleSecondaryButtonClick());
        secondaryButton.setVisible(false);
        
        buttonsPanelInner.add(primaryButton);
        buttonsPanelInner.add(secondaryButton);
        
        buttonsPanel.add(buttonsPanelInner, BorderLayout.CENTER);
        
        // Add components to main panel
        add(scrollPane, BorderLayout.CENTER);
        add(buttonsPanel, BorderLayout.SOUTH);
        add(inputPanel, BorderLayout.SOUTH);
    }
    
    /**
     * Sets the conversation to display.
     *
     * @param conversation The conversation
     */
    public void setConversation(@Nullable Conversation conversation) {
        this.conversation = conversation;
        refreshMessages();
    }
    
    /**
     * Refreshes the messages display.
     */
    private void refreshMessages() {
        messagesPanel.removeAll();
        
        if (conversation == null) {
            // Show welcome view
            JPanel welcomePanel = createWelcomePanel();
            messagesPanel.add(welcomePanel);
        } else {
            // Add task header if available
            Message task = conversation.getMessages().isEmpty() ? null : conversation.getMessages().get(0);
            if (task != null) {
                TaskHeader taskHeader = new TaskHeader(task, conversation);
                messagesPanel.add(taskHeader);
            }
            
            // Add messages
            for (Message message : conversation.getMessages()) {
                if (message == task) continue; // Skip the task message
                
                ChatRow chatRow = new ChatRow(message, project);
                messagesPanel.add(chatRow);
            }
        }
        
        messagesPanel.revalidate();
        messagesPanel.repaint();
        
        // Scroll to bottom
        SwingUtilities.invokeLater(() -> {
            JScrollBar verticalScrollBar = scrollPane.getVerticalScrollBar();
            verticalScrollBar.setValue(verticalScrollBar.getMaximum());
        });
    }
    
    /**
     * Creates the welcome panel.
     *
     * @return The welcome panel
     */
    private JPanel createWelcomePanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(JBUI.Borders.empty(20));
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JBLabel titleLabel = new JBLabel("What can I do for you?");
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 18));
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JTextArea descriptionArea = new JTextArea(
                "Thanks to Claude 3.5 Sonnet's agentic coding capabilities, " +
                "I can handle complex software development tasks step-by-step. " +
                "With tools that let me create & edit files, explore complex projects, " +
                "use the browser, and execute terminal commands (after you grant permission), " +
                "I can assist you in ways that go beyond code completion or tech support. " +
                "I can even use MCP to create new tools and extend my own capabilities."
        );
        descriptionArea.setEditable(false);
        descriptionArea.setWrapStyleWord(true);
        descriptionArea.setLineWrap(true);
        descriptionArea.setOpaque(false);
        descriptionArea.setAlignmentX(Component.LEFT_ALIGNMENT);
        descriptionArea.setBorder(JBUI.Borders.empty(10, 0, 0, 0));
        
        panel.add(titleLabel);
        panel.add(descriptionArea);
        
        // Add history preview if available
        // TODO: Implement history preview
        
        return panel;
    }
    
    /**
     * Sends a message.
     */
    private void sendMessage() {
        String text = inputArea.getText().trim();
        if (text.isEmpty() && selectedImages.isEmpty()) return;
        
        if (conversation == null) {
            // Create a new task
            // TODO: Implement new task creation
            LOG.info("Creating new task: " + text);
        } else {
            // Add user message to conversation
            Message userMessage = Message.createUserMessage(text);
            conversation.addMessage(userMessage);
            
            // TODO: Send message to API
            LOG.info("Sending message: " + text);
        }
        
        // Clear input
        inputArea.setText("");
        selectedImages.clear();
        
        // Disable input while waiting for response
        setInputDisabled(true);
        
        // Refresh messages
        refreshMessages();
    }
    
    /**
     * Handles the primary button click.
     */
    private void handlePrimaryButtonClick() {
        // TODO: Implement primary button click handling
        LOG.info("Primary button clicked");
        
        // Hide buttons
        setPrimaryButtonVisible(false);
        setSecondaryButtonVisible(false);
        
        // Enable input
        setInputDisabled(false);
    }
    
    /**
     * Handles the secondary button click.
     */
    private void handleSecondaryButtonClick() {
        // TODO: Implement secondary button click handling
        LOG.info("Secondary button clicked");
        
        // Hide buttons
        setPrimaryButtonVisible(false);
        setSecondaryButtonVisible(false);
        
        // Enable input
        setInputDisabled(false);
    }
    
    /**
     * Sets whether the input is disabled.
     *
     * @param disabled Whether the input is disabled
     */
    public void setInputDisabled(boolean disabled) {
        this.isInputDisabled = disabled;
        inputArea.setEnabled(!disabled);
        sendButton.setEnabled(!disabled);
    }
    
    /**
     * Sets the primary button text and visibility.
     *
     * @param text The button text
     * @param visible Whether the button is visible
     */
    public void setPrimaryButton(String text, boolean visible) {
        primaryButton.setText(text);
        setPrimaryButtonVisible(visible);
    }
    
    /**
     * Sets the secondary button text and visibility.
     *
     * @param text The button text
     * @param visible Whether the button is visible
     */
    public void setSecondaryButton(String text, boolean visible) {
        secondaryButton.setText(text);
        setSecondaryButtonVisible(visible);
    }
    
    /**
     * Sets whether the primary button is visible.
     *
     * @param visible Whether the button is visible
     */
    public void setPrimaryButtonVisible(boolean visible) {
        primaryButton.setVisible(visible);
    }
    
    /**
     * Sets whether the secondary button is visible.
     *
     * @param visible Whether the button is visible
     */
    public void setSecondaryButtonVisible(boolean visible) {
        secondaryButton.setVisible(visible);
    }
    
    /**
     * Sets whether the chat view is hidden.
     *
     * @param hidden Whether the chat view is hidden
     */
    public void setHidden(boolean hidden) {
        this.isHidden = hidden;
        setVisible(!hidden);
    }
    
    /**
     * Sets the callback for showing the history view.
     *
     * @param onShowHistoryView The callback
     */
    public void setOnShowHistoryView(Consumer<Boolean> onShowHistoryView) {
        this.onShowHistoryView = onShowHistoryView;
    }
}