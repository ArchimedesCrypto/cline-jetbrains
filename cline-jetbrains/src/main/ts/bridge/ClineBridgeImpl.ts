import { ClineBridge, ClineSettings, TaskOptions, TaskResult, TaskStatus, CommandResult, FileResult, FileListResult, FileSearchResult } from './ClineBridge';
import { ClineUIBridge } from './ClineUIBridge';

/**
 * Implementation of the Cline bridge.
 * This class implements the interface between the Java/Kotlin code and the TypeScript code.
 */
export class ClineBridgeImpl implements ClineBridge {
    private projectPath: string = '';
    private settings: ClineSettings | null = null;
    private taskStatus: TaskStatus = { status: 'idle', message: 'No task running' };
    private uiBridge: ClineUIBridge | null = null;

    /**
     * Initialize the bridge.
     * @param projectPath The path of the project
     * @param settings The settings
     */
    async initialize(projectPath: string, settings: ClineSettings): Promise<void> {
        console.log(`Initializing Cline bridge for project: ${projectPath}`);
        this.projectPath = projectPath;
        this.settings = settings;

        try {
            // Initialize the UI bridge
            this.uiBridge = new ClineUIBridge(this);
            await this.uiBridge.initialize(settings);

            console.log('Cline bridge initialized successfully');
        } catch (error) {
            console.error('Failed to initialize Cline bridge', error);
            throw error;
        }
    }

    /**
     * Execute a task.
     * @param task The task to execute
     * @param options The options
     */
    async executeTask(task: string, options?: TaskOptions): Promise<TaskResult> {
        console.log(`Executing task: ${task}`);

        if (!this.uiBridge) {
            return {
                success: false,
                message: 'UI bridge not initialized'
            };
        }

        try {
            // Execute the task using the UI bridge
            return await this.uiBridge.handleTaskSubmit(task, options);
        } catch (error) {
            console.error('Failed to execute task', error);
            return {
                success: false,
                message: `Task execution failed: ${error}`
            };
        }
    }

    /**
     * Cancel the current task.
     */
    async cancelTask(): Promise<void> {
        console.log('Cancelling task');

        if (!this.uiBridge) {
            throw new Error('UI bridge not initialized');
        }

        try {
            await this.uiBridge.handleTaskCancel();
        } catch (error) {
            console.error('Failed to cancel task', error);
            throw error;
        }
    }

    /**
     * Get the status of the current task.
     */
    async getTaskStatus(): Promise<TaskStatus> {
        if (!this.uiBridge) {
            return this.taskStatus;
        }

        try {
            return this.uiBridge.getState().taskStatus;
        } catch (error) {
            console.error('Failed to get task status', error);
            return this.taskStatus;
        }
    }

    /**
     * Execute a command.
     * @param command The command to execute
     */
    async executeCommand(command: string): Promise<CommandResult> {
        console.log(`Executing command: ${command}`);

        // Send the command to Java through the jsQuery
        try {
            window.jsQuery0.result(JSON.stringify({
                type: 'command',
                command
            }));

            return {
                success: true,
                output: 'Command sent to Java'
            };
        } catch (error) {
            console.error('Failed to execute command', error);
            return {
                success: false,
                output: '',
                error: `Command execution failed: ${error}`
            };
        }
    }

    /**
     * Edit a file.
     * @param filePath The path of the file to edit
     * @param content The new content of the file
     */
    async editFile(filePath: string, content: string): Promise<FileResult> {
        console.log(`Editing file: ${filePath}`);

        try {
            window.jsQuery0.result(JSON.stringify({
                type: 'editFile',
                filePath,
                content
            }));

            return {
                success: true,
                content: 'File edit request sent to Java'
            };
        } catch (error) {
            console.error('Failed to edit file', error);
            return {
                success: false,
                error: `File editing failed: ${error}`
            };
        }
    }

    /**
     * Create a file.
     * @param filePath The path of the file to create
     * @param content The content of the file
     */
    async createFile(filePath: string, content: string): Promise<FileResult> {
        console.log(`Creating file: ${filePath}`);

        try {
            window.jsQuery0.result(JSON.stringify({
                type: 'createFile',
                filePath,
                content
            }));

            return {
                success: true,
                content: 'File creation request sent to Java'
            };
        } catch (error) {
            console.error('Failed to create file', error);
            return {
                success: false,
                error: `File creation failed: ${error}`
            };
        }
    }

    /**
     * Delete a file.
     * @param filePath The path of the file to delete
     */
    async deleteFile(filePath: string): Promise<FileResult> {
        console.log(`Deleting file: ${filePath}`);

        try {
            window.jsQuery0.result(JSON.stringify({
                type: 'deleteFile',
                filePath
            }));

            return {
                success: true,
                content: 'File deletion request sent to Java'
            };
        } catch (error) {
            console.error('Failed to delete file', error);
            return {
                success: false,
                error: `File deletion failed: ${error}`
            };
        }
    }

    /**
     * Read a file.
     * @param filePath The path of the file to read
     */
    async readFile(filePath: string): Promise<FileResult> {
        console.log(`Reading file: ${filePath}`);

        try {
            window.jsQuery0.result(JSON.stringify({
                type: 'readFile',
                filePath
            }));

            return {
                success: true,
                content: 'File read request sent to Java'
            };
        } catch (error) {
            console.error('Failed to read file', error);
            return {
                success: false,
                error: `File reading failed: ${error}`
            };
        }
    }

    /**
     * List files in a directory.
     * @param directoryPath The path of the directory to list
     * @param recursive Whether to list files recursively
     */
    async listFiles(directoryPath: string, recursive?: boolean): Promise<FileListResult> {
        console.log(`Listing files in directory: ${directoryPath}, recursive: ${recursive}`);

        try {
            window.jsQuery0.result(JSON.stringify({
                type: 'listFiles',
                directoryPath,
                recursive
            }));

            return {
                success: true,
                files: []
            };
        } catch (error) {
            console.error('Failed to list files', error);
            return {
                success: false,
                files: [],
                error: `File listing failed: ${error}`
            };
        }
    }

    /**
     * Search for files.
     * @param pattern The pattern to search for
     * @param directoryPath The path of the directory to search in
     */
    async searchFiles(pattern: string, directoryPath: string): Promise<FileSearchResult> {
        console.log(`Searching for files with pattern: ${pattern} in directory: ${directoryPath}`);

        try {
            window.jsQuery0.result(JSON.stringify({
                type: 'searchFiles',
                pattern,
                directoryPath
            }));

            return {
                success: true,
                matches: []
            };
        } catch (error) {
            console.error('Failed to search files', error);
            return {
                success: false,
                matches: [],
                error: `File searching failed: ${error}`
            };
        }
    }
}

/**
 * Create a new instance of the Cline bridge.
 * This function is called from the Java/Kotlin code.
 */
export function createClineBridge(): ClineBridge {
    return new ClineBridgeImpl();
}

// Make the createClineBridge function available to the Java/Kotlin code
(window as any).createClineBridge = createClineBridge;