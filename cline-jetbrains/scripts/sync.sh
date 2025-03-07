#!/bin/bash

# Script to sync changes from the main Cline repository
# This script should be run from the root of the cline-jetbrains directory

# Exit on error
set -e

# Print commands
set -x

# Check if the main repository is already cloned
if [ ! -d "cline-core" ]; then
    echo "Cloning main Cline repository..."
    git clone https://github.com/cline/cline.git cline-core
fi

# Navigate to the main repository
cd cline-core

# Fetch the latest changes
git fetch origin

# Reset to the latest main branch
git reset --hard origin/main

# Navigate back to the JetBrains plugin directory
cd ..

# Copy the necessary files from the main repository
echo "Copying files from main repository..."

# Create directories if they don't exist
mkdir -p src/main/ts/core

# Copy the core TypeScript files
cp -r cline-core/src/core/* src/main/ts/core/
cp -r cline-core/src/api/* src/main/ts/core/api/
cp -r cline-core/src/shared/* src/main/ts/core/shared/
cp -r cline-core/src/utils/* src/main/ts/core/utils/

# Copy the necessary resources
mkdir -p src/main/resources/icons
cp -r cline-core/assets/icons/* src/main/resources/icons/

# Update the bridge and adapter files if needed
echo "Updating bridge and adapter files..."

# TODO: Add logic to update bridge and adapter files if needed

# Build the TypeScript code
echo "Building TypeScript code..."
cd src/main/ts
npx tsc
cd ../../..

# Run tests
echo "Running tests..."
# TODO: Add tests

echo "Sync completed successfully!"