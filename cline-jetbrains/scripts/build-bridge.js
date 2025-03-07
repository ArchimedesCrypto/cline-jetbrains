/**
 * Script to build the TypeScript bridge code.
 * This script compiles the TypeScript code to JavaScript and bundles it into a single file.
 */

const esbuild = require('esbuild');
const path = require('path');
const fs = require('fs');

// Define the paths
const srcDir = path.resolve(__dirname, '../src/main/ts');
const distDir = path.resolve(__dirname, '../dist');
const entryPoint = path.resolve(srcDir, 'bridge/bridge.ts');
const outputFile = path.resolve(distDir, 'bridge.js');

// Create the dist directory if it doesn't exist
if (!fs.existsSync(distDir)) {
  fs.mkdirSync(distDir, { recursive: true });
}

// Build the TypeScript code
esbuild
  .build({
    entryPoints: [entryPoint],
    bundle: true,
    minify: true,
    sourcemap: false,
    platform: 'browser',
    target: 'es2020',
    outfile: outputFile,
    format: 'iife',
    globalName: 'ClineBridge',
    define: {
      'process.env.NODE_ENV': '"production"',
    },
  })
  .then(() => {
    console.log(`Successfully built bridge.js to ${outputFile}`);
  })
  .catch((error) => {
    console.error('Error building bridge.js:', error);
    process.exit(1);
  });