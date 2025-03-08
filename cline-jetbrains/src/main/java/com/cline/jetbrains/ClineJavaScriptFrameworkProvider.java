package com.cline.jetbrains;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Provider for JavaScript framework integration.
 * This class handles the integration with the TypeScript code.
 *
 * Note: Previously implemented FrameworkSpecificHandler which is no longer available
 * in the current IntelliJ version. This is now a standalone class with similar functionality.
 */
public class ClineJavaScriptFrameworkProvider {
    private static final Logger LOG = Logger.getInstance(ClineJavaScriptFrameworkProvider.class);

    /**
     * Get the name of the framework.
     * @return The framework name
     */
    @NotNull
    public String getFrameworkName() {
        return "Cline";
    }

    /**
     * Check if the file is part of the framework.
     * @param project The current project
     * @param file The file to check
     * @return True if the file is part of the framework
     */
    public boolean isFileInFramework(@NotNull Project project, @NotNull VirtualFile file) {
        // Check if the file is part of the Cline TypeScript bridge
        String path = file.getPath();
        return path.contains("/ts/bridge/") || path.contains("/ts/adapters/");
    }

    /**
     * Initialize the framework.
     * @param project The current project
     */
    public void initFramework(@NotNull Project project) {
        LOG.info("Initializing Cline JavaScript framework for project: " + project.getName());
        
        // TODO: Initialize TypeScript bridge
    }
}