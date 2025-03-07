/**
 * Tests for the TypeScript bridge implementation.
 */

import { ClineBridgeImpl } from '../../../main/ts/bridge/ClineBridgeImpl';
import { ClineCore } from '../../../main/ts/core/ClineCore';

// Mock the ClineCore class
jest.mock('../../../main/ts/core/ClineCore', () => {
  return {
    ClineCore: jest.fn().mockImplementation(() => {
      return {
        initialize: jest.fn().mockResolvedValue(undefined),
        executeTask: jest.fn().mockResolvedValue({
          success: true,
          message: 'Task executed successfully',
          data: { taskId: 'test-task-id' }
        }),
        cancelTask: jest.fn().mockResolvedValue(undefined),
        getTaskStatus: jest.fn().mockResolvedValue({
          status: 'completed',
          message: 'Task completed'
        })
      };
    })
  };
});

// Mock the window object
(global as any).window = {
  initializeBridge: jest.fn(),
  executeTask: jest.fn(),
  cancelTask: jest.fn(),
  getTaskStatus: jest.fn()
};

describe('TypeScript Bridge', () => {
  // Set up the query names
  const executeTaskQueryName = 'executeTaskQuery';
  const cancelTaskQueryName = 'cancelTaskQuery';
  const getTaskStatusQueryName = 'getTaskStatusQuery';
  
  // Set up the window object
  (window as any)[executeTaskQueryName] = { result: jest.fn() };
  (window as any)[cancelTaskQueryName] = { result: jest.fn() };
  (window as any)[getTaskStatusQueryName] = { result: jest.fn() };
  
  beforeEach(() => {
    // Reset the mocks
    jest.clearAllMocks();
  });
  
  describe('initializeBridge', () => {
    it('should initialize the bridge', () => {
      // Initialize the bridge
      window.initializeBridge(
        executeTaskQueryName,
        cancelTaskQueryName,
        getTaskStatusQueryName
      );
      
      // Verify that the bridge was initialized
      expect(window.initializeBridge).toHaveBeenCalledWith(
        executeTaskQueryName,
        cancelTaskQueryName,
        getTaskStatusQueryName
      );
    });
  });
  
  describe('executeTask', () => {
    it('should execute a task', async () => {
      // Initialize the bridge
      window.initializeBridge(
        executeTaskQueryName,
        cancelTaskQueryName,
        getTaskStatusQueryName
      );
      
      // Execute a task
      await window.executeTask('Test task', '{"model":"gpt-4"}');
      
      // Verify that the task was executed
      expect(window.executeTask).toHaveBeenCalledWith('Test task', '{"model":"gpt-4"}');
      
      // Verify that the result was returned
      expect((window as any)[executeTaskQueryName].result).toHaveBeenCalled();
    });
    
    it('should handle errors', async () => {
      // Initialize the bridge
      window.initializeBridge(
        executeTaskQueryName,
        cancelTaskQueryName,
        getTaskStatusQueryName
      );
      
      // Mock the ClineCore.executeTask method to throw an error
      (ClineCore as jest.Mock).mockImplementationOnce(() => {
        return {
          initialize: jest.fn().mockResolvedValue(undefined),
          executeTask: jest.fn().mockRejectedValue(new Error('Test error')),
          cancelTask: jest.fn().mockResolvedValue(undefined),
          getTaskStatus: jest.fn().mockResolvedValue({
            status: 'completed',
            message: 'Task completed'
          })
        };
      });
      
      // Execute a task
      await window.executeTask('Test task', '{"model":"gpt-4"}');
      
      // Verify that the task was executed
      expect(window.executeTask).toHaveBeenCalledWith('Test task', '{"model":"gpt-4"}');
      
      // Verify that the error was returned
      expect((window as any)[executeTaskQueryName].result).toHaveBeenCalledWith(
        expect.stringContaining('Error')
      );
    });
  });
  
  describe('cancelTask', () => {
    it('should cancel a task', async () => {
      // Initialize the bridge
      window.initializeBridge(
        executeTaskQueryName,
        cancelTaskQueryName,
        getTaskStatusQueryName
      );
      
      // Execute a task
      await window.executeTask('Test task', '{"model":"gpt-4"}');
      
      // Cancel the task
      await window.cancelTask();
      
      // Verify that the task was cancelled
      expect(window.cancelTask).toHaveBeenCalled();
      
      // Verify that the result was returned
      expect((window as any)[cancelTaskQueryName].result).toHaveBeenCalled();
    });
    
    it('should handle errors', async () => {
      // Initialize the bridge
      window.initializeBridge(
        executeTaskQueryName,
        cancelTaskQueryName,
        getTaskStatusQueryName
      );
      
      // Mock the ClineCore.cancelTask method to throw an error
      (ClineCore as jest.Mock).mockImplementationOnce(() => {
        return {
          initialize: jest.fn().mockResolvedValue(undefined),
          executeTask: jest.fn().mockResolvedValue({
            success: true,
            message: 'Task executed successfully',
            data: { taskId: 'test-task-id' }
          }),
          cancelTask: jest.fn().mockRejectedValue(new Error('Test error')),
          getTaskStatus: jest.fn().mockResolvedValue({
            status: 'completed',
            message: 'Task completed'
          })
        };
      });
      
      // Execute a task
      await window.executeTask('Test task', '{"model":"gpt-4"}');
      
      // Cancel the task
      await window.cancelTask();
      
      // Verify that the task was cancelled
      expect(window.cancelTask).toHaveBeenCalled();
      
      // Verify that the error was returned
      expect((window as any)[cancelTaskQueryName].result).toHaveBeenCalledWith(
        expect.stringContaining('Error')
      );
    });
  });
  
  describe('getTaskStatus', () => {
    it('should get the task status', async () => {
      // Initialize the bridge
      window.initializeBridge(
        executeTaskQueryName,
        cancelTaskQueryName,
        getTaskStatusQueryName
      );
      
      // Execute a task
      await window.executeTask('Test task', '{"model":"gpt-4"}');
      
      // Get the task status
      await window.getTaskStatus();
      
      // Verify that the task status was requested
      expect(window.getTaskStatus).toHaveBeenCalled();
      
      // Verify that the result was returned
      expect((window as any)[getTaskStatusQueryName].result).toHaveBeenCalled();
    });
    
    it('should handle errors', async () => {
      // Initialize the bridge
      window.initializeBridge(
        executeTaskQueryName,
        cancelTaskQueryName,
        getTaskStatusQueryName
      );
      
      // Mock the ClineCore.getTaskStatus method to throw an error
      (ClineCore as jest.Mock).mockImplementationOnce(() => {
        return {
          initialize: jest.fn().mockResolvedValue(undefined),
          executeTask: jest.fn().mockResolvedValue({
            success: true,
            message: 'Task executed successfully',
            data: { taskId: 'test-task-id' }
          }),
          cancelTask: jest.fn().mockResolvedValue(undefined),
          getTaskStatus: jest.fn().mockRejectedValue(new Error('Test error'))
        };
      });
      
      // Execute a task
      await window.executeTask('Test task', '{"model":"gpt-4"}');
      
      // Get the task status
      await window.getTaskStatus();
      
      // Verify that the task status was requested
      expect(window.getTaskStatus).toHaveBeenCalled();
      
      // Verify that the error was returned
      expect((window as any)[getTaskStatusQueryName].result).toHaveBeenCalledWith(
        expect.stringContaining('Error')
      );
    });
  });
});