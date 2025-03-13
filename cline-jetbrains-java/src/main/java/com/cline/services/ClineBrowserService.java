package com.cline.services;

import com.cline.services.browser.BrowserSession;
import com.cline.services.browser.JxBrowserSession;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Service for browser operations in the Cline plugin.
 */
@Service
public final class ClineBrowserService {
    private static final Logger LOG = Logger.getInstance(ClineBrowserService.class);
    private final Project project;
    private final ClineSettingsService settingsService;
    private final AtomicReference<BrowserSession> currentSession = new AtomicReference<>();
    
    /**
     * Creates a new browser service.
     *
     * @param project The project
     */
    public ClineBrowserService(Project project) {
        this.project = project;
        this.settingsService = ClineSettingsService.getInstance();
    }
    
    /**
     * Gets the browser service instance.
     *
     * @param project The project
     * @return The browser service instance
     */
    public static ClineBrowserService getInstance(@NotNull Project project) {
        return project.getService(ClineBrowserService.class);
    }
    
    /**
     * Launches the browser at the specified URL.
     *
     * @param url The URL to launch the browser at
     * @return A CompletableFuture that completes with a screenshot of the browser
     */
    public CompletableFuture<BrowserResult> launchBrowser(String url) {
        LOG.info("Launching browser at URL: " + url);
        
        // Close any existing session
        if (currentSession.get() != null) {
            closeBrowser().join();
        }
        
        // Create a new session
        BrowserSession session = new JxBrowserSession(settingsService.getBrowserSettings());
        currentSession.set(session);
        
        return session.launch(url)
                .thenCompose(v -> captureScreenshotAndLogs());
    }
    
    /**
     * Clicks at the specified coordinates.
     *
     * @param coordinate The coordinates to click at (format: "x,y")
     * @return A CompletableFuture that completes with a screenshot of the browser
     */
    public CompletableFuture<BrowserResult> clickAt(String coordinate) {
        LOG.info("Clicking at coordinates: " + coordinate);
        
        BrowserSession session = currentSession.get();
        if (session == null) {
            return CompletableFuture.failedFuture(new IllegalStateException("Browser is not running"));
        }
        
        String[] parts = coordinate.split(",");
        if (parts.length != 2) {
            return CompletableFuture.failedFuture(new IllegalArgumentException("Invalid coordinate format: " + coordinate));
        }
        
        try {
            int x = Integer.parseInt(parts[0]);
            int y = Integer.parseInt(parts[1]);
            
            return session.click(x, y)
                    .thenCompose(v -> captureScreenshotAndLogs());
        } catch (NumberFormatException e) {
            return CompletableFuture.failedFuture(new IllegalArgumentException("Invalid coordinate format: " + coordinate));
        }
    }
    
    /**
     * Types the specified text.
     *
     * @param text The text to type
     * @return A CompletableFuture that completes with a screenshot of the browser
     */
    public CompletableFuture<BrowserResult> typeText(String text) {
        LOG.info("Typing text: " + text);
        
        BrowserSession session = currentSession.get();
        if (session == null) {
            return CompletableFuture.failedFuture(new IllegalStateException("Browser is not running"));
        }
        
        return session.type(text)
                .thenCompose(v -> captureScreenshotAndLogs());
    }
    
    /**
     * Scrolls down the page.
     *
     * @return A CompletableFuture that completes with a screenshot of the browser
     */
    public CompletableFuture<BrowserResult> scrollDown() {
        LOG.info("Scrolling down");
        
        BrowserSession session = currentSession.get();
        if (session == null) {
            return CompletableFuture.failedFuture(new IllegalStateException("Browser is not running"));
        }
        
        return session.scrollDown()
                .thenCompose(v -> captureScreenshotAndLogs());
    }
    
    /**
     * Scrolls up the page.
     *
     * @return A CompletableFuture that completes with a screenshot of the browser
     */
    public CompletableFuture<BrowserResult> scrollUp() {
        LOG.info("Scrolling up");
        
        BrowserSession session = currentSession.get();
        if (session == null) {
            return CompletableFuture.failedFuture(new IllegalStateException("Browser is not running"));
        }
        
        return session.scrollUp()
                .thenCompose(v -> captureScreenshotAndLogs());
    }
    
    /**
     * Closes the browser.
     *
     * @return A CompletableFuture that completes with true if the browser was closed successfully
     */
    public CompletableFuture<Boolean> closeBrowser() {
        LOG.info("Closing browser");
        
        BrowserSession session = currentSession.getAndSet(null);
        if (session == null) {
            return CompletableFuture.completedFuture(true);
        }
        
        return session.close()
                .thenApply(v -> true)
                .exceptionally(e -> {
                    LOG.error("Error closing browser", e);
                    return false;
                });
    }
    
    /**
     * Captures a screenshot and console logs from the browser.
     *
     * @return A CompletableFuture that completes with the screenshot and logs
     */
    private CompletableFuture<BrowserResult> captureScreenshotAndLogs() {
        BrowserSession session = currentSession.get();
        if (session == null) {
            return CompletableFuture.failedFuture(new IllegalStateException("Browser is not running"));
        }
        
        return CompletableFuture.allOf(
                session.captureScreenshot().toCompletableFuture(),
                session.getConsoleLogs().toCompletableFuture()
        ).thenApply(v -> {
            try {
                BufferedImage screenshot = session.captureScreenshot().join();
                List<String> logs = session.getConsoleLogs().join();
                
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                ImageIO.write(screenshot, "png", outputStream);
                String base64Image = Base64.getEncoder().encodeToString(outputStream.toByteArray());
                
                return new BrowserResult(base64Image, logs);
            } catch (IOException e) {
                throw new RuntimeException("Error capturing screenshot", e);
            }
        });
    }
    
    /**
     * Result of a browser operation.
     */
    public static class BrowserResult {
        private final String screenshot;
        private final List<String> logs;
        
        /**
         * Create a new browser result.
         *
         * @param screenshot The screenshot as a base64-encoded string
         * @param logs The console logs
         */
        public BrowserResult(String screenshot, List<String> logs) {
            this.screenshot = screenshot;
            this.logs = logs;
        }
        
        /**
         * Get the screenshot as a base64-encoded string.
         *
         * @return The screenshot
         */
        public String getScreenshot() {
            return screenshot;
        }
        
        /**
         * Get the console logs.
         *
         * @return The console logs
         */
        public List<String> getLogs() {
            return logs;
        }
    }
}