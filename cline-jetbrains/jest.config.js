/** @type {import('ts-jest').JestConfigWithTsJest} */
module.exports = {
  preset: 'ts-jest',
  testEnvironment: 'node',
  testMatch: ['**/src/test/ts/**/*.ts'],
  moduleNameMapper: {
    '^@/(.*)$': '<rootDir>/src/main/ts/$1'
  },
  transform: {
    '^.+\\.tsx?$': [
      'ts-jest',
      {
        tsconfig: 'src/main/ts/tsconfig.json'
      }
    ]
  }
};