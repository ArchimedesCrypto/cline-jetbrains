#!/bin/bash

# Script to run all tests for the Cline JetBrains plugin.
# This script runs both Java and TypeScript tests.

set -e

# Get the directory of the script
SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
PROJECT_DIR="$( cd "$SCRIPT_DIR/.." && pwd )"

# Print the current directory
echo "Running tests for Cline JetBrains plugin in $PROJECT_DIR"

# Install npm dependencies
echo "Installing npm dependencies..."
cd "$PROJECT_DIR"
npm install

# Run TypeScript tests
echo "Running TypeScript tests..."
npm test

# Run Java tests
echo "Running Java tests..."
cd "$PROJECT_DIR"
./gradlew test

echo "All tests completed successfully!"