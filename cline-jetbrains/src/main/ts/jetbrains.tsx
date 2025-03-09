import React from 'react';
import ReactDOM from 'react-dom';
import App from '../../../../webview-ui/src/App';
import { ExtensionStateContextProvider } from '../../../../webview-ui/src/context/ExtensionStateContext';

// Define the global interface for JetBrains bridge
declare global {
    interface Window {
        initializeBridge: (options: any) => void;
        updateUI: (data: any) => void;
        jsQuery0: any; // Execute task query
        jsQuery1: any; // Cancel task query
        jsQuery2: any; // Get task status query
        jsQuery3: any; // UI event query
    }
}

// Initialize the UI
window.initializeBridge = (options) => {
    console.log('Initializing UI with options:', options);

    // Create a mock vscode API that maps to JetBrains functionality
    const jetbrainsVscode = {
        postMessage: (message: any) => {
            console.log('Posting message:', message);
            switch (message.type) {
                case 'newTask':
                    window.jsQuery0.result(JSON.stringify({
                        type: 'newTask',
                        text: message.text,
                        images: message.images
                    }));
                    break;
                case 'askResponse':
                    window.jsQuery0.result(JSON.stringify({
                        type: 'askResponse',
                        askResponse: message.askResponse,
                        text: message.text,
                        images: message.images
                    }));
                    break;
                case 'clearTask':
                    window.jsQuery0.result(JSON.stringify({
                        type: 'clearTask'
                    }));
                    break;
                case 'cancelTask':
                    window.jsQuery1.result(JSON.stringify({
                        type: 'cancelTask'
                    }));
                    break;
                case 'getTaskStatus':
                    window.jsQuery2.result(JSON.stringify({
                        type: 'getTaskStatus'
                    }));
                    break;
                default:
                    window.jsQuery3.result(JSON.stringify(message));
                    break;
            }
        },
        getState: () => options.state || {},
        setState: (state: any) => {
            window.jsQuery3.result(JSON.stringify({
                type: 'setState',
                state
            }));
        }
    };

    // Replace the vscode API with our JetBrains version
    (window as any).acquireVsCodeApi = () => jetbrainsVscode;

    // Render the React app
    ReactDOM.render(
        <ExtensionStateContextProvider>
            <App />
        </ExtensionStateContextProvider>,
        document.getElementById('root')
    );
};

// Update the UI state
window.updateUI = (data) => {
    console.log('Updating UI with data:', data);
    
    // Dispatch a custom event that the React app can listen for
    window.dispatchEvent(new CustomEvent('jetbrains-state-update', {
        detail: data
    }));
};

// Export functions that need to be called from Java
export function executeTask(taskInput: string, options: string): void {
    console.log('Executing task:', taskInput, options);
    window.jsQuery0.result(JSON.stringify({
        taskInput,
        options
    }));
}

export function cancelTask(): void {
    console.log('Cancelling task');
    window.jsQuery1.result('cancel');
}

export function getTaskStatus(): void {
    console.log('Getting task status');
    window.jsQuery2.result('status');
}

// Add event listeners for UI updates
window.addEventListener('message', (event) => {
    const message = event.data;
    console.log('Received message:', message);

    // Forward the message to Java
    window.jsQuery3.result(JSON.stringify(message));
});