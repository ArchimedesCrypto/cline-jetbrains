package com.cline.services.api;

import com.intellij.openapi.diagnostic.Logger;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * Strategy for retrying API requests.
 */
public class RetryStrategy {
    private static final Logger LOG = Logger.getInstance(RetryStrategy.class);
    
    private final int maxRetries;
    private final long initialDelayMs;
    private final double backoffFactor;
    private final long maxDelayMs;
    
    /**
     * Creates a new retry strategy.
     *
     * @param maxRetries     The maximum number of retries
     * @param initialDelayMs The initial delay in milliseconds
     * @param backoffFactor  The backoff factor
     * @param maxDelayMs     The maximum delay in milliseconds
     */
    public RetryStrategy(int maxRetries, long initialDelayMs, double backoffFactor, long maxDelayMs) {
        this.maxRetries = maxRetries;
        this.initialDelayMs = initialDelayMs;
        this.backoffFactor = backoffFactor;
        this.maxDelayMs = maxDelayMs;
    }
    
    /**
     * Creates a default retry strategy.
     *
     * @return The default retry strategy
     */
    public static RetryStrategy createDefault() {
        return new RetryStrategy(3, 1000, 2.0, 10000);
    }
    
    /**
     * Executes a function with retry.
     *
     * @param supplier The function to execute
     * @param <T>      The return type
     * @return A CompletableFuture that completes with the result of the function
     */
    public <T> CompletableFuture<T> execute(Supplier<CompletableFuture<T>> supplier) {
        return executeWithRetry(supplier, 0, initialDelayMs);
    }
    
    /**
     * Executes a function with retry.
     *
     * @param supplier  The function to execute
     * @param attempt   The current attempt
     * @param delayMs   The delay in milliseconds
     * @param <T>       The return type
     * @return A CompletableFuture that completes with the result of the function
     */
    private <T> CompletableFuture<T> executeWithRetry(Supplier<CompletableFuture<T>> supplier, int attempt, long delayMs) {
        return supplier.get().exceptionally(e -> {
            if (attempt < maxRetries && isRetryable(e)) {
                LOG.info("Retrying API request (attempt " + (attempt + 1) + " of " + maxRetries + ")");
                
                // Calculate the next delay
                long nextDelayMs = Math.min((long) (delayMs * backoffFactor), maxDelayMs);
                
                // Create a new CompletableFuture for the retry
                CompletableFuture<T> retryFuture = new CompletableFuture<>();
                
                // Schedule the retry
                CompletableFuture.delayedExecutor(delayMs, TimeUnit.MILLISECONDS).execute(() -> {
                    executeWithRetry(supplier, attempt + 1, nextDelayMs)
                            .thenAccept(retryFuture::complete)
                            .exceptionally(retryError -> {
                                retryFuture.completeExceptionally(retryError);
                                return null;
                            });
                });
                
                return retryFuture.join();
            } else {
                // Rethrow the exception
                if (e instanceof RuntimeException) {
                    throw (RuntimeException) e;
                } else {
                    throw new RuntimeException(e);
                }
            }
        });
    }
    
    /**
     * Checks if an exception is retryable.
     *
     * @param e The exception
     * @return Whether the exception is retryable
     */
    private boolean isRetryable(Throwable e) {
        // Retry on network errors, timeouts, and 5xx errors
        String message = e.getMessage();
        if (message == null) {
            return false;
        }
        
        return message.contains("timeout") ||
                message.contains("connection") ||
                message.contains("network") ||
                message.contains("5") ||
                message.contains("server error");
    }
}