#!/bin/bash

# Script to build the JetBrains plugin
# This script should be run from the root of the cline-jetbrains directory

# Exit on error
set -e

# Print commands
set -x

# Check if Gradle is installed
if ! command -v gradle &> /dev/null; then
    echo "Gradle is not installed. Please install Gradle and try again."
    exit 1
fi

# Build the TypeScript code
echo "Building TypeScript code..."
cd src/main/ts
npx tsc
cd ../../..

# Make the script executable
chmod +x scripts/sync.sh

# Build the plugin
echo "Building plugin..."
gradle clean build

# Check if the build was successful
if [ $? -eq 0 ]; then
    echo "Build completed successfully!"
    echo "Plugin JAR file is located at: build/libs/cline-jetbrains-1.0.0.jar"
else
    echo "Build failed!"
    exit 1
fi

# Run the plugin in the IDE (optional)
if [ "$1" == "--run" ]; then
    echo "Running plugin in the IDE..."
    gradle runIde
fi