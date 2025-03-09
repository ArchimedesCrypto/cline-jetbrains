package com.cline.jetbrains.ui;

import com.cline.jetbrains.bridge.ClineBridgeManager;
import com.cline.jetbrains.services.ClineProjectService;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.ui.jcef.JBCefBrowser;
import com.intellij.ui.jcef.JBCefJSQuery;
import com.intellij.ui.components.JBPanel;
import org.cef.browser.CefBrowser;
import org.cef.browser.CefFrame;
import org.cef.handler.CefLoadHandlerAdapter;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Main panel for the Cline tool window.
 * This class is responsible for creating the UI components of the plugin.
 */
public class ClineToolWindowPanel extends JBPanel<ClineToolWindowPanel> {
    private static final Logger LOG = Logger.getInstance(ClineToolWindowPanel.class);
    
    private final Project project;
    private final ToolWindow toolWindow;
    private final ClineProjectService projectService;
    private final ClineBridgeManager bridgeManager;
    
    private JBCefBrowser browser;
    private final List<JBCefJSQuery> jsQueries = new ArrayList<>();
    
    /**
     * Constructor.
     * @param project The current project
     * @param toolWindow The tool window
     */
    public ClineToolWindowPanel(Project project, ToolWindow toolWindow) {
        super(new BorderLayout());
        this.project = project;
        this.toolWindow = toolWindow;
        this.projectService = project.getService(ClineProjectService.class);
        this.bridgeManager = ClineBridgeManager.getInstance(project);
        
        // Initialize UI components
        initializeUI();
    }
    
    /**
     * Initialize the UI components.
     */
    private void initializeUI() {
        LOG.info("Initializing Cline tool window UI");
        
        // Create the JCEF browser
        browser = JBCefBrowser.createBuilder()
            .setOffScreenRendering(false)
            .build();
        
        // Create JS queries for UI operations
        createJSQueries();
        
        // Load the TypeScript UI
        loadTypeScriptUI();
        
        // Add the browser to the panel
        add(browser.getComponent(), BorderLayout.CENTER);
    }
    
    /**
     * Create JS queries for UI operations.
     */
    private void createJSQueries() {
        // Create queries for task operations
        JBCefJSQuery executeTaskQuery = JBCefJSQuery.create(browser);
        executeTaskQuery.addHandler(result -> {
            LOG.info("Execute task request: " + result);
            bridgeManager.executeTask(result, "{}");
            return null;
        });
        jsQueries.add(executeTaskQuery);
        
        JBCefJSQuery cancelTaskQuery = JBCefJSQuery.create(browser);
        cancelTaskQuery.addHandler(result -> {
            LOG.info("Cancel task request: " + result);
            bridgeManager.cancelTask();
            return null;
        });
        jsQueries.add(cancelTaskQuery);
        
        JBCefJSQuery getTaskStatusQuery = JBCefJSQuery.create(browser);
        getTaskStatusQuery.addHandler(result -> {
            LOG.info("Get task status request: " + result);
            bridgeManager.getTaskStatus();
            return null;
        });
        jsQueries.add(getTaskStatusQuery);
        
        // Create query for UI events
        JBCefJSQuery uiEventQuery = JBCefJSQuery.create(browser);
        uiEventQuery.addHandler(result -> {
            LOG.info("UI event: " + result);
            // Handle UI events
            return null;
        });
        jsQueries.add(uiEventQuery);
    }
    
    /**
     * Load the TypeScript UI.
     */
    private void loadTypeScriptUI() {
        // Get the project directory
        Path projectDir = Paths.get(project.getBasePath());
        if (projectDir == null) {
            LOG.warn("Project directory not found");
            loadDefaultUI();
            return;
        }
        
        // Get the UI bundle files
        Path uiBundlePath = projectDir.resolve("cline-jetbrains/dist/ui-bundle.js");
        Path uiStylesPath = projectDir.resolve("cline-jetbrains/dist/ui-styles.css");
        
        if (!Files.exists(uiBundlePath) || !Files.exists(uiStylesPath)) {
            LOG.warn("UI bundle files not found");
            loadDefaultUI();
            return;
        }
        
        try {
            // Read the UI bundle files
            String uiBundle = new String(Files.readAllBytes(uiBundlePath));
            String uiStyles = new String(Files.readAllBytes(uiStylesPath));
            
            // Create the HTML
            String html = "<!DOCTYPE html>\n" +
                    "<html>\n" +
                    "<head>\n" +
                    "    <title>Cline</title>\n" +
                    "    <style>\n" +
                    "        body { margin: 0; padding: 0; }\n" +
                    "        #root { height: 100vh; }\n" +
                    uiStyles +
                    "    </style>\n" +
                    "</head>\n" +
                    "<body>\n" +
                    "    <div id=\"root\"></div>\n" +
                    "    <script>\n" +
                    uiBundle +
                    "    </script>\n" +
                    "</body>\n" +
                    "</html>";
            
            // Load the HTML
            browser.loadHTML(html);
            
            // Add a load handler to detect when the page is loaded
            browser.getJBCefClient().addLoadHandler(new CefLoadHandlerAdapter() {
                @Override
                public void onLoadEnd(CefBrowser cefBrowser, CefFrame frame, int httpStatusCode) {
                    LOG.info("UI HTML loaded");
                    
                    // Initialize the UI bridge
                    injectJSQueries();
                    
                    // Initialize the UI
                    initializeUIBridge();
                }
            }, browser.getCefBrowser());
        } catch (Exception e) {
            LOG.error("Failed to load UI bundle files", e);
            loadDefaultUI();
        }
    }
    
    /**
     * Load the default UI when the TypeScript UI cannot be loaded.
     */
    private void loadDefaultUI() {
        String html = "<!DOCTYPE html>\n" +
                "<html>\n" +
                "<head>\n" +
                "    <title>Cline</title>\n" +
                "    <style>\n" +
                "        body { margin: 20px; font-family: system-ui; }\n" +
                "    </style>\n" +
                "</head>\n" +
                "<body>\n" +
                "    <h1>Cline - AI Coding Assistant</h1>\n" +
                "    <p>Failed to load the TypeScript UI. Please check the logs for more information.</p>\n" +
                "</body>\n" +
                "</html>";
        
        browser.loadHTML(html);
    }
    
    /**
     * Inject the JS queries into the browser.
     */
    private void injectJSQueries() {
        for (int i = 0; i < jsQueries.size(); i++) {
            JBCefJSQuery query = jsQueries.get(i);
            String queryName = "jsQuery" + i;
            query.inject(queryName);
        }
    }
    
    /**
     * Initialize the UI bridge.
     */
    private void initializeUIBridge() {
        String initScript = "window.initializeBridge({" +
                "projectName: '" + project.getName() + "'," +
                "projectPath: '" + project.getBasePath() + "'," +
                "state: {}" +
                "});";
        
        browser.getCefBrowser().executeJavaScript(initScript, browser.getCefBrowser().getURL(), 0);
    }
    
    /**
     * Dispose the panel.
     */
    public void dispose() {
        LOG.info("Disposing Cline tool window panel");
        
        // Dispose JS queries
        for (JBCefJSQuery query : jsQueries) {
            query.dispose();
        }
        jsQueries.clear();
        
        // Dispose browser
        if (browser != null) {
            browser.dispose();
            browser = null;
        }
    }
}