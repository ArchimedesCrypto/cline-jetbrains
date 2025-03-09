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
const entryPoint = path.resolve(srcDir, 'jetbrains.tsx');
const outputFile = path.resolve(distDir, 'bridge.js');
const outputCssFile = path.resolve(distDir, 'ui-styles.css');

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
    loader: {
      '.tsx': 'tsx',
      '.ts': 'ts',
      '.jsx': 'jsx',
      '.js': 'js',
      '.css': 'css',
      '.svg': 'dataurl',
      '.png': 'dataurl',
      '.jpg': 'dataurl',
      '.gif': 'dataurl'
    },
    define: {
      'process.env.NODE_ENV': '"production"',
      'process.env.PLATFORM': '"jetbrains"'
    },
    external: [
      'vscode'  // Exclude vscode from the bundle
    ],
    plugins: [
      {
        name: 'css-output',
        setup(build) {
          let css = '';
          
          // Collect all CSS content
          build.onLoad({ filter: /\.css$/ }, async (args) => {
            const content = await fs.promises.readFile(args.path, 'utf8');
            css += content + '\n';
            return {
              contents: `
                const style = document.createElement('style');
                style.textContent = ${JSON.stringify(content)};
                document.head.appendChild(style);
              `,
              loader: 'js',
            };
          });
          
          // Write collected CSS to file
          build.onEnd(async () => {
            await fs.promises.writeFile(outputCssFile, css);
            console.log(`Successfully built ui-styles.css to ${outputCssFile}`);
          });
        },
      },
    ],
  })
  .then(() => {
    console.log(`Successfully built bridge.js to ${outputFile}`);
  })
  .catch((error) => {
    console.error('Error building bridge.js:', error);
    process.exit(1);
  });