package com.cline.jetbrains;

import com.intellij.ide.AppLifecycleListener;
import com.intellij.openapi.diagnostic.Logger;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Listener for application lifecycle events.
 * This class handles events like application startup and shutdown.
 */
public class ClineApplicationListener implements AppLifecycleListener {
    private static final Logger LOG = Logger.getInstance(ClineApplicationListener.class);

    /**
     * Called when the application is starting.
     * @param isRestart Whether the application is restarting
     */
    @Override
    public void appStarting(@NotNull List<String> commandLineArgs) {
        LOG.info("Cline application starting");
        // Initialize global resources here
    }

    /**
     * Called when the application is about to close.
     */
    @Override
    public void appClosing() {
        LOG.info("Cline application closing");
        // Clean up global resources here
    }

    /**
     * Called when all projects are closed.
     */
    @Override
    public void projectFrameClosed() {
        LOG.info("All project frames closed");
        // Handle project frame closure
    }

    /**
     * Called when the application is about to exit.
     * @param exitCode The exit code
     */
    @Override
    public void appWillBeClosed(boolean isRestart) {
        LOG.info("Application will be closed, restart: " + isRestart);
        // Perform final cleanup
    }
}