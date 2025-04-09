# Image Support Plan

## Goal
Enable attaching and displaying images in chat messages.

## Why
- React UI supports images
- Needed for parity

## Approach
- Add "Attach Image" button to input panel
- Allow selecting multiple images
- Store image paths in `selectedImages` list
- When sending message, include images
- Display images inline in ChatRow
- Support multiple images per message

## Steps
1. Add attach button to input panel
2. Implement file chooser for images
3. Store selected images
4. Update sendMessage() to include images
5. Update ChatRow to display images
6. Style images
7. Test with multiple images