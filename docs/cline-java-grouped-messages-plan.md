# Grouped Messages Refactor Plan

## Goal
Replicate the React UI's grouped message display in Java, especially for:
- Browser sessions
- Tool execution sequences
- Command sequences

## Why
- Improves readability
- Matches React UI behavior
- Enables expand/collapse of groups
- Supports special styling for groups

## Approach

### 1. Data Grouping Logic
- In `ChatView.refreshMessages()`, process `conversation.getMessages()`
- Group messages based on:
  - Browser session start/end
  - Tool execution sequences
  - Command sequences
- Create a list of groups (single message or list of messages)

### 2. UI Rendering
- For each group:
  - Single message: render with `ChatRow`
  - Group: render with `ChatGroupPanel`
    - Contains multiple `ChatRow`s
    - Has expand/collapse toggle
    - Has group header

### 3. New Classes
- `ChatGroupPanel` extends `JPanel`
  - Holds multiple `ChatRow`s
  - Handles expand/collapse
  - Styled with border, background, header

### 4. Expand/Collapse
- Default collapsed or expanded based on group type
- Toggle button in header
- Animate expand/collapse (optional)

### 5. Styling
- Group background color
- Border or shadow
- Indentation or margin

## Implementation Steps
1. Implement grouping logic in `ChatView.refreshMessages()`
2. Create `ChatGroupPanel` class
3. Update `refreshMessages()` to render groups
4. Style groups and add expand/collapse
5. Test with various message sequences
6. Compare to React UI grouping
7. Adjust until identical