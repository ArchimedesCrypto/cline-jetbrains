# Cline JetBrains Java Implementation

This is a pure Java implementation of the Cline plugin for JetBrains IDEs. It provides the same functionality as the VSCode version but is built using JetBrains' UI components and APIs.

## Version 0.0.4 Release

The latest version (0.0.4) includes several major enhancements:

- **Multiple API Providers**: Support for both Anthropic (Claude) and OpenAI models
- **Azure OpenAI Support**: Integration with Azure OpenAI services
- **Prompt Caching**: Improved performance with Claude 3.5 prompt caching
- **MCP Integration**: Support for Model Context Protocol servers
- **Auto-approval for Tools**: Configure tools to be automatically approved
- **Enhanced Browser Functionality**: Improved browser integration with screenshot capture
- **Improved Error Handling**: Better error handling and stability

## Features

- **Chat Interface**: Interact with AI models directly within your JetBrains IDE
- **Markdown Rendering**: Display rich markdown content in messages
- **Code Block Rendering**: Syntax highlighting for code blocks
- **Image Support**: Display images in messages
- **Tool Execution**: Execute tools requested by the AI
- **Terminal Integration**: Execute commands in the terminal
- **File System Operations**: Access and modify files
- **Browser Integration**: Open URLs and local files in the browser
- **Multiple API Providers**: Choose between Anthropic and OpenAI models
- **MCP Integration**: Connect to Model Context Protocol servers
- **Auto-approval**: Configure tools to be automatically approved

## Architecture

The plugin follows a modular architecture with clear separation of concerns:

### UI Layer
- **ClineToolWindowFactory**: Creates the tool window for the plugin
- **ClineToolWindowContent**: Main container for all UI components
- **ChatView**: Displays the chat interface
- **ChatRow**: Renders individual messages
- **HistoryView**: Displays conversation history
- **SettingsView**: Provides UI for configuring the plugin
- **ToolTestView**: Allows testing tools directly

### Service Layer
- **ClineApiService**: Handles communication with the AI API
- **ApiProvider**: Interface for API providers
  - **AnthropicProvider**: Provider for Claude models
  - **OpenAiProvider**: Provider for OpenAI models (including Azure)
- **ClineSettingsService**: Manages plugin settings
- **ClineFileService**: Provides file system operations
- **ClineTerminalService**: Handles terminal command execution
- **ClineBrowserService**: Provides browser functionality
  - **BrowserSession**: Interface for browser sessions
  - **JxBrowserSession**: Implementation using JxBrowser
- **ClineMcpService**: Manages Model Context Protocol servers
- **ClineHistoryService**: Manages conversation history

### Core Layer
- **Message**: Represents a single message in a conversation
- **Conversation**: Represents a complete conversation
- **MessageRole**: Enum for message roles (user, assistant, system, tool)
- **Tool**: Interface for tools that can be executed
- **ToolResult**: Represents the result of a tool execution
- **ToolExecutor**: Manages tool registration and execution
- **AutoApprovalSettings**: Settings for tool auto-approval
- **McpServer**: Interface for MCP servers
- **McpTool**: Represents a tool provided by an MCP server
- **McpResource**: Represents a resource provided by an MCP server

### Rendering Components
- **MarkdownRenderer**: Renders markdown content
- **CodeBlockRenderer**: Renders code blocks with syntax highlighting
- **ImageRenderer**: Renders images in messages

## Tools

The plugin provides a set of tools that can be executed by the AI:

### File Tools
- **ReadFileTool**: Read the contents of a file
- **WriteToFileTool**: Write content to a file
- **ApplyDiffTool**: Apply a diff to a file
- **SearchFilesTool**: Search files with a regex pattern
- **ListFilesTool**: List files in a directory
- **ListCodeDefinitionsTool**: List code definitions in a directory

### Command Tools
- **ExecuteCommandTool**: Execute a CLI command

### Browser Tools
- **BrowserActionTool**: Interact with a browser (launch, click, type, scroll, close)

### MCP Tools
- **UseMcpToolTool**: Execute a tool provided by an MCP server
- **AccessMcpResourceTool**: Access a resource provided by an MCP server

### Interaction Tools
- **AskFollowupQuestionTool**: Ask the user a follow-up question
- **AttemptCompletionTool**: Attempt to complete a task
- **SwitchModeTool**: Switch to a different mode
- **NewTaskTool**: Create a new task

## Development

### Prerequisites
- Java Development Kit (JDK) 17 or later
- Gradle 8.0 or later
- IntelliJ IDEA (for development)

### Building the Plugin
```bash
./gradlew build
```

### Running the Plugin
```bash
./gradlew runIde
```

### Testing the Plugin
```bash
./gradlew test
```

### Packaging the Plugin
```bash
./gradlew buildPlugin
```

## Implementation Details

### API Communication
The `ClineApiService` handles communication with the AI API. It supports both streaming and non-streaming responses, and can handle tool execution requests. The service uses the `ApiProvider` interface to abstract the communication with different AI providers.

### API Providers
- **AnthropicProvider**: Implements the `ApiProvider` interface for Claude models. It supports prompt caching for Claude 3.5 to improve performance.
- **OpenAiProvider**: Implements the `ApiProvider` interface for OpenAI models. It supports both standard OpenAI and Azure OpenAI endpoints.

### MCP Integration
The `ClineMcpService` manages Model Context Protocol servers. It can connect to MCP servers, execute tools provided by these servers, and access resources from them.

### Auto-approval for Tools
The `AutoApprovalSettings` class provides settings for tool auto-approval. It allows users to configure which tools should be automatically approved without requiring explicit confirmation.

### Markdown Rendering
The `MarkdownRenderer` uses the CommonMark library to parse markdown and render it as HTML in a JTextPane.

### Code Block Rendering
The `CodeBlockRenderer` uses JetBrains' editor components to render code blocks with syntax highlighting.

### Image Support
The `ImageRenderer` can load images from URLs, files, and base64 data, and display them in the chat.

### Tool Execution
The `ToolExecutor` manages tool registration and execution. It can execute tools requested by the AI and continue the conversation with the tool result.

### Terminal Integration
The `ClineTerminalService` can execute commands in the terminal and capture the output.

### File System Operations
The `ClineFileService` provides file system operations such as reading, writing, and searching files.

### Browser Integration
The `ClineBrowserService` can open URLs and local files in the browser. It uses the `BrowserSession` interface to abstract the browser implementation, with `JxBrowserSession` providing a Chromium-based browser implementation.

## Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

## License

This project is licensed under the MIT License - see the LICENSE file for details.