import { ClineBridge, ClineSettings, TaskOptions, TaskResult, TaskStatus, CommandResult, FileResult, FileListResult, FileSearchResult } from './ClineBridge';
import { ClineCore } from '../core/ClineCore';

/**
 * Implementation of the Cline bridge.
 * This class implements the interface between the Java/Kotlin code and the TypeScript code.
 */
export class ClineBridgeImpl implements ClineBridge {
    private projectPath: string = '';
    private settings: ClineSettings | null = null;
    private taskStatus: TaskStatus = { status: 'idle', message: 'No task running' };
    private clineCore: ClineCore | null = null;
    
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
            // Initialize the Cline core
            this.clineCore = new ClineCore(projectPath, settings);
            await this.clineCore.initialize();
            
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
        
        if (!this.clineCore) {
            return {
                success: false,
                message: 'Cline core not initialized'
            };
        }
        
        // Update task status
        this.taskStatus = { status: 'running', message: 'Task is running', progress: 0 };
        
        try {
            // Execute the task using the Cline core
            const result = await this.clineCore.executeTask(task, options);
            
            // Update task status
            this.taskStatus = { status: 'completed', message: 'Task completed', progress: 100 };
            
            return result;
        } catch (error) {
            // Update task status
            this.taskStatus = { status: 'failed', message: `Task failed: ${error}` };
            
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
        
        if (!this.clineCore) {
            throw new Error('Cline core not initialized');
        }
        
        try {
            // Cancel the task using the Cline core
            await this.clineCore.cancelTask();
            
            // Update task status
            this.taskStatus = { status: 'idle', message: 'Task cancelled' };
        } catch (error) {
            console.error('Failed to cancel task', error);
            throw error;
        }
    }
    
    /**
     * Get the status of the current task.
     */
    async getTaskStatus(): Promise<TaskStatus> {
        if (!this.clineCore) {
            return this.taskStatus;
        }
        
        try {
            // Get the task status from the Cline core
            return await this.clineCore.getTaskStatus();
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
        
        if (!this.clineCore) {
            return {
                success: false,
                output: 'Cline core not initialized'
            };
        }
        
        try {
            // Execute the command using the Cline core
            const output = await this.clineCore.executeCommand(command);
            
            return {
                success: true,
                output
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
        
        if (!this.clineCore) {
            return {
                success: false,
                error: 'Cline core not initialized'
            };
        }
        
        try {
            // Edit the file using the Cline core
            const result = await this.clineCore.editFile(filePath, content);
            
            return {
                success: true,
                content: result
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
        
        if (!this.clineCore) {
            return {
                success: false,
                error: 'Cline core not initialized'
            };
        }
        
        try {
            // Create the file using the Cline core
            const result = await this.clineCore.createFile(filePath, content);
            
            return {
                success: true,
                content: result
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
        
        if (!this.clineCore) {
            return {
                success: false,
                error: 'Cline core not initialized'
            };
        }
        
        try {
            // Delete the file using the Cline core
            const result = await this.clineCore.deleteFile(filePath);
            
            return {
                success: true,
                content: result
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
        
        if (!this.clineCore) {
            return {
                success: false,
                error: 'Cline core not initialized'
            };
        }
        
        try {
            // Read the file using the Cline core
            const content = await this.clineCore.readFile(filePath);
            
            return {
                success: true,
                content
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
        
        if (!this.clineCore) {
            return {
                success: false,
                files: [],
                error: 'Cline core not initialized'
            };
        }
        
        try {
            // List the files using the Cline core
            const files = await this.clineCore.listFiles(directoryPath, recursive);
            
            return {
                success: true,
                files
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
        
        if (!this.clineCore) {
            return {
                success: false,
                matches: [],
                error: 'Cline core not initialized'
            };
        }
        
        try {
            // Search for the files using the Cline core
            const files = await this.clineCore.searchFiles(pattern, directoryPath);
            
            // Convert the files to matches
            const matches = files.map(file => ({
                file,
                line: 1,
                content: `Match for pattern: ${pattern}`
            }));
            
            return {
                success: true,
                matches
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