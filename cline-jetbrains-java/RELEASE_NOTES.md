# Cline JetBrains Java Implementation Release Notes

## Version 0.0.4 (March 13, 2025)

### Major Features

- **Multiple API Providers**
  - Added support for both Anthropic (Claude) and OpenAI models
  - Implemented `ApiProvider` interface for abstracting API communication
  - Created `AnthropicProvider` for Claude models
  - Created `OpenAiProvider` for OpenAI models
  - Added support for Azure OpenAI endpoints

- **Prompt Caching**
  - Implemented prompt caching for Claude 3.5
  - Added cache control headers to API requests
  - Improved performance for repeated requests

- **MCP Integration**
  - Added support for Model Context Protocol servers
  - Implemented `McpServer` interface for MCP servers
  - Created `McpTool` and `McpResource` classes
  - Added `ClineMcpService` for managing MCP servers
  - Implemented MCP tool and resource access functionality

- **Auto-approval for Tools**
  - Added `AutoApprovalSettings` for tool auto-approval
  - Implemented tool-specific auto-approval configuration
  - Added settings UI for configuring auto-approval

- **Enhanced Browser Functionality**
  - Implemented `BrowserSession` interface for browser sessions
  - Created `JxBrowserSession` implementation using JxBrowser
  - Added support for screenshot capture and console log capture
  - Improved browser settings configuration

### Improvements

- **Error Handling**
  - Improved error handling in API communication
  - Added better error messages for failed API requests
  - Enhanced error recovery mechanisms

- **Testing**
  - Added tests for API providers
  - Implemented tests for MCP functionality
  - Added tests for auto-approval settings
  - Fixed test failures related to browser functionality

- **Documentation**
  - Updated README with information about new features
  - Added detailed implementation documentation
  - Improved code comments and JavaDoc

### Bug Fixes

- Fixed issues with browser functionality
- Resolved API communication edge cases
- Fixed file system operation errors
- Addressed UI rendering issues

## Version 0.0.1 (February 15, 2025)

### Initial Release

- Complete rewrite in Java
- Feature parity with VSCode version
- Native JetBrains UI components
- Basic API communication
- Tool execution functionality
- Terminal command execution
- File system operations
- Browser functionality (stub implementation)
- Markdown rendering, image support, and code block rendering