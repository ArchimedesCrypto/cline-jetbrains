package com.cline.jetbrains.bridge;

import com.cline.jetbrains.services.ClineSettingsService;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.jcef.JBCefBrowser;
import com.intellij.ui.jcef.JBCefJSQuery;
import org.cef.browser.CefBrowser;
import org.cef.browser.CefFrame;
import org.cef.handler.CefLoadHandlerAdapter;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Bridge for JavaScript communication.
 * This class is responsible for communicating with the TypeScript code using JCEF.
 */
public class JavaScriptBridge {
    private static final Logger LOG = Logger.getInstance(JavaScriptBridge.class);
    
    private final Project project;
    private final ClineSettingsService settingsService;
    private final AtomicBoolean initialized = new AtomicBoolean(false);
    
    private JBCefBrowser browser;
    private JBCefJSQuery executeTaskQuery;
    private JBCefJSQuery cancelTaskQuery;
    private JBCefJSQuery getTaskStatusQuery;
    
    /**
     * Constructor.
     * @param project The current project
     */
    public JavaScriptBridge(@NotNull Project project) {
        this.project = project;
        this.settingsService = ClineSettingsService.getInstance();
        
        LOG.info("JavaScriptBridge created for project: " + project.getName());
    }
    
    /**
     * Initialize the bridge.
     * @return A future that completes when the bridge is initialized
     */
    public CompletableFuture<Void> initialize() {
        LOG.info("Initializing JavaScript bridge");
        
        CompletableFuture<Void> future = new CompletableFuture<>();
        
        try {
            // Create the browser
            browser = JBCefBrowser.createBuilder()
                .setOffScreenRendering(true)
                .build();
            
            // Create the JS queries
            executeTaskQuery = JBCefJSQuery.create(browser);
            cancelTaskQuery = JBCefJSQuery.create(browser);
            getTaskStatusQuery = JBCefJSQuery.create(browser);
            
            // Register the JS queries
            executeTaskQuery.addHandler(result -> {
                LOG.info("Execute task result: " + result);
                return null;
            });
            
            cancelTaskQuery.addHandler(result -> {
                LOG.info("Cancel task result: " + result);
                return null;
            });
            
            getTaskStatusQuery.addHandler(result -> {
                LOG.info("Get task status result: " + result);
                return null;
            });
            
            // Create the bridge HTML file
            String bridgeHtml = createBridgeHtml();
            
            // Load the bridge HTML
            browser.loadHTML(bridgeHtml);
            
            // Add a load handler to detect when the page is loaded
            browser.getJBCefClient().addLoadHandler(new CefLoadHandlerAdapter() {
                @Override
                public void onLoadEnd(CefBrowser cefBrowser, CefFrame frame, int httpStatusCode) {
                    LOG.info("Bridge HTML loaded");
                    
                    // Initialize the bridge
                    String initScript = "window.initializeBridge(" +
                            "'" + executeTaskQuery.getQueryName() + "', " +
                            "'" + cancelTaskQuery.getQueryName() + "', " +
                            "'" + getTaskStatusQuery.getQueryName() + "'" +
                            ");";
                    
                    browser.getCefBrowser().executeJavaScript(initScript, browser.getCefBrowser().getURL(), 0);
                    
                    // Mark as initialized
                    initialized.set(true);
                    
                    // Complete the future
                    future.complete(null);
                }
            }, browser.getCefBrowser());
        } catch (Exception e) {
            LOG.error("Failed to initialize JavaScript bridge", e);
            future.completeExceptionally(e);
        }
        
        return future;
    }
    
    /**
     * Execute a task.
     * @param taskInput The task input
     * @param options The options
     * @return A future that completes with the task result
     */
    public CompletableFuture<String> executeTask(String taskInput, String options) {
        LOG.info("Executing task: " + taskInput);
        
        CompletableFuture<String> future = new CompletableFuture<>();
        
        if (!initialized.get()) {
            LOG.warn("JavaScript bridge not initialized");
            future.completeExceptionally(new IllegalStateException("JavaScript bridge not initialized"));
            return future;
        }
        
        // Escape the task input and options
        String escapedTaskInput = escapeJavaScriptString(taskInput);
        String escapedOptions = escapeJavaScriptString(options);
        
        // Create the execute task script
        String executeTaskScript = "window.executeTask('" + escapedTaskInput + "', '" + escapedOptions + "')";
        
        // Execute the script
        browser.getCefBrowser().executeJavaScript(executeTaskScript, browser.getCefBrowser().getURL(), 0);
        
        // Register a handler for the result
        executeTaskQuery.addHandler(result -> {
            LOG.info("Execute task result: " + result);
            future.complete(result);
            return null;
        });
        
        return future;
    }
    
    /**
     * Cancel the current task.
     * @return A future that completes when the task is cancelled
     */
    public CompletableFuture<Void> cancelTask() {
        LOG.info("Cancelling task");
        
        CompletableFuture<Void> future = new CompletableFuture<>();
        
        if (!initialized.get()) {
            LOG.warn("JavaScript bridge not initialized");
            future.completeExceptionally(new IllegalStateException("JavaScript bridge not initialized"));
            return future;
        }
        
        // Create the cancel task script
        String cancelTaskScript = "window.cancelTask()";
        
        // Execute the script
        browser.getCefBrowser().executeJavaScript(cancelTaskScript, browser.getCefBrowser().getURL(), 0);
        
        // Register a handler for the result
        cancelTaskQuery.addHandler(result -> {
            LOG.info("Cancel task result: " + result);
            future.complete(null);
            return null;
        });
        
        return future;
    }
    
