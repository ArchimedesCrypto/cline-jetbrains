package com.cline.ui.chat;

import com.cline.core.model.Conversation;
import com.cline.core.model.Message;
import com.intellij.icons.AllIcons;
import com.intellij.ui.JBColor;
import com.intellij.ui.components.JBLabel;
import com.intellij.util.ui.JBUI;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Header component for displaying task information.
 * This is the Java equivalent of the TaskHeader.tsx component in the TypeScript version.
 */
public class TaskHeader extends JPanel {
    private static final Pattern MENTION_PATTERN = Pattern.compile("@([a-zA-Z0-9_-]+)");
    
    private final Message task;
    private final Conversation conversation;
    
    /**
     * Creates a new task header.
     *
     * @param task The task message
     * @param conversation The conversation
     */
    public TaskHeader(@NotNull Message task, @NotNull Conversation conversation) {
        this.task = task;
        this.conversation = conversation;
        
        setLayout(new BorderLayout());
        setBorder(JBUI.Borders.compound(
                JBUI.Borders.customLine(JBColor.border(), 0, 0, 1, 0),
                JBUI.Borders.empty(10)
        ));
        
        createUIComponents();
    }
    
    /**
     * Creates the UI components.
     */
    private void createUIComponents() {
        // Task info panel (left side)
        JPanel taskInfoPanel = new JPanel(new BorderLayout());
        taskInfoPanel.setOpaque(false);
        
        // Task text
        JTextArea taskTextArea = new JTextArea(task.getContent());
        taskTextArea.setEditable(false);
        taskTextArea.setWrapStyleWord(true);
        taskTextArea.setLineWrap(true);
        taskTextArea.setOpaque(false);
        taskTextArea.setFont(taskTextArea.getFont().deriveFont(Font.BOLD));
        taskTextArea.setBorder(JBUI.Borders.empty());
        
        // Task timestamp
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM d, yyyy h:mm a");
        String timestamp = dateFormat.format(Date.from(task.getTimestamp()));
        
        JBLabel timestampLabel = new JBLabel(timestamp);
        timestampLabel.setForeground(JBColor.gray);
        timestampLabel.setFont(timestampLabel.getFont().deriveFont(Font.PLAIN, 11f));
        timestampLabel.setBorder(JBUI.Borders.empty(5, 0, 0, 0));
        
        taskInfoPanel.add(taskTextArea, BorderLayout.CENTER);
        taskInfoPanel.add(timestampLabel, BorderLayout.SOUTH);
        
        // Metrics panel (right side)
        JPanel metricsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        metricsPanel.setOpaque(false);
        
        // TODO: Add metrics like tokens in/out, cost, etc.
        
        // Close button
        JButton closeButton = new JButton(AllIcons.Actions.Close);
        closeButton.setBorderPainted(false);
        closeButton.setContentAreaFilled(false);
        closeButton.setFocusPainted(false);
        closeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // TODO: Implement close action
            }
        });
        
        // Add components to main panel
        add(taskInfoPanel, BorderLayout.CENTER);
        add(metricsPanel, BorderLayout.EAST);
        add(closeButton, BorderLayout.WEST);
    }
    
    /**
     * Highlights mentions in the text.
     *
     * @param text The text to highlight
     * @return The highlighted text
     */
    public static String highlightMentions(String text) {
        if (text == null) return "";
        
        StringBuffer result = new StringBuffer();
        Matcher matcher = MENTION_PATTERN.matcher(text);
        
        while (matcher.find()) {
            matcher.appendReplacement(result, "<b>@" + matcher.group(1) + "</b>");
        }
        
        matcher.appendTail(result);
        return result.toString();
    }
}