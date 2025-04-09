# MCP Overlay Plan

## Goal
Implement the MCP view overlay.

## Why
- React UI has MCP view
- Needed for parity
- Manage MCP servers, tools, resources

## Approach
- Create McpView.java (JPanel)
- Add UI elements:
  - List of connected servers
  - List of tools/resources for selected server
  - Connect/Disconnect buttons
- Style to match React UI
- Add to ClineToolWindowContent overlay
- Add toolbar button to toggle

## Steps
1. Create McpView.java
2. Add UI components (lists, buttons)
3. Style the view
4. Add to ClineToolWindowContent overlay
5. Add toolbar button and toggle logic
6. Implement server connection/disconnection
7. Implement tool/resource display
8. Test