package com.cline.ui.chat;

import com.cline.core.model.Conversation;
import com.cline.core.model.Message;
import com.cline.core.model.MessageRole;
import com.cline.core.tool.ToolExecutor;
import com.cline.services.ClineApiService;
import com.cline.services.ClineSettingsService;
import com.google.gson.JsonObject;
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
import java.util.concurrent.CompletableFuture;
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
    private final ToolExecutor toolExecutor;
    
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
        this.toolExecutor = ToolExecutor.getInstance(project);
        
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
            // Create a new conversation
            conversation = Conversation.createEmpty();
        }
        
        // Add user message to conversation
        Message userMessage = Message.createUserMessage(text);
        conversation.addMessage(userMessage);
        
        // Clear input
        inputArea.setText("");
        selectedImages.clear();
        
        // Disable input while waiting for response
        setInputDisabled(true);
        
        // Refresh messages
        refreshMessages();
        
        // Send message to API
        sendMessageToApi();
    }
    
    /**
     * Sends the current conversation to the API.
     */
    private void sendMessageToApi() {
        isStreaming = true;
        
        // Create a temporary message for streaming
        Message streamingMessage = Message.createAssistantMessage("");
        conversation.addMessage(streamingMessage);
        refreshMessages();
        
        // Create a string builder for the streaming content
        StringBuilder contentBuilder = new StringBuilder();
        
        // Send the conversation to the API with streaming
        apiService.sendConversationStreaming(conversation, new ClineApiService.StreamHandler() {
            @Override
            public void onTextChunk(String text) {
                // Append the text chunk to the content builder
                contentBuilder.append(text);
                
                // Update the streaming message
                SwingUtilities.invokeLater(() -> {
                    streamingMessage.setContent(contentBuilder.toString());
                    refreshMessages();
                });
            }
            
            @Override
            public void onToolUse(String toolName, JsonObject toolInput) {
                // Handle tool use
                SwingUtilities.invokeLater(() -> {
                    // Remove the streaming message
                    conversation.getMessages().remove(streamingMessage);
                    
                    // Add a tool use message
                    Message toolUseMessage = Message.createAssistantMessage(toolInput.toString());
                    // TODO: Set tool use properties
                    conversation.addMessage(toolUseMessage);
                    
                    // Show approval buttons
                    setPrimaryButton("Approve", true);
                    setSecondaryButton("Reject", true);
                    
                    // Refresh messages
                    refreshMessages();
                    
                    // Store the tool name and input for later use
                    toolUseMessage.setToolName(toolName);
                    toolUseMessage.setToolInput(toolInput);
                });
            }
            
            @Override
            public void onUsage(int inputTokens, int outputTokens) {
                // Log usage information
                LOG.info("API usage: " + inputTokens + " input tokens, " + outputTokens + " output tokens");
            }
            
            @Override
            public void onComplete() {
                // Handle completion
                SwingUtilities.invokeLater(() -> {
                    isStreaming = false;
                    
                    // If we have a streaming message, finalize it
                    if (conversation.getMessages().contains(streamingMessage)) {
                        // Replace the streaming message with a final message
                        conversation.getMessages().remove(streamingMessage);
                        Message finalMessage = Message.createAssistantMessage(contentBuilder.toString());
                        conversation.addMessage(finalMessage);
                    }
                    
                    // Enable input
                    setInputDisabled(false);
                    
                    // Refresh messages
                    refreshMessages();
                    
                    // Process any tool uses in the conversation
                    processToolUses();
                });
            }
            
            @Override
            public void onError(Throwable error) {
                // Handle error
                SwingUtilities.invokeLater(() -> {
                    isStreaming = false;
                    
                    // Remove the streaming message
                    conversation.getMessages().remove(streamingMessage);
                    
                    // Add an error message
                    JsonObject metadata = new JsonObject();
                    metadata.addProperty("error", true);
                    Message errorMessage = new Message(
                            null,
                            "Error: " + error.getMessage(),
                            MessageRole.ASSISTANT,
                            null,
                            metadata,
                            null,
                            null
                    );
                    conversation.addMessage(errorMessage);
                    
                    // Enable input
                    setInputDisabled(false);
                    
                    // Refresh messages
                    refreshMessages();
                });
            }
        });
    }
    
    /**
     * Processes any tool uses in the conversation.
     */
    private void processToolUses() {
        // Find the last message
        Message lastMessage = conversation.getLastMessage();
        if (lastMessage == null || lastMessage.getRole() != MessageRole.ASSISTANT) {
            return;
        }
        
        // Check if the message is a tool use
        if (lastMessage.isToolUse()) {
            // Show approval buttons
            setPrimaryButton("Approve", true);
            setSecondaryButton("Reject", true);
        }
    }
    
    /**
     * Handles the primary button click.
     */
    private void handlePrimaryButtonClick() {
        // Find the last message
        Message lastMessage = conversation.getLastMessage();
        if (lastMessage == null || lastMessage.getRole() != MessageRole.ASSISTANT) {
            return;
        }
        
        // Check if the message is a tool use
        if (lastMessage.isToolUse()) {
            // Execute the tool
            executeToolAndContinue(lastMessage);
        } else if (lastMessage.isCommand()) {
            // Execute the command
            executeCommandAndContinue(lastMessage);
        }
        
        // Hide buttons
        setPrimaryButtonVisible(false);
        setSecondaryButtonVisible(false);
    }
    
    /**
     * Handles the secondary button click.
     */
    private void handleSecondaryButtonClick() {
        // Find the last message
        Message lastMessage = conversation.getLastMessage();
        if (lastMessage == null || lastMessage.getRole() != MessageRole.ASSISTANT) {
            return;
        }
        
        // Add a rejection message
        Message rejectionMessage = Message.createUserMessage("I don't want to do that. Please suggest an alternative approach.");
        conversation.addMessage(rejectionMessage);
        
        // Hide buttons
        setPrimaryButtonVisible(false);
        setSecondaryButtonVisible(false);
        
        // Enable input
        setInputDisabled(false);
        
        // Refresh messages
        refreshMessages();
        
        // Send the rejection to the API
        sendMessageToApi();
    }
    
    /**
     * Executes a tool and continues the conversation.
     *
     * @param toolUseMessage The tool use message
     */
    private void executeToolAndContinue(Message toolUseMessage) {
        // Disable input
        setInputDisabled(true);
        
        // Get the tool name and input
        String toolName = toolUseMessage.getToolName();
        JsonObject toolInput = toolUseMessage.getToolInput();
        
        if (toolName == null || toolInput == null) {
            LOG.error("Tool name or input is null");
            setInputDisabled(false);
            return;
        }
        
        // Execute the tool
        toolExecutor.executeTool(toolName, toolInput)
                .thenCompose(toolResult -> {
                    // Add the tool result to the conversation
                    Message toolResultMessage = Message.createToolMessage(
                            toolName,
                            toolResult.isSuccess() ? "Tool executed successfully" : "Tool execution failed",
                            toolResult.toJson()
                    );
                    conversation.addMessage(toolResultMessage);
                    
                    // Refresh messages
                    SwingUtilities.invokeLater(this::refreshMessages);
                    
                    // Continue the conversation
                    return apiService.sendConversation(conversation);
                })
                .thenAccept(assistantMessage -> {
                    // Add the assistant message to the conversation
                    conversation.addMessage(assistantMessage);
                    
                    // Enable input
                    SwingUtilities.invokeLater(() -> {
                        setInputDisabled(false);
                        refreshMessages();
                        processToolUses();
                    });
                })
                .exceptionally(e -> {
                    // Handle error
                    LOG.error("Error executing tool", e);
                    
                    // Add an error message to the conversation
                    JsonObject metadata = new JsonObject();
                    metadata.addProperty("error", true);
                    Message errorMessage = new Message(
                            null,
                            "Error executing tool: " + e.getMessage(),
                            MessageRole.ASSISTANT,
                            null,
                            metadata,
                            null,
                            null
                    );
                    conversation.addMessage(errorMessage);
                    
                    // Enable input
                    SwingUtilities.invokeLater(() -> {
                        setInputDisabled(false);
                        refreshMessages();
                    });
                    
                    return null;
                });
    }
    
    /**
     * Executes a command and continues the conversation.
     *
     * @param commandMessage The command message
     */
    private void executeCommandAndContinue(Message commandMessage) {
        // TODO: Implement command execution
        LOG.info("Executing command: " + commandMessage.getContent());
        
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