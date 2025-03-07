# Cline for JetBrains IDEs

This is the JetBrains version of [Cline](https://github.com/cline/cline), an AI assistant that can use your CLI and Editor to help with complex software development tasks.

## Features

- AI-assisted coding with Claude 3.5 Sonnet's agentic capabilities
- Create and edit files
- Execute terminal commands
- Use the browser for interactive debugging
- Explore large projects
- Extend capabilities with MCP

## Requirements

- JetBrains IDE (IntelliJ IDEA, WebStorm, PyCharm, etc.) version 2023.3 or later
- Java 17 or later
- Gradle 8.0 or later
- Node.js 18 or later (for TypeScript development)

## Building the Plugin

1. Clone the repository:
   ```bash
   git clone https://github.com/cline/cline-jetbrains.git
   cd cline-jetbrains
   ```

2. Build the plugin:
   ```bash
   ./scripts/build.sh
   ```

3. The plugin JAR file will be located at `build/libs/cline-jetbrains-1.0.0.jar`.

## Running the Plugin

1. Run the plugin in the IDE:
   ```bash
   ./scripts/build.sh --run
   ```

2. Alternatively, you can install the plugin manually:
   - Open your JetBrains IDE
   - Go to Settings/Preferences > Plugins
   - Click on the gear icon and select "Install Plugin from Disk..."
   - Select the plugin JAR file

## Development

### Project Structure

- `src/main/java/com/cline/jetbrains/`: Java/Kotlin code for JetBrains integration
- `src/main/ts/`: TypeScript code for the bridge and adapters
- `src/main/resources/`: Resources (icons, plugin.xml, etc.)
- `scripts/`: Build and sync scripts

### Syncing with the Main Repository

To sync changes from the main Cline repository:

```bash
./scripts/sync.sh
```

This script will:
1. Clone the main repository if it doesn't exist
2. Fetch the latest changes
3. Copy the necessary files to the JetBrains plugin
4. Update the bridge and adapter files if needed
5. Build the TypeScript code

### TypeScript Development

The TypeScript code is located in the `src/main/ts/` directory. It consists of:

- `bridge/`: Bridge interface between Java/Kotlin and TypeScript
- `adapters/`: Adapters for the existing Cline TypeScript code
- `core/`: Core functionality copied from the main repository

To build the TypeScript code:

```bash
cd src/main/ts
npx tsc
```

## Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

## License

[Apache 2.0 © 2025 Cline Bot Inc.](../LICENSE)