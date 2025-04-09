package com.cline.ui.history;

import com.cline.core.model.Conversation;
import com.cline.services.ClineHistoryService;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.ui.components.JBList;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.util.ui.JBUI;

import javax.swing.*;
import java.awt.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.function.Consumer;

/**
 * View for displaying conversation history.
 * This is the Java equivalent of the HistoryView.tsx component in the TypeScript version.
 */
public class HistoryView extends JPanel {
    private static final Logger LOG = Logger.getInstance(HistoryView.class);
    
    private final Project project;
    private final ClineHistoryService historyService;
    
    private JBList<Conversation> conversationList;
    private DefaultListModel<Conversation> listModel;
    private JTextPane previewPane;
    
    private Consumer<Conversation> onSelectConversation;
    
    /**
     * Creates a new history view.
     *
     * @param project The project
     */
    public HistoryView(Project project) {
        super(new BorderLayout());
        this.project = project;
        this.historyService = ClineHistoryService.getInstance(project);
        
        setBorder(JBUI.Borders.empty(10));
        
        createUIComponents();
        loadConversations();
    }
    
    /**
     * Creates the UI components.
     */
    private void createUIComponents() {
        // Create list model and list
        listModel = new DefaultListModel<>();
        conversationList = new JBList<>(listModel);
        conversationList.setCellRenderer(new ConversationCellRenderer());
        conversationList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        conversationList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                Conversation selected = conversationList.getSelectedValue();
                if (selected != null) {
                    updatePreview(selected);
                    if (onSelectConversation != null) {
                        onSelectConversation.accept(selected);
                    }
                }
            }
        });
        
        // Create scroll pane
        JBScrollPane scrollPane = new JBScrollPane(conversationList);
        scrollPane.setBorder(JBUI.Borders.empty());
        
        // Create header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBorder(JBUI.Borders.emptyBottom(10));
        
        JLabel titleLabel = new JLabel("History"); // Shorter title
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 16f));
        
        JButton clearButton = new JButton("Clear History");
        clearButton.addActionListener(e -> clearHistory());
        
        JTextField searchField = new JTextField(20);
        searchField.putClientProperty("JTextField.variant", "search"); // Use search field style
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { filterList(); }
            @Override public void removeUpdate(DocumentEvent e) { filterList(); }
            @Override public void changedUpdate(DocumentEvent e) { filterList(); }

            private void filterList() {
                String filterText = searchField.getText().toLowerCase();
                List<Conversation> allConversations = historyService.getConversations();
                List<Conversation> filtered = allConversations.stream()
                        .filter(conv -> conversationMatches(conv, filterText))
                        .toList();
                updateList(filtered);
            }
        
            /**
             * Checks if a conversation matches the filter text.
             *
             * @param conversation The conversation
             * @param filterText   The filter text (lowercase)
             * @return True if matches, false otherwise
             */
            private boolean conversationMatches(Conversation conversation, String filterText) {
                if (filterText.isEmpty()) {
                    return true;
                }
                // Check title
                String title = conversation.getTitle();
                if (title != null && title.toLowerCase().contains(filterText)) {
                    return true;
                }
                // Check message content
                for (Message message : conversation.getMessages()) {
                    if (message.getContent() != null && message.getContent().toLowerCase().contains(filterText)) {
                        return true;
                    }
                }
                return false;
            }
        });

        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        leftPanel.add(titleLabel);
        leftPanel.add(searchField);

        headerPanel.add(leftPanel, BorderLayout.WEST);
        headerPanel.add(clearButton, BorderLayout.EAST);
        
        // Add components to main panel
        // Create preview pane
        previewPane = new JTextPane();
        previewPane.setEditable(false);
        previewPane.setBorder(JBUI.Borders.empty(5));
        JBScrollPane previewScrollPane = new JBScrollPane(previewPane);
        previewScrollPane.setBorder(JBUI.Borders.customLine(JBColor.border(), 1, 0, 0, 0));

        // Create split pane
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, scrollPane, previewScrollPane);
        splitPane.setDividerLocation(200);
        splitPane.setResizeWeight(0.3);

        add(headerPanel, BorderLayout.NORTH);
        add(splitPane, BorderLayout.CENTER);
    }
    
    /**
     * Loads conversations from the history service.
     */
    private void loadConversations() {
        List<Conversation> conversations = historyService.getConversations();
        updateList(conversations);
    }

    /**
     * Updates the preview pane with the selected conversation.
     *
     * @param conversation The selected conversation
     */
    private void updatePreview(Conversation conversation) {
        StringBuilder previewText = new StringBuilder();
        for (Message message : conversation.getMessages()) {
            previewText.append("<b>").append(message.getRole()).append(":</b><br>");
            previewText.append(message.getContent().replace("\n", "<br>")).append("<br><br>");
        }
        previewPane.setContentType("text/html");
        previewPane.setText("<html>" + previewText.toString() + "</html>");
        previewPane.setCaretPosition(0);
    }
    
    /**
     * Updates the list with the given conversations.
     *
     * @param conversations The conversations to display
     */
    private void updateList(List<Conversation> conversations) {
        listModel.clear();
        for (Conversation conversation : conversations) {
            listModel.addElement(conversation);
        }
    }
    
    /**
     * Clears the conversation history.
     */
    private void clearHistory() {
        int result = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to clear all conversation history?",
                "Clear History",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );
        
        if (result == JOptionPane.YES_OPTION) {
            historyService.clearHistory()
                    .thenRun(() -> {
                        SwingUtilities.invokeLater(() -> {
                            listModel.clear();
                            JOptionPane.showMessageDialog(
                                    this,
                                    "Conversation history cleared.",
                                    "Clear History",
                                    JOptionPane.INFORMATION_MESSAGE
                            );
                        });
                    })
                    .exceptionally(e -> {
                        LOG.error("Error clearing history", e);
                        SwingUtilities.invokeLater(() -> {
                            JOptionPane.showMessageDialog(
                                    this,
                                    "Error clearing history: " + e.getMessage(),
                                    "Error",
                                    JOptionPane.ERROR_MESSAGE
                            );
                        });
                        return null;
                    });
        }
    }
    
    /**
     * Adds a conversation to the history.
     *
     * @param conversation The conversation to add
     */
    public void addConversation(Conversation conversation) {
        historyService.addConversation(conversation)
                .thenRun(() -> {
                    SwingUtilities.invokeLater(this::loadConversations);
                })
                .exceptionally(e -> {
                    LOG.error("Error adding conversation to history", e);
                    return null;
                });
    }
    
    /**
     * Updates a conversation in the history.
     *
     * @param conversation The conversation to update
     */
    public void updateConversation(Conversation conversation) {
        historyService.updateConversation(conversation)
                .thenRun(() -> {
                    SwingUtilities.invokeLater(this::loadConversations);
                })
                .exceptionally(e -> {
                    LOG.error("Error updating conversation in history", e);
                    return null;
                });
    }
    
    /**
     * Removes a conversation from the history.
     *
     * @param conversation The conversation to remove
     */
    public void removeConversation(Conversation conversation) {
        historyService.removeConversation(conversation)
                .thenRun(() -> {
                    SwingUtilities.invokeLater(this::loadConversations);
                })
                .exceptionally(e -> {
                    LOG.error("Error removing conversation from history", e);
                    return null;
                });
    }
    
    /**
     * Sets the callback for when a conversation is selected.
     *
     * @param onSelectConversation The callback
     */
    public void setOnSelectConversation(Consumer<Conversation> onSelectConversation) {
        this.onSelectConversation = onSelectConversation;
    }
    
    /**
     * Cell renderer for conversations.
     */
    private static class ConversationCellRenderer extends DefaultListCellRenderer {
        private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("MMM d, yyyy h:mm a");
        
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            
            if (value instanceof Conversation) {
                Conversation conversation = (Conversation) value;
                
                String title = conversation.getTitle();
                if (title == null || title.isEmpty()) {
                    title = "Untitled Conversation";
                }
                
                String date = DATE_FORMAT.format(Date.from(conversation.getUpdatedAt()));
                
                label.setText("<html><b>" + title + "</b><br><small>" + date + "</small></html>");
                label.setBorder(JBUI.Borders.empty(5, 10));
            }
            
            return label;
        }
    }
}