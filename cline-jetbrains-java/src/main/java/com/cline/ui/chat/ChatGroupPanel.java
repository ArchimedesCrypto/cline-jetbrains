package com.cline.ui.chat;

import com.cline.core.model.Message;
import com.intellij.ui.JBColor;
import com.intellij.util.ui.JBUI;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class ChatGroupPanel extends JPanel {
    private boolean expanded = true;
    private final JPanel contentPanel;

    public ChatGroupPanel(@NotNull List<Message> messages, @NotNull Project project) {
        setLayout(new BorderLayout());
        setBorder(JBUI.Borders.customLine(JBColor.border(), 1));
        setBackground(JBColor.namedColor("ToolWindow.background", JBColor.background().brighter()));

        JButton toggleButton = new JButton("-");
        toggleButton.setFocusPainted(false);
        toggleButton.setBorderPainted(false);
        toggleButton.setContentAreaFilled(false);
        toggleButton.addActionListener(e -> toggle());

        JLabel headerLabel = new JLabel("Grouped Messages (" + messages.size() + ")");
        headerLabel.setFont(headerLabel.getFont().deriveFont(Font.BOLD));

        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(getBackground());
        headerPanel.add(headerLabel, BorderLayout.CENTER);
        headerPanel.add(toggleButton, BorderLayout.EAST);
        headerPanel.setBorder(JBUI.Borders.empty(5));

        add(headerPanel, BorderLayout.NORTH);

        contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBorder(JBUI.Borders.empty(5));

        for (Message message : messages) {
            ChatRow chatRow = new ChatRow(message, project);
            contentPanel.add(chatRow);
        }

        add(contentPanel, BorderLayout.CENTER);
    }

    private void toggle() {
        expanded = !expanded;
        // Basic visibility toggle for now, animation can be added later
        contentPanel.setVisible(expanded);
        // Update toggle button icon
        ((JButton) ((JPanel) getComponent(0)).getComponent(1)).setIcon(expanded ? com.intellij.icons.AllIcons.General.ArrowDown : com.intellij.icons.AllIcons.General.ArrowUp); // Corrected icons
        // Ensure parent revalidates to adjust layout
        if (getParent() != null) {
            getParent().revalidate();
            getParent().repaint();
        } else {
            revalidate();
            repaint();
        }
    }
}