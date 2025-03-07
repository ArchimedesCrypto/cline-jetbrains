/**
 * Bridge between JetBrains and Cline TypeScript code.
 * This file is compiled to bridge.js and loaded by the JavaScriptBridge class.
 */

import { ClineBridgeImpl } from './ClineBridgeImpl';
import { ClineCore } from '../core/ClineCore';

// Global variables
let executeTaskQueryName: string = '';
let cancelTaskQueryName: string = '';
let getTaskStatusQueryName: string = '';
let bridgeImpl: ClineBridgeImpl | null = null;
let clineCore: ClineCore | null = null;
let currentTaskId: string | null = null;

/**
 * Initialize the bridge.
 * @param executeTaskQuery The execute task query name
 * @param cancelTaskQuery The cancel task query name
 * @param getTaskStatusQuery The get task status query name
 */
window.initializeBridge = function(
    executeTaskQuery: string,
    cancelTaskQuery: string,
    getTaskStatusQuery: string
): void {
    console.log('Initializing bridge');
    
    // Store the query names
    executeTaskQueryName = executeTaskQuery;
    cancelTaskQueryName = cancelTaskQuery;
    getTaskStatusQueryName = getTaskStatusQuery;
    
    // Create the bridge implementation
    bridgeImpl = new ClineBridgeImpl();
    
    console.log('Bridge initialized');
};

/**
 * Execute a task.
 * @param taskInput The task input
 * @param options The options
 */
window.executeTask = async function(taskInput: string, options: string): Promise<void> {
    console.log('Executing task:', taskInput);
    
    if (!bridgeImpl) {
        console.error('Bridge not initialized');
        window[executeTaskQueryName].result('Error: Bridge not initialized');
        return;
    }
    
    try {
        // Parse the options
        const parsedOptions = JSON.parse(options);
        
        // Initialize the bridge if not already initialized
        if (!clineCore) {
            // Create the Cline core
            clineCore = new ClineCore('', {
                apiProvider: parsedOptions.apiProvider || 'openai',
                apiKey: parsedOptions.apiKey || '',
                apiModel: parsedOptions.model || 'gpt-4',
                darkMode: parsedOptions.darkMode || false,
                fontSize: parsedOptions.fontSize || 14,
                enableBrowser: parsedOptions.enableBrowser || true,
                enableTerminal: parsedOptions.enableTerminal || true,
                enableFileEditing: parsedOptions.enableFileEditing || true,
                enableAutoApproval: parsedOptions.enableAutoApproval || false,
                maxAutoApprovedRequests: parsedOptions.maxAutoApprovedRequests || 0
            });
            
            // Initialize the Cline core
            await clineCore.initialize();
        }
        
        // Execute the task
        const result = await clineCore.executeTask(taskInput, parsedOptions);
        
        // Store the task ID
        currentTaskId = result.data?.taskId || null;
        
        // Return the result
        window[executeTaskQueryName].result(JSON.stringify(result));
    } catch (error: any) {
        console.error('Failed to execute task:', error);
        window[executeTaskQueryName].result(`Error: ${error.message || String(error)}`);
    }
};

/**
 * Cancel the current task.
 */
window.cancelTask = async function(): Promise<void> {
    console.log('Cancelling task');
    
    if (!bridgeImpl) {
        console.error('Bridge not initialized');
        window[cancelTaskQueryName].result('Error: Bridge not initialized');
        return;
    }
    
    if (!clineCore) {
        console.error('Cline core not initialized');
        window[cancelTaskQueryName].result('Error: Cline core not initialized');
        return;
    }
    
    try {
        // Cancel the task
        await clineCore.cancelTask();
        
        // Return the result
        window[cancelTaskQueryName].result('Task cancelled');
    } catch (error: any) {
        console.error('Failed to cancel task:', error);
        window[cancelTaskQueryName].result(`Error: ${error.message || String(error)}`);
    }
};

/**
 * Get the status of the current task.
 */
window.getTaskStatus = async function(): Promise<void> {
    console.log('Getting task status');
    
    if (!bridgeImpl) {
        console.error('Bridge not initialized');
        window[getTaskStatusQueryName].result('Error: Bridge not initialized');
        return;
    }
    
    if (!clineCore) {
        console.error('Cline core not initialized');
        window[getTaskStatusQueryName].result('Error: Cline core not initialized');
        return;
    }
    
    try {
        // Get the task status
        const status = await clineCore.getTaskStatus();
        
        // Return the result
        window[getTaskStatusQueryName].result(JSON.stringify(status));
    } catch (error: any) {
        console.error('Failed to get task status:', error);
        window[getTaskStatusQueryName].result(`Error: ${error.message || String(error)}`);
    }
};

// Declare the global functions
declare global {
    interface Window {
        initializeBridge: (
            executeTaskQuery: string,
            cancelTaskQuery: string,
            getTaskStatusQuery: string
        ) => void;
        executeTask: (taskInput: string, options: string) => Promise<void>;
        cancelTask: () => Promise<void>;
        getTaskStatus: () => Promise<void>;
        [key: string]: any;
    }
}