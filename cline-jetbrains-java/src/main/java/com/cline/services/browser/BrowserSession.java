package com.cline.services.browser;

import java.awt.image.BufferedImage;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Interface for browser sessions.
 */
public interface BrowserSession {
    /**
     * Launch the browser and navigate to the specified URL.
     *
     * @param url The URL to navigate to
     * @return A CompletableFuture that completes when the browser is launched
     */
    CompletableFuture<Void> launch(String url);
    
    /**
     * Navigate to the specified URL.
     *
     * @param url The URL to navigate to
     * @return A CompletableFuture that completes when the navigation is complete
     */
    CompletableFuture<Void> navigate(String url);
    
    /**
     * Click at the specified coordinates.
     *
     * @param x The x coordinate
     * @param y The y coordinate
     * @return A CompletableFuture that completes when the click is complete
     */
    CompletableFuture<Void> click(int x, int y);
    
    /**
     * Type the specified text.
     *
     * @param text The text to type
     * @return A CompletableFuture that completes when the typing is complete
     */
    CompletableFuture<Void> type(String text);
    
    /**
     * Scroll down the page.
     *
     * @return A CompletableFuture that completes when the scroll is complete
     */
    CompletableFuture<Void> scrollDown();
    
    /**
     * Scroll up the page.
     *
     * @return A CompletableFuture that completes when the scroll is complete
     */
    CompletableFuture<Void> scrollUp();
    
    /**
     * Capture a screenshot of the browser.
     *
     * @return A CompletableFuture containing the screenshot as a BufferedImage
     */
    CompletableFuture<BufferedImage> captureScreenshot();
    
    /**
     * Get the console logs from the browser.
     *
     * @return A CompletableFuture containing the console logs
     */
    CompletableFuture<List<String>> getConsoleLogs();
    
    /**
     * Close the browser.
     *
     * @return A CompletableFuture that completes when the browser is closed
     */
    CompletableFuture<Void> close();
    
    /**
     * Check if the browser is running.
     *
     * @return True if the browser is running, false otherwise
     */
    boolean isRunning();
}