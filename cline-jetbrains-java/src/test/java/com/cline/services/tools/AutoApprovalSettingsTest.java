package com.cline.services.tools;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the AutoApprovalSettings class.
 */
public class AutoApprovalSettingsTest {
    
    @Test
    public void testDefaultConstructor() {
        AutoApprovalSettings settings = new AutoApprovalSettings();
        
        assertFalse(settings.isEnabled());
        assertEquals(3, settings.getMaxConsecutiveRequests());
        assertTrue(settings.getToolSettings().isEmpty());
    }
    
    @Test
    public void testConstructorWithParameters() {
        Map<String, Boolean> toolSettings = new HashMap<>();
        toolSettings.put("read_file", true);
        toolSettings.put("write_to_file", false);
        
        AutoApprovalSettings settings = new AutoApprovalSettings(true, 5, toolSettings);
        
        assertTrue(settings.isEnabled());
        assertEquals(5, settings.getMaxConsecutiveRequests());
        assertEquals(2, settings.getToolSettings().size());
        assertTrue(settings.getToolSettings().get("read_file"));
        assertFalse(settings.getToolSettings().get("write_to_file"));
    }
    
    @Test
    public void testShouldAutoApprove() {
        Map<String, Boolean> toolSettings = new HashMap<>();
        toolSettings.put("read_file", true);
        toolSettings.put("write_to_file", false);
        
        // Enabled settings
        AutoApprovalSettings enabledSettings = new AutoApprovalSettings(true, 5, toolSettings);
        
        assertTrue(enabledSettings.shouldAutoApprove("read_file"));
        assertFalse(enabledSettings.shouldAutoApprove("write_to_file"));
        assertFalse(enabledSettings.shouldAutoApprove("unknown_tool"));
        
        // Disabled settings
        AutoApprovalSettings disabledSettings = new AutoApprovalSettings(false, 5, toolSettings);
        
        assertFalse(disabledSettings.shouldAutoApprove("read_file"));
        assertFalse(disabledSettings.shouldAutoApprove("write_to_file"));
        assertFalse(disabledSettings.shouldAutoApprove("unknown_tool"));
    }
    
    @Test
    public void testBuilder() {
        AutoApprovalSettings settings = AutoApprovalSettings.builder()
                .enabled(true)
                .maxConsecutiveRequests(10)
                .toolSetting("read_file", true)
                .toolSetting("write_to_file", false)
                .build();
        
        assertTrue(settings.isEnabled());
        assertEquals(10, settings.getMaxConsecutiveRequests());
        assertEquals(2, settings.getToolSettings().size());
        assertTrue(settings.getToolSettings().get("read_file"));
        assertFalse(settings.getToolSettings().get("write_to_file"));
    }
    
    @Test
    public void testBuilderWithToolSettings() {
        Map<String, Boolean> toolSettings = new HashMap<>();
        toolSettings.put("read_file", true);
        toolSettings.put("write_to_file", false);
        
        AutoApprovalSettings settings = AutoApprovalSettings.builder()
                .enabled(true)
                .maxConsecutiveRequests(10)
                .toolSettings(toolSettings)
                .build();
        
        assertTrue(settings.isEnabled());
        assertEquals(10, settings.getMaxConsecutiveRequests());
        assertEquals(2, settings.getToolSettings().size());
        assertTrue(settings.getToolSettings().get("read_file"));
        assertFalse(settings.getToolSettings().get("write_to_file"));
    }
}