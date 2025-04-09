# Virtualized Scrolling Plan

## Goal
Improve performance of ChatView with large conversations by virtualizing message rendering.

## Why
- Current BoxLayout renders all messages, causing lag with many messages.
- React uses react-virtuoso for virtualization.
- Java should use JList with custom renderer.

## Approach
- Replace messagesPanel + JScrollPane with a JList inside JScrollPane.
- Use DefaultListModel to hold message groups.
- Use custom ListCellRenderer:
  - Renders ChatRow for single message
  - Renders ChatGroupPanel for groups
- Update refreshMessages() to populate the list model with groups.
- Support scrolling, selection, and expand/collapse.

## Steps
1. Create DefaultListModel<List<Message>>.
2. Create JList with custom renderer.
3. Replace messagesPanel with JList in ChatView.
4. Update refreshMessages() to populate model.
5. Test with large conversations.
6. Style and optimize.