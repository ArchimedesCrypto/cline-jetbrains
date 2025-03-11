package com.cline.core.tool;

import com.cline.core.tool.impl.ReadFileTool;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Registry for tools that can be executed by the assistant.
 */
@Service
public final class ToolRegistry {
    private static final Logger LOG = Logger.getInstance(ToolRegistry.class);
    
    private final Map<String, Tool> tools = new HashMap<>();
    private final Project project;

    /**
     * Creates a new tool registry.
     *
     * @param project The project
     */
    public ToolRegistry(Project project) {
        this.project = project;
        registerDefaultTools();
    }

    /**
     * Gets the tool registry instance for the given project.
     *
     * @param project The project
     * @return The tool registry instance
     */
    public static ToolRegistry getInstance(@NotNull Project project) {
        return project.getService(ToolRegistry.class);
    }

    /**
     * Registers the default tools.
     */
    private void registerDefaultTools() {
        // Register file tools
        registerTool(new ReadFileTool(project));
        
        // Register other tools as they are implemented
        // registerTool(new WriteToFileTool(project));
        // registerTool(new ApplyDiffTool(project));
        // registerTool(new ListFilesTool(project));
        // registerTool(new SearchFilesTool(project));
        // registerTool(new ExecuteCommandTool(project));
        // registerTool(new BrowserActionTool(project));
        // etc.
    }

    /**
     * Registers a tool.
     *
     * @param tool The tool to register
     */
    public void registerTool(@NotNull Tool tool) {
        String name = tool.getName();
        if (tools.containsKey(name)) {
            LOG.warn("Tool with name '" + name + "' is already registered. Overwriting.");
        }
        tools.put(name, tool);
        LOG.info("Registered tool: " + name);
    }

    /**
     * Gets a tool by name.
     *
     * @param name The tool name
     * @return The tool, or null if not found
     */
    @Nullable
    public Tool getTool(@NotNull String name) {
        return tools.get(name);
    }

    /**
     * Gets all registered tools.
     *
     * @return An unmodifiable collection of all registered tools
     */
    @NotNull
    public Collection<Tool> getAllTools() {
        return Collections.unmodifiableCollection(tools.values());
    }

    /**
     * Checks if a tool with the given name is registered.
     *
     * @param name The tool name
     * @return True if the tool is registered, false otherwise
     */
    public boolean hasTool(@NotNull String name) {
        return tools.containsKey(name);
    }

    /**
     * Gets the number of registered tools.
     *
     * @return The number of registered tools
     */
    public int getToolCount() {
        return tools.size();
    }
}