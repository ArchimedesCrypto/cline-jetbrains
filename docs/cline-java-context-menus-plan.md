# Context Menus Plan

## Goal
Add right-click context menus to messages in ChatRow.

## Why
- React UI has context menus
- Improves usability
- Needed for parity

## Approach
- Add MouseListener to ChatRow
- On right-click, show JPopupMenu
- Menu items:
  - Copy Text
  - Copy Code (if code block)
  - Retry (if assistant)
  - Delete (if user)
  - Edit (if user)
- Implement actions for each item

## Steps
1. Add MouseListener to ChatRow
2. Create JPopupMenu
3. Add JMenuItems based on message type
4. Implement actions (copy, retry, delete, edit)
5. Test and style