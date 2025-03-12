package com.cline.services;

import com.intellij.openapi.project.Project;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the ClineBrowserService class.
 */
public class ClineBrowserServiceTest {
    
    private ClineBrowserService browserService;
    
    @Mock
    private Project project;
    
    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        browserService = new ClineBrowserService(project);
    }
    
    @Test
    public void testLaunchBrowser() throws ExecutionException, InterruptedException {
        CompletableFuture<String> result = browserService.launchBrowser("https://example.com");
        String screenshot = result.get();
        
        assertNotNull(screenshot);
        assertTrue(screenshot.contains("https://example.com"));
    }
    
    @Test
    public void testClickAt() throws ExecutionException, InterruptedException {
        CompletableFuture<String> result = browserService.clickAt("100,200");
        String screenshot = result.get();
        
        assertNotNull(screenshot);
        assertTrue(screenshot.contains("100,200"));
    }
    
    @Test
    public void testTypeText() throws ExecutionException, InterruptedException {
        CompletableFuture<String> result = browserService.typeText("Hello, world!");
        String screenshot = result.get();
        
        assertNotNull(screenshot);
        assertTrue(screenshot.contains("Hello, world!"));
    }
    
    @Test
    public void testScrollDown() throws ExecutionException, InterruptedException {
        CompletableFuture<String> result = browserService.scrollDown();
        String screenshot = result.get();
        
        assertNotNull(screenshot);
        assertTrue(screenshot.contains("Scrolled down"));
    }
    
    @Test
    public void testScrollUp() throws ExecutionException, InterruptedException {
        CompletableFuture<String> result = browserService.scrollUp();
        String screenshot = result.get();
        
        assertNotNull(screenshot);
        assertTrue(screenshot.contains("Scrolled up"));
    }
    
    @Test
    public void testCloseBrowser() throws ExecutionException, InterruptedException {
        CompletableFuture<Boolean> result = browserService.closeBrowser();
        Boolean success = result.get();
        
        assertTrue(success);
    }
    
    @Test
    public void testBrowserWorkflow() throws ExecutionException, InterruptedException {
        // Launch browser
        CompletableFuture<String> launchResult = browserService.launchBrowser("https://example.com");
        String launchScreenshot = launchResult.get();
        assertNotNull(launchScreenshot);
        
        // Click at coordinates
        CompletableFuture<String> clickResult = browserService.clickAt("100,200");
        String clickScreenshot = clickResult.get();
        assertNotNull(clickScreenshot);
        
        // Type text
        CompletableFuture<String> typeResult = browserService.typeText("Hello, world!");
        String typeScreenshot = typeResult.get();
        assertNotNull(typeScreenshot);
        
        // Scroll down
        CompletableFuture<String> scrollDownResult = browserService.scrollDown();
        String scrollDownScreenshot = scrollDownResult.get();
        assertNotNull(scrollDownScreenshot);
        
        // Scroll up
        CompletableFuture<String> scrollUpResult = browserService.scrollUp();
        String scrollUpScreenshot = scrollUpResult.get();
        assertNotNull(scrollUpScreenshot);
        
        // Close browser
        CompletableFuture<Boolean> closeResult = browserService.closeBrowser();
        Boolean closeSuccess = closeResult.get();
        assertTrue(closeSuccess);
    }
}