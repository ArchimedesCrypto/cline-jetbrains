module.exports = {
  preset: 'ts-jest',
  testEnvironment: 'jsdom',
  roots: ['<rootDir>/src/test/ts'],
  transform: {
    '^.+\\.tsx?$': 'ts-jest',
  },
  testRegex: '(/__tests__/.*|(\\.|/)(test|spec))\\.tsx?$',
  moduleFileExtensions: ['ts', 'tsx', 'js', 'jsx', 'json', 'node'],
  moduleNameMapper: {
    '^@/(.*)$': '<rootDir>/src/main/ts/$1',
  },
  globals: {
    'ts-jest': {
      tsconfig: '<rootDir>/src/test/ts/tsconfig.json',
    },
  },
  setupFilesAfterEnv: ['<rootDir>/src/test/ts/setup.js'],
};