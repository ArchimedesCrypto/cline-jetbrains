package com.cline.ui.history;

import com.cline.core.model.Conversation;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.ui.components.JBList;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.util.ui.JBUI;

import javax.swing.*;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
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
    private final List<Conversation> conversations = new ArrayList<>();
    
    private JBList<Conversation> conversationList;
    private DefaultListModel<Conversation> listModel;
    
    private Consumer<Conversation> onSelectConversation;
    
    /**
     * Creates a new history view.
     *
     * @param project The project
     */
    public HistoryView(Project project) {
        super(new BorderLayout());
        this.project = project;
        
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
                if (selected != null && onSelectConversation != null) {
                    onSelectConversation.accept(selected);
                }
            }
        });
        
        // Create scroll pane
        JBScrollPane scrollPane = new JBScrollPane(conversationList);
        scrollPane.setBorder(JBUI.Borders.empty());
        
        // Create header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBorder(JBUI.Borders.emptyBottom(10));
        
        JLabel titleLabel = new JLabel("Conversation History");
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 16f));
        
        JButton clearButton = new JButton("Clear History");
        clearButton.addActionListener(e -> clearHistory());
        
        headerPanel.add(titleLabel, BorderLayout.WEST);
        headerPanel.add(clearButton, BorderLayout.EAST);
        
        // Add components to main panel
        add(headerPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
    }
    
    /**
     * Loads conversations from storage.
     */
    private void loadConversations() {
        // TODO: Load conversations from storage
        // For now, we'll just add some dummy conversations
        for (int i = 1; i <= 5; i++) {
            Conversation conversation = Conversation.createEmpty();
            conversation.setTitle("Conversation " + i);
            conversations.add(conversation);
        }
        
        updateList();
    }
    
    /**
     * Updates the list with the current conversations.
     */
    private void updateList() {
        listModel.clear();
        for (Conversation conversation : conversations) {
            listModel.addElement(conversation);
        }
    }
    
    /**
     * Clears the conversation history.
     */
    private void clearHistory() {
        // TODO: Clear conversations from storage
        conversations.clear();
        updateList();
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