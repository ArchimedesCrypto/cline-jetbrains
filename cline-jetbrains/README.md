# Cline for JetBrains IDEs

Cline is an AI-assisted coding agent that can use your CLI and Editor to help with complex software development tasks. This is the JetBrains plugin version of Cline, which works with IntelliJ IDEA, WebStorm, PyCharm, and other JetBrains IDEs.

## Features

- AI-assisted coding with support for multiple models (GPT-4, Claude, etc.)
- File creation, editing, and navigation
- Terminal command execution
- Browser interaction
- Task history and management

## Development

### Prerequisites

- JDK 17 or later
- Node.js 16 or later
- Gradle 7.6 or later
- JetBrains IDE (IntelliJ IDEA, WebStorm, PyCharm, etc.)

### Setup

1. Clone the repository:

```bash
git clone https://github.com/ArchimedesCrypto/cline.git
cd cline
git checkout jetbrains
```

2. Install dependencies:

```bash
cd cline-jetbrains
npm install
```

3. Sync with the main Cline repository:

```bash
./scripts/sync.sh
```

4. Build the plugin:

```bash
./scripts/build.sh
```

### Testing

#### Running Tests

To run the Java tests:

```bash
./gradlew test
```

To run the TypeScript tests:

```bash
npm test
```

To run all tests:

```bash
./scripts/test.sh
```

#### Manual Testing

1. Build the plugin:

```bash
./scripts/build.sh
```

2. Install the plugin in a JetBrains IDE:

   - Open the JetBrains IDE
   - Go to Settings/Preferences > Plugins
   - Click on the gear icon and select "Install Plugin from Disk..."
   - Navigate to `cline-jetbrains/build/distributions/cline-jetbrains-1.0.0.zip`
   - Click "OK" and restart the IDE

3. Test the plugin:

   - Open a project in the IDE
   - Open the Cline tool window by clicking on the Cline icon in the tool window bar
   - Create a new task by entering a prompt in the "New Task" tab
   - Test the various features of the plugin:
     - File creation and editing
     - Terminal command execution
     - Task history and management

### Debugging

To debug the plugin:

1. Run the IDE with the plugin in debug mode:

```bash
./gradlew runIde --debug-jvm
```

2. Connect to the debug port (default: 5005) from your IDE.

## Architecture

The Cline JetBrains plugin is built using the following architecture:

1. **Java/Kotlin Layer**: This layer handles the JetBrains IDE integration, including UI components, services, and actions.

2. **TypeScript Bridge**: This layer provides a bridge between the Java/Kotlin layer and the TypeScript layer, allowing communication between the two.

3. **TypeScript Layer**: This layer contains the core Cline functionality, including the AI model integration, task execution, and file operations.

## Contributing

Contributions are welcome! Please see the [CONTRIBUTING.md](../CONTRIBUTING.md) file for more information.

## License

This project is licensed under the MIT License - see the [LICENSE](../LICENSE) file for details.