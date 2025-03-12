# Cline JetBrains Java Implementation

This is a pure Java implementation of the Cline plugin for JetBrains IDEs. It provides the same functionality as the VSCode version but is built using JetBrains' UI components and APIs.

## Features

- **Chat Interface**: Interact with AI models directly within your JetBrains IDE
- **Markdown Rendering**: Display rich markdown content in messages
- **Code Block Rendering**: Syntax highlighting for code blocks
- **Image Support**: Display images in messages
- **Tool Execution**: Execute tools requested by the AI
- **Terminal Integration**: Execute commands in the terminal
- **File System Operations**: Access and modify files
- **Browser Integration**: Open URLs and local files in the browser

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
- **ClineSettingsService**: Manages plugin settings
- **ClineFileService**: Provides file system operations
- **ClineTerminalService**: Handles terminal command execution
- **ClineBrowserService**: Provides browser functionality
- **ClineHistoryService**: Manages conversation history

### Core Layer
- **Message**: Represents a single message in a conversation
- **Conversation**: Represents a complete conversation
- **MessageRole**: Enum for message roles (user, assistant, system, tool)
- **Tool**: Interface for tools that can be executed
- **ToolResult**: Represents the result of a tool execution
- **ToolExecutor**: Manages tool registration and execution

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

### Interaction Tools
- **AskFollowupQuestionTool**: Ask the user a follow-up question
- **AttemptCompletionTool**: Attempt to complete a task

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
The `ClineApiService` handles communication with the AI API. It supports both streaming and non-streaming responses, and can handle tool execution requests.

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
The `ClineBrowserService` can open URLs and local files in the browser.

## Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

## License

This project is licensed under the MIT License - see the LICENSE file for details.