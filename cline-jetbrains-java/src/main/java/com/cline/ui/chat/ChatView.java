package com.cline.ui.chat;

import com.cline.core.model.Conversation;
import com.cline.core.model.Message;
import com.cline.core.model.MessageRole;
import com.cline.core.tool.ToolExecutor;
import com.cline.services.ClineApiService;
import com.cline.services.ClineSettingsService;
import com.cline.services.api.ApiProvider;
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
    
    private DefaultListModel<List<Message>> messageListModel;
    private JList<List<Message>> messageList;
    private JTextArea inputArea;
    private JButton sendButton;
    private JButton primaryButton;
    private JButton secondaryButton;
    private JScrollPane scrollPane;
    private JProgressBar loadingIndicator;
    
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
    private JPanel announcementPanel;

    private JPanel toastPanel;

    private void createUIComponents() {
        setLayout(new BorderLayout());

        // Toast panel overlay
        toastPanel = new JPanel();
        toastPanel.setOpaque(false);
        toastPanel.setLayout(new BoxLayout(toastPanel, BoxLayout.Y_AXIS));
        toastPanel.setAlignmentX(1.0f);
        toastPanel.setAlignmentY(0.0f);
        toastPanel.setVisible(true);

        // Announcement banner
        announcementPanel = new JPanel(new BorderLayout());
        announcementPanel.setBackground(JBColor.YELLOW);
        announcementPanel.setBorder(JBUI.Borders.empty(5));
        announcementPanel.setAlignmentX(0.0f);
        announcementPanel.setAlignmentY(0.0f);

        JLabel announcementLabel = new JLabel("Welcome to Cline! This is an announcement banner.");
        announcementPanel.add(announcementLabel, BorderLayout.CENTER);

        JButton closeAnnouncementButton = new JButton("X");
        closeAnnouncementButton.setFocusPainted(false);
        closeAnnouncementButton.setBorderPainted(false);
        closeAnnouncementButton.setContentAreaFilled(false);
        closeAnnouncementButton.addActionListener(e -> announcementPanel.setVisible(false));
        announcementPanel.add(closeAnnouncementButton, BorderLayout.EAST);

        announcementPanel.setVisible(true);

        // Container panel with overlay layout
        JPanel containerPanel = new JPanel(null);
        containerPanel.setLayout(new OverlayLayout(containerPanel));

        // Message list model and list
        messageListModel = new DefaultListModel<>();
        messageList = new JList<>(messageListModel);
        messageList.setCellRenderer(new ChatListCellRenderer(project));
        messageList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        messageList.setLayoutOrientation(JList.VERTICAL);
        messageList.setVisibleRowCount(-1);

        // Scroll pane for message list
        scrollPane = new JScrollPane(messageList);
        scrollPane.setBorder(JBUI.Borders.empty());
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setAlignmentX(0.0f);
        scrollPane.setAlignmentY(0.0f);

        containerPanel.add(scrollPane);
        containerPanel.add(announcementPanel);
        containerPanel.add(toastPanel);
        containerPanel.add(announcementPanel);

        // Scroll-to-bottom button
        JButton scrollToBottomButton = new JButton(com.intellij.icons.AllIcons.General.Down);
        scrollToBottomButton.setToolTipText("Scroll to bottom");
        scrollToBottomButton.setFocusPainted(false);
        scrollToBottomButton.setBorderPainted(false);
        scrollToBottomButton.setContentAreaFilled(false);
        scrollToBottomButton.setOpaque(false);
        scrollToBottomButton.setAlignmentX(1.0f);
        scrollToBottomButton.setAlignmentY(1.0f);
        scrollToBottomButton.setMaximumSize(new Dimension(30, 30));
        scrollToBottomButton.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                scrollToBottomButton.setContentAreaFilled(true);
            }
            @Override
            public void mouseExited(java.awt.event.MouseEvent evt) {
                scrollToBottomButton.setContentAreaFilled(false);
            }
        });
        scrollToBottomButton.addActionListener(e -> {
            JScrollBar verticalScrollBar = scrollPane.getVerticalScrollBar();
            verticalScrollBar.setValue(verticalScrollBar.getMaximum());
        });

        containerPanel.add(scrollToBottomButton);

        // Input panel
        JPanel inputPanel = new JPanel(new BorderLayout());
        inputPanel.setBorder(JBUI.Borders.empty(5, 10, 10, 10));

        inputArea = new JTextArea(3, 20);
        inputArea.setLineWrap(true);
        inputArea.setWrapStyleWord(true);
        inputArea.setBorder(JBUI.Borders.empty(5));
        inputArea.getAccessibleContext().setAccessibleName("Chat input area");
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

        JPanel inputButtonsPanel = new JPanel(new BorderLayout());

        JButton attachImageButton = new JButton(com.intellij.icons.AllIcons.Actions.Upload);
        attachImageButton.setToolTipText("Attach images");
        attachImageButton.setFocusPainted(false);
        attachImageButton.setBorderPainted(false);
        attachImageButton.setContentAreaFilled(false);
        attachImageButton.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                attachImageButton.setContentAreaFilled(true);
            }
            @Override
            public void mouseExited(java.awt.event.MouseEvent evt) {
                attachImageButton.setContentAreaFilled(false);
            }
        });
        attachImageButton.addActionListener(e -> selectImages());
        attachImageButton.getAccessibleContext().setAccessibleName("Attach images");

        sendButton = new JButton("Send", com.intellij.icons.AllIcons.Actions.Execute);
        sendButton.setToolTipText("Send message (Ctrl+Enter)");
        sendButton.setFocusPainted(false);
        sendButton.setBorderPainted(false);
        sendButton.setContentAreaFilled(false);
        sendButton.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                sendButton.setContentAreaFilled(true);
            }
            @Override
            public void mouseExited(java.awt.event.MouseEvent evt) {
                sendButton.setContentAreaFilled(false);
            }
        });
        sendButton.addActionListener(e -> sendMessage());
        sendButton.getAccessibleContext().setAccessibleName("Send message");

        inputButtonsPanel.add(attachImageButton, BorderLayout.WEST);
        inputButtonsPanel.add(sendButton, BorderLayout.EAST);

        inputPanel.add(inputScrollPane, BorderLayout.CENTER);
        inputPanel.add(inputButtonsPanel, BorderLayout.EAST);

        // Buttons panel
        JPanel buttonsPanel = new JPanel(new BorderLayout());
        buttonsPanel.setBorder(JBUI.Borders.empty(5, 10, 0, 10));

        JPanel buttonsPanelInner = new JPanel(new GridLayout(1, 2, 10, 0));

        primaryButton = new JButton("Approve", com.intellij.icons.AllIcons.Actions.Commit);
        primaryButton.setToolTipText("Approve tool or command");
        primaryButton.setFocusPainted(false);
        primaryButton.setBorderPainted(false);
        primaryButton.setContentAreaFilled(false);
        primaryButton.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                primaryButton.setContentAreaFilled(true);
            }
            @Override
            public void mouseExited(java.awt.event.MouseEvent evt) {
                primaryButton.setContentAreaFilled(false);
            }
        });
        primaryButton.addActionListener(e -> handlePrimaryButtonClick());
        primaryButton.getAccessibleContext().setAccessibleName("Approve");
        primaryButton.setVisible(false);

        secondaryButton = new JButton("Reject", com.intellij.icons.AllIcons.Actions.Cancel);
        secondaryButton.setToolTipText("Reject tool or command");
        secondaryButton.setFocusPainted(false);
        secondaryButton.setBorderPainted(false);
        secondaryButton.setContentAreaFilled(false);
        secondaryButton.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                secondaryButton.setContentAreaFilled(true);
            }
            @Override
            public void mouseExited(java.awt.event.MouseEvent evt) {
                secondaryButton.setContentAreaFilled(false);
            }
        });
        secondaryButton.addActionListener(e -> handleSecondaryButtonClick());
        secondaryButton.getAccessibleContext().setAccessibleName("Reject");
        secondaryButton.setVisible(false);

        buttonsPanelInner.add(primaryButton);
        buttonsPanelInner.add(secondaryButton);

        buttonsPanel.add(buttonsPanelInner, BorderLayout.CENTER);

        // Loading indicator
        loadingIndicator = new JProgressBar();
        loadingIndicator.setIndeterminate(true);
        loadingIndicator.setVisible(false);

        JPanel southPanel = new JPanel(new BorderLayout());
        southPanel.add(buttonsPanel, BorderLayout.NORTH);
        southPanel.add(inputPanel, BorderLayout.CENTER);
        southPanel.add(loadingIndicator, BorderLayout.SOUTH);

        add(containerPanel, BorderLayout.CENTER);
        add(southPanel, BorderLayout.SOUTH);
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
        messageListModel.clear();

        if (conversation == null) {
            List<Message> welcomeGroup = new ArrayList<>();
            Message welcomeMessage = Message.createAssistantMessage("Welcome to Cline!");
            welcomeGroup.add(welcomeMessage);
            messageListModel.addElement(welcomeGroup);
        } else {
            Message task = conversation.getMessages().isEmpty() ? null : conversation.getMessages().get(0);
            if (task != null) {
                messageListModel.addElement(List.of(task));
            }

            List<List<Message>> groups = new ArrayList<>();
            List<Message> currentGroup = new ArrayList<>();
            boolean inBrowserSession = false;

            for (Message message : conversation.getMessages()) {
                if (message == task) continue;

                if (message.isBrowserSessionStart()) {
                    if (!currentGroup.isEmpty()) {
                        groups.add(new ArrayList<>(currentGroup));
                        currentGroup.clear();
                        /**
                         * Fades in a component.
                         */
                        public void fadeInComponent(JComponent component) {
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
                    
                        /**
                         * Fades out a component.
                         */
                        public void fadeOutComponent(JComponent component) {
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
                    }
                    inBrowserSession = true;
                    currentGroup.add(message);
                } else if (message.isBrowserSessionEnd()) {
                    currentGroup.add(message);
                    groups.add(new ArrayList<>(currentGroup));
                    currentGroup.clear();
                    inBrowserSession = false;
                } else if (inBrowserSession) {
                    currentGroup.add(message);
                } else {
                    groups.add(List.of(message));
                }
            }
            if (!currentGroup.isEmpty()) {
                groups.add(new ArrayList<>(currentGroup));
            }

            for (List<Message> group : groups) {
                messageListModel.addElement(group);
            }
        }

        SwingUtilities.invokeLater(() -> {
            JScrollBar verticalScrollBar = scrollPane.getVerticalScrollBar();
            verticalScrollBar.setValue(verticalScrollBar.getMaximum());
            fadeInComponent(messageList); // Animate fade-in
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
            conversation = Conversation.createEmpty();
        }

        Message userMessage = Message.createUserMessage(text);
        userMessage.setImagePaths(new ArrayList<>(selectedImages));
        conversation.addMessage(userMessage);

        inputArea.setText("");
        selectedImages.clear();

        setInputDisabled(true);

        refreshMessages();

        sendMessageToApi();
    }
    
    /**
     * Sends the current conversation to the API.
     */
    private void sendMessageToApi() {
        isStreaming = true;
        loadingIndicator.setVisible(true);
        
        // Create a temporary message for streaming
        Message streamingMessage = Message.createAssistantMessage("");
        conversation.addMessage(streamingMessage);
        refreshMessages();
        
        // Create a string builder for the streaming content
        StringBuilder contentBuilder = new StringBuilder();
        
        // Send the conversation to the API with streaming
        apiService.sendConversationStreaming(conversation, new ApiProvider.StreamHandler() {
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
                    loadingIndicator.setVisible(false);
                    
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
                    loadingIndicator.setVisible(false);
                    
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
                    showToast("Error: " + error.getMessage()); // Show toast on API error
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
        // Disable input and show loading
        setInputDisabled(true);
        loadingIndicator.setVisible(true);
        setInputDisabled(true);
        
        // Get the tool name and input
        String toolName = toolUseMessage.getToolName();
        JsonObject toolInput = toolUseMessage.getToolInput();
        
        if (toolName == null || toolInput == null) {
            LOG.error("Tool name or input is null");
            setInputDisabled(false);
            loadingIndicator.setVisible(false);
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
                        loadingIndicator.setVisible(false);
                        refreshMessages();
                        showToast("Error executing tool: " + e.getMessage()); // Show toast on tool error
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
        public void showToast(String message) {
            JLabel toastLabel = new JLabel(message);
            toastLabel.setOpaque(true);
            toastLabel.setBackground(new Color(0, 0, 0, 200));
            toastLabel.setForeground(Color.WHITE);
            toastLabel.setBorder(JBUI.Borders.empty(5, 10));
            toastLabel.setAlignmentX(1.0f);
            toastLabel.setMaximumSize(new Dimension(300, 30));
    
            toastPanel.add(toastLabel, 0);
            toastPanel.revalidate();
            toastPanel.repaint();
    
            Timer fadeTimer = new Timer(50, null);
            fadeTimer.addActionListener(e -> {
                float alpha = toastLabel.getBackground().getAlpha() / 255f;
                alpha -= 0.05f;
                if (alpha <= 0f) {
                    toastPanel.remove(toastLabel);
                    toastPanel.revalidate();
                    toastPanel.repaint();
                    fadeTimer.stop();
                } else {
                    toastLabel.setBackground(new Color(0, 0, 0, (int)(alpha * 255)));
                }
            });
    
            Timer delayTimer = new Timer(2000, evt -> fadeTimer.start());
            delayTimer.setRepeats(false);
            delayTimer.start();
        }
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
        private void selectImages() {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setMultiSelectionEnabled(true);
            fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
            int result = fileChooser.showOpenDialog(this);
            if (result == JFileChooser.APPROVE_OPTION) {
                for (var file : fileChooser.getSelectedFiles()) {
                    if (!selectedImages.contains(file.getAbsolutePath())) {
                        selectedImages.add(file.getAbsolutePath());
                    }
                }
            }
        }
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
        /**
         * Retries sending the message preceding the failed assistant message.
         *
         * @param failedAssistantMessage The assistant message that indicated failure or error.
         */
        public void retryFailedAssistantMessage(Message failedAssistantMessage) {
            if (conversation == null) return;
    
            int failedIndex = conversation.getMessages().indexOf(failedAssistantMessage);
            if (failedIndex <= 0) return; // Cannot retry if it's the first message or not found
    
            // Find the last user message before the failure
            Message lastUserMessage = null;
            for (int i = failedIndex - 1; i >= 0; i--) {
                if (conversation.getMessages().get(i).getRole() == MessageRole.USER) {
                    lastUserMessage = conversation.getMessages().get(i);
                    break;
                }
            }
    
            if (lastUserMessage == null) return; // No user message found to retry
    
            // Remove the failed message and any subsequent messages
            conversation.getMessages().subList(failedIndex, conversation.getMessages().size()).clear();
    
            // Resend the conversation (which now ends with the last user message)
            setInputDisabled(true);
            refreshMessages();
            sendMessageToApi();
            /**
             * Populates the input area with the content of a user message for editing.
             *
             * @param userMessage The user message to edit.
             */
            public void editUserMessage(Message userMessage) {
                if (userMessage.getRole() != MessageRole.USER) return;
        
                // TODO: Handle removing the message and resending after edit confirmation?
                // For now, just populate the input area.
                inputArea.setText(userMessage.getContent());
                // TODO: Handle images associated with the message?
                inputArea.requestFocus();
                /**
                 * Deletes a user message from the current conversation.
                 *
                 * @param userMessage The user message to delete.
                 */
                public void deleteUserMessage(Message userMessage) {
                    if (conversation == null || userMessage.getRole() != MessageRole.USER) return;
            
                    int confirm = JOptionPane.showConfirmDialog(
                            this,
                            "Are you sure you want to delete this message?",
                            "Confirm Delete",
                            JOptionPane.YES_NO_OPTION,
                            JOptionPane.WARNING_MESSAGE);
            
                    if (confirm == JOptionPane.YES_OPTION) {
                        boolean removed = conversation.getMessages().remove(userMessage);
                        if (removed) {
                            refreshMessages();
                            // TODO: Consider if API needs to be notified or if subsequent messages need re-evaluation
                        } else {
                            showToast("Error: Could not find message to delete.");
                        }
                    }
                }
            }
        }
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