package com.cline.ui.chat;

import com.cline.core.model.Message;
import com.intellij.openapi.project.Project;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class ChatListCellRenderer implements ListCellRenderer<List<Message>> {
    private final Project project;

    public ChatListCellRenderer(Project project) {
        this.project = project;
    }

    @Override
    public Component getListCellRendererComponent(JList<? extends List<Message>> list, List<Message> value, int index, boolean isSelected, boolean cellHasFocus) {
        if (value.size() == 1) {
            return new ChatRow(value.get(0), project);
        } else {
            return new ChatGroupPanel(value, project);
        }
    }
}