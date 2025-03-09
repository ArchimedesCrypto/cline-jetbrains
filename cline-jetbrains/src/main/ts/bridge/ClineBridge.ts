/**
 * Interface for task options.
 */
export interface TaskOptions {
    mode?: string;
    model?: string;
    context?: string[];
    images?: string[];
}

/**
 * Interface for task status.
 */
export interface TaskStatus {
    status: 'idle' | 'running' | 'completed' | 'failed';
    message: string;
    progress?: number;
}

/**
 * Interface for task result.
 */
export interface TaskResult {
    success: boolean;
    message: string;
    output?: string;
    error?: string;
}

/**
 * Interface for command result.
 */
export interface CommandResult {
    success: boolean;
    output: string;
    error?: string;
}

/**
 * Interface for file result.
 */
export interface FileResult {
    success: boolean;
    content?: string;
    error?: string;
}

/**
 * Interface for file list result.
 */
export interface FileListResult {
    success: boolean;
    files: string[];
    error?: string;
}

/**
 * Interface for file search match.
 */
export interface FileSearchMatch {
    file: string;
    line: number;
    content: string;
}

/**
 * Interface for file search result.
 */
export interface FileSearchResult {
    success: boolean;
    matches: FileSearchMatch[];
    error?: string;
}

/**
 * Interface for Cline settings.
 */
export interface ClineSettings {
    apiKey?: string;
    model?: string;
    mode?: string;
    theme?: string;
    fontSize?: number;
    autoApprove?: boolean;
}

/**
 * Interface for the Cline bridge.
 * This interface defines the methods that can be called from Java code.
 */
export interface ClineBridge {
    /**
     * Initialize the bridge.
     * @param projectPath The path of the project
     * @param settings The settings
     */
    initialize(projectPath: string, settings: ClineSettings): Promise<void>;

    /**
     * Execute a task.
     * @param task The task to execute
     * @param options The options
     */
    executeTask(task: string, options?: TaskOptions): Promise<TaskResult>;

    /**
     * Cancel the current task.
     */
    cancelTask(): Promise<void>;

    /**
     * Get the status of the current task.
     */
    getTaskStatus(): Promise<TaskStatus>;

    /**
     * Execute a command.
     * @param command The command to execute
     */
    executeCommand(command: string): Promise<CommandResult>;

    /**
     * Edit a file.
     * @param filePath The path of the file to edit
     * @param content The new content of the file
     */
    editFile(filePath: string, content: string): Promise<FileResult>;

    /**
     * Create a file.
     * @param filePath The path of the file to create
     * @param content The content of the file
     */
    createFile(filePath: string, content: string): Promise<FileResult>;

    /**
     * Delete a file.
     * @param filePath The path of the file to delete
     */
    deleteFile(filePath: string): Promise<FileResult>;

    /**
     * Read a file.
     * @param filePath The path of the file to read
     */
    readFile(filePath: string): Promise<FileResult>;

    /**
     * List files in a directory.
     * @param directoryPath The path of the directory to list
     * @param recursive Whether to list files recursively
     */
    listFiles(directoryPath: string, recursive?: boolean): Promise<FileListResult>;

    /**
     * Search for files.
     * @param pattern The pattern to search for
     * @param directoryPath The path of the directory to search in
     */
    searchFiles(pattern: string, directoryPath: string): Promise<FileSearchResult>;
}