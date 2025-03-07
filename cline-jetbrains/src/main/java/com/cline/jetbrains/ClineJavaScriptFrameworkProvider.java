package com.cline.jetbrains;

import com.intellij.lang.javascript.frameworks.FrameworkSpecificHandler;
import com.intellij.lang.javascript.library.JSLibraryManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Provider for JavaScript framework integration.
 * This class handles the integration with the TypeScript code.
 */
public class ClineJavaScriptFrameworkProvider implements FrameworkSpecificHandler {
    private static final Logger LOG = Logger.getInstance(ClineJavaScriptFrameworkProvider.class);

    /**
     * Get the name of the framework.
     * @return The framework name
     */
    @NotNull
    @Override
    public String getFrameworkName() {
        return "Cline";
    }

    /**
     * Check if the file is part of the framework.
     * @param project The current project
     * @param file The file to check
     * @return True if the file is part of the framework
     */
    @Override
    public boolean isFileInFramework(@NotNull Project project, @NotNull VirtualFile file) {
        // Check if the file is part of the Cline TypeScript bridge
        String path = file.getPath();
        return path.contains("/ts/bridge/") || path.contains("/ts/adapters/");
    }

    /**
     * Get the library manager for the framework.
     * @param project The current project
     * @return The library manager
     */
    @Nullable
    @Override
    public JSLibraryManager getLibraryManager(@NotNull Project project) {
        // We don't need a custom library manager for now
        return null;
    }

    /**
     * Initialize the framework.
     * @param project The current project
     */
    @Override
    public void initFramework(@NotNull Project project) {
        LOG.info("Initializing Cline JavaScript framework for project: " + project.getName());
        
        // TODO: Initialize TypeScript bridge
    }
}