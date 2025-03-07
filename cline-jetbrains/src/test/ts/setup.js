// Jest setup file

// Mock the window object
global.window = {
  initializeBridge: jest.fn(),
  executeTask: jest.fn(),
  cancelTask: jest.fn(),
  getTaskStatus: jest.fn(),
};

// Mock the console
global.console = {
  ...console,
  log: jest.fn(),
  error: jest.fn(),
  warn: jest.fn(),
  info: jest.fn(),
};