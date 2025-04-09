# History View Parity Plan

## Goal
Replicate the React HistoryView UI and functionality.

## Why
- React UI has history browsing
- Needed for parity
- User access to past conversations

## Approach
- Update HistoryView.java (JPanel)
- Use JList to display conversation history (titles/timestamps)
- Show preview panel for selected conversation
- Allow selecting conversation to load in ChatView
- Add search/filter field
- Style to match React UI

## Steps
1. Add JList for conversation history
2. Add preview panel
3. Implement conversation loading from service
4. Implement selection handling
5. Add search/filter logic
6. Style components and layout
7. Test