package com.cline.services;

import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import org.junit.jupiter.api.Test;

public class ClineSettingsServiceTest extends BasePlatformTestCase {

    private ClineSettingsService settingsService;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        settingsService = ClineSettingsService.getInstance();
        // Reset to defaults before each test
        settingsService.resetToDefaults();
    }

    @Test
    public void testDefaultSettings() {
        assertEquals("anthropic", settingsService.getApiProvider());
        assertEquals("", settingsService.getApiKey());
        assertEquals("claude-3-5-sonnet-20241022", settingsService.getModel());
        assertEquals(0.7, settingsService.getTemperature());
        assertFalse(settingsService.isAutoApproveEnabled());
        assertEquals(10, settingsService.getAutoApproveMaxRequests());
    }

    @Test
    public void testSetAndGetSettings() {
        settingsService.setApiProvider("openai");
        settingsService.setApiKey("test-key");
        settingsService.setModel("gpt-4");
        settingsService.setTemperature(0.5);
        settingsService.setAutoApproveEnabled(true);
        settingsService.setAutoApproveMaxRequests(5);

        assertEquals("openai", settingsService.getApiProvider());
        assertEquals("test-key", settingsService.getApiKey());
        assertEquals("gpt-4", settingsService.getModel());
        assertEquals(0.5, settingsService.getTemperature());
        assertTrue(settingsService.isAutoApproveEnabled());
        assertEquals(5, settingsService.getAutoApproveMaxRequests());
    }

    @Test
    public void testResetToDefaults() {
        settingsService.setApiProvider("openai");
        settingsService.setApiKey("test-key");
        settingsService.resetToDefaults();

        assertEquals("anthropic", settingsService.getApiProvider());
        assertEquals("", settingsService.getApiKey());
    }
}