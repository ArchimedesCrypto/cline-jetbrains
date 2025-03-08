#!/bin/bash

# Script to build the Cline JetBrains plugin.
# This script builds the TypeScript bridge code and the Gradle project.

set -e

# Get the directory of the script
SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
PROJECT_DIR="$( cd "$SCRIPT_DIR/.." && pwd )"

# Print the current directory
echo "Building Cline JetBrains plugin in $PROJECT_DIR"

# Install npm dependencies
echo "Installing npm dependencies..."
cd "$PROJECT_DIR"
npm install

# Build the TypeScript bridge code
echo "Building TypeScript bridge code..."
npm run build

# Build the Gradle project
echo "Building Gradle project..."
cd "$PROJECT_DIR"
# Use system Gradle instead of wrapper since we're having issues with the wrapper
gradle build

echo "Build completed successfully!"