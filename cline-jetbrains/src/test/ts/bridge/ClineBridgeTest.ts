/**
 * Tests for the Cline bridge.
 */

import { ClineBridge } from '../../../main/ts/bridge/ClineBridge';
import { ClineBridgeImpl } from '../../../main/ts/bridge/ClineBridgeImpl';

// Import Jest types
import { describe, beforeEach, test, expect } from '@jest/globals';

describe('ClineBridge', () => {
    let bridge: ClineBridge;
    
    beforeEach(() => {
        // Create a new bridge instance for each test
        bridge = new ClineBridgeImpl();
    });
    
    test('initialize', async () => {
        // Define test settings
        const projectPath = '/test/project';
        const settings = {
            apiProvider: 'openai',
            apiKey: 'test-api-key',
            apiModel: 'gpt-4',
            darkMode: false,
            fontSize: 14,
            enableBrowser: true,
            enableTerminal: true,
            enableFileEditing: true,
            enableAutoApproval: false,
            maxAutoApprovedRequests: 10
        };
        
        // Initialize the bridge
        await bridge.initialize(projectPath, settings);
        
        // Get the task status
        const status = await bridge.getTaskStatus();
        
        // Check that the status is idle
        expect(status.status).toBe('idle');
        expect(status.message).toBe('No task running');
    });
    
    test('executeTask', async () => {
        // Define test settings
        const projectPath = '/test/project';
        const settings = {
            apiProvider: 'openai',
            apiKey: 'test-api-key',
            apiModel: 'gpt-4',
            darkMode: false,
            fontSize: 14,
            enableBrowser: true,
            enableTerminal: true,
            enableFileEditing: true,
            enableAutoApproval: false,
            maxAutoApprovedRequests: 10
        };
        
        // Initialize the bridge
        await bridge.initialize(projectPath, settings);
        
        // Execute a task
        const result = await bridge.executeTask('Test task');
        
        // Check that the task was executed successfully
        expect(result.success).toBe(true);
        expect(result.message).toBe('Task executed successfully');
        
        // Get the task status
        const status = await bridge.getTaskStatus();
        
        // Check that the status is completed
        expect(status.status).toBe('completed');
        expect(status.message).toBe('Task completed');
        expect(status.progress).toBe(100);
    });
    
    test('cancelTask', async () => {
        // Define test settings
        const projectPath = '/test/project';
        const settings = {
            apiProvider: 'openai',
            apiKey: 'test-api-key',
            apiModel: 'gpt-4',
            darkMode: false,
            fontSize: 14,
            enableBrowser: true,
            enableTerminal: true,
            enableFileEditing: true,
            enableAutoApproval: false,
            maxAutoApprovedRequests: 10
        };
        
        // Initialize the bridge
        await bridge.initialize(projectPath, settings);
        
        // Execute a task (don't await it)
        const taskPromise = bridge.executeTask('Test task');
        
        // Cancel the task
        await bridge.cancelTask();
        
        // Get the task status
        const status = await bridge.getTaskStatus();
        
        // Check that the status is idle
        expect(status.status).toBe('idle');
        expect(status.message).toBe('Task cancelled');
    });
    
    // Add more tests for other bridge methods
});