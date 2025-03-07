#!/bin/bash

# Script to sync changes from the main Cline repository.
# This script syncs the TypeScript code from the main repository to the JetBrains plugin.

set -e

# Get the directory of the script
SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
PROJECT_DIR="$( cd "$SCRIPT_DIR/.." && pwd )"
MAIN_REPO_DIR="$( cd "$PROJECT_DIR/../.." && pwd )"

# Print the current directory
echo "Syncing Cline JetBrains plugin in $PROJECT_DIR"

# Check if the main repository exists
if [ ! -d "$MAIN_REPO_DIR/src" ]; then
  echo "Error: Main repository not found at $MAIN_REPO_DIR"
  exit 1
fi

# Create the core directory if it doesn't exist
mkdir -p "$PROJECT_DIR/src/main/ts/core"

# Sync the core TypeScript code
echo "Syncing core TypeScript code..."
cp -r "$MAIN_REPO_DIR/src/core" "$PROJECT_DIR/src/main/ts/"
cp -r "$MAIN_REPO_DIR/src/shared" "$PROJECT_DIR/src/main/ts/"
cp -r "$MAIN_REPO_DIR/src/utils" "$PROJECT_DIR/src/main/ts/"
cp -r "$MAIN_REPO_DIR/src/api" "$PROJECT_DIR/src/main/ts/"

# Sync the TypeScript types
echo "Syncing TypeScript types..."
cp -r "$MAIN_REPO_DIR/src/exports" "$PROJECT_DIR/src/main/ts/"

# Update the imports in the TypeScript files
echo "Updating imports in TypeScript files..."
find "$PROJECT_DIR/src/main/ts" -name "*.ts" -type f -exec sed -i 's|from "../|from "../../|g' {} \;
find "$PROJECT_DIR/src/main/ts" -name "*.ts" -type f -exec sed -i 's|from "../../core/|from "../core/|g' {} \;
find "$PROJECT_DIR/src/main/ts" -name "*.ts" -type f -exec sed -i 's|from "../../shared/|from "../shared/|g' {} \;
find "$PROJECT_DIR/src/main/ts" -name "*.ts" -type f -exec sed -i 's|from "../../utils/|from "../utils/|g' {} \;
find "$PROJECT_DIR/src/main/ts" -name "*.ts" -type f -exec sed -i 's|from "../../api/|from "../api/|g' {} \;
find "$PROJECT_DIR/src/main/ts" -name "*.ts" -type f -exec sed -i 's|from "../../exports/|from "../exports/|g' {} \;

echo "Sync completed successfully!"