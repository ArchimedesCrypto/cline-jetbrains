package com.cline.core.tool;

import com.cline.core.tool.impl.*;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Registry for tools.
 * This class is responsible for registering all available tools with the ToolExecutor.
 */
@Service
public final class ToolRegistry {
    private static final Logger LOG = Logger.getInstance(ToolRegistry.class);
    
    private final Project project;
    private final ToolExecutor toolExecutor;
    
    /**
     * Creates a new tool registry.
     *
     * @param project The project
     */
    public ToolRegistry(Project project) {
        this.project = project;
        this.toolExecutor = ToolExecutor.getInstance(project);
        
        registerTools();
    }
    
    /**
     * Gets the tool registry instance.
     *
     * @param project The project
     * @return The tool registry instance
     */
    public static ToolRegistry getInstance(@NotNull Project project) {
        return project.getService(ToolRegistry.class);
    }
    
    /**
     * Gets the number of registered tools.
     *
     * @return The number of registered tools
     */
    public int getToolCount() {
        return toolExecutor.getTools().size();
    }
    
    /**
     * Gets all registered tools.
     *
     * @return A list of all registered tools
     */
    public List<Tool> getAllTools() {
        return new ArrayList<>(toolExecutor.getTools().values());
    }
    
    /**
     * Gets a tool by name.
     *
     * @param name The name of the tool
     * @return The tool, or null if not found
     */
    @Nullable
    public Tool getTool(String name) {
        return toolExecutor.getTools().get(name);
    }
    
    /**
     * Registers all available tools.
     */
    private void registerTools() {
        LOG.info("Registering tools");
        
        // Register file tools
        registerTool(new ReadFileTool(project));
        registerTool(new WriteToFileTool(project));
        registerTool(new ApplyDiffTool(project));
        registerTool(new SearchFilesTool(project));
        registerTool(new ListFilesTool(project));
        registerTool(new ListCodeDefinitionsTool(project));
        
        // Register command tools
        registerTool(new ExecuteCommandTool(project));
        
        // Register browser tools
        registerTool(new BrowserActionTool(project));
        
        // Register interaction tools
        registerTool(new AskFollowupQuestionTool(project));
        registerTool(new AttemptCompletionTool(project));
        
        LOG.info("Registered " + toolExecutor.getTools().size() + " tools");
    }
    
    /**
     * Registers a tool.
     *
     * @param tool The tool to register
     */
    private void registerTool(@NotNull Tool tool) {
        LOG.info("Registering tool: " + tool.getName());
        toolExecutor.registerTool(tool);
    }
}