    /**
     * Get the status of the current task.
     * @return A future that completes with the task status
     */
    public CompletableFuture<String> getTaskStatus() {
        LOG.info("Getting task status");
        
        CompletableFuture<String> future = new CompletableFuture<>();
        
        if (!initialized.get()) {
            LOG.warn("JavaScript bridge not initialized");
            future.completeExceptionally(new IllegalStateException("JavaScript bridge not initialized"));
            return future;
        }
        
        // Create the get task status script
        String getTaskStatusScript = "window.getTaskStatus()";
        
        // Execute the script
        browser.getCefBrowser().executeJavaScript(getTaskStatusScript, browser.getCefBrowser().getURL(), 0);
        
        // Register a handler for the result
        getTaskStatusQuery.addHandler(result -> {
            LOG.info("Get task status result: " + result);
            future.complete(result);
            return null;
        });
        
        return future;
    }
    
    /**
     * Dispose the bridge.
     */
    public void dispose() {
        LOG.info("Disposing JavaScript bridge");
        
        if (browser != null) {
            browser.dispose();
            browser = null;
        }
        
        if (executeTaskQuery != null) {
            executeTaskQuery.dispose();
            executeTaskQuery = null;
        }
        
        if (cancelTaskQuery != null) {
            cancelTaskQuery.dispose();
            cancelTaskQuery = null;
        }
        
        if (getTaskStatusQuery != null) {
            getTaskStatusQuery.dispose();
            getTaskStatusQuery = null;
        }
        
        initialized.set(false);
    }
    
    /**
     * Create the bridge HTML.
     * @return The bridge HTML
     */
    private String createBridgeHtml() {
        LOG.info("Creating bridge HTML");
        
        // Get the project directory
        VirtualFile projectDir = project.getBaseDir();
        if (projectDir == null) {
            LOG.warn("Project directory not found");
            return createDefaultBridgeHtml();
        }
        
        // Get the bridge.js file
        Path bridgeJsPath = Paths.get(projectDir.getPath(), "cline-jetbrains", "dist", "bridge.js");
        if (!Files.exists(bridgeJsPath)) {
            LOG.warn("Bridge.js not found at: " + bridgeJsPath);
            return createDefaultBridgeHtml();
        }
        
        try {
            // Read the bridge.js file
            String bridgeJs = new String(Files.readAllBytes(bridgeJsPath));
            
            // Create the HTML
            return "<!DOCTYPE html>\n" +
                    "<html>\n" +
                    "<head>\n" +
                    "    <title>Cline Bridge</title>\n" +
                    "</head>\n" +
                    "<body>\n" +
                    "    <script>\n" +
                    bridgeJs +
                    "    </script>\n" +
                    "</body>\n" +
                    "</html>";
        } catch (IOException e) {
            LOG.error("Failed to read bridge.js", e);
            return createDefaultBridgeHtml();
        }
    }
    
    /**
     * Create the default bridge HTML.
     * @return The default bridge HTML
     */
    private String createDefaultBridgeHtml() {
        LOG.info("Creating default bridge HTML");
        
        return "<!DOCTYPE html>\n" +
                "<html>\n" +
                "<head>\n" +
                "    <title>Cline Bridge</title>\n" +
                "</head>\n" +
                "<body>\n" +
                "    <script>\n" +
                "        // Bridge initialization\n" +
                "        let executeTaskQueryName = '';\n" +
                "        let cancelTaskQueryName = '';\n" +
                "        let getTaskStatusQueryName = '';\n" +
                "\n" +
                "        // Initialize the bridge\n" +
                "        window.initializeBridge = function(executeTaskQuery, cancelTaskQuery, getTaskStatusQuery) {\n" +
                "            executeTaskQueryName = executeTaskQuery;\n" +
                "            cancelTaskQueryName = cancelTaskQuery;\n" +
                "            getTaskStatusQueryName = getTaskStatusQuery;\n" +
                "            console.log('Bridge initialized');\n" +
                "        };\n" +
                "\n" +
                "        // Execute a task\n" +
                "        window.executeTask = function(taskInput, options) {\n" +
                "            console.log('Executing task: ' + taskInput);\n" +
                "            window[executeTaskQueryName].result('Task executed: ' + taskInput);\n" +
                "        };\n" +
                "\n" +
                "        // Cancel the current task\n" +
                "        window.cancelTask = function() {\n" +
                "            console.log('Cancelling task');\n" +
                "            window[cancelTaskQueryName].result('Task cancelled');\n" +
                "        };\n" +
                "\n" +
                "        // Get the status of the current task\n" +
                "        window.getTaskStatus = function() {\n" +
                "            console.log('Getting task status');\n" +
                "            window[getTaskStatusQueryName].result('Task status: completed');\n" +
                "        };\n" +
                "    </script>\n" +
                "</body>\n" +
                "</html>";
    }
    
    /**
     * Escape a JavaScript string.
     * @param str The string to escape
     * @return The escaped string
     */
    private String escapeJavaScriptString(String str) {
        if (str == null) {
            return "";
        }
        
        return str.replace("\\", "\\\\")
                .replace("'", "\\'")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}