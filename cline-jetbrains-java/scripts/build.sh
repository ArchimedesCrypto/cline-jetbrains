#!/bin/bash

# Exit on error
set -e

# Change to the project directory
cd "$(dirname "$0")/.."

# Build the plugin
./gradlew clean buildPlugin

echo "Build completed successfully!"
echo "Plugin zip file is located at: $(pwd)/build/distributions/"