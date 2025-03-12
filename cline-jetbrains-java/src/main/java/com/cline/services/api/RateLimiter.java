package com.cline.services.api;

import com.intellij.openapi.diagnostic.Logger;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * Rate limiter for API requests.
 */
public class RateLimiter {
    private static final Logger LOG = Logger.getInstance(RateLimiter.class);
    
    private final Semaphore semaphore;
    private final int maxRequestsPerMinute;
    private final long refillIntervalMs;
    
    /**
     * Creates a new rate limiter.
     *
     * @param maxRequestsPerMinute The maximum number of requests per minute
     */
    public RateLimiter(int maxRequestsPerMinute) {
        this.maxRequestsPerMinute = maxRequestsPerMinute;
        this.semaphore = new Semaphore(maxRequestsPerMinute);
        this.refillIntervalMs = 60000 / maxRequestsPerMinute;
        
        // Start the token refill thread
        startRefillThread();
    }
    
    /**
     * Creates a default rate limiter.
     *
     * @return The default rate limiter
     */
    public static RateLimiter createDefault() {
        return new RateLimiter(60);
    }
    
    /**
     * Executes a function with rate limiting.
     *
     * @param supplier The function to execute
     * @param <T>      The return type
     * @return A CompletableFuture that completes with the result of the function
     */
    public <T> CompletableFuture<T> execute(Supplier<CompletableFuture<T>> supplier) {
        CompletableFuture<T> future = new CompletableFuture<>();
        
        // Try to acquire a permit
        try {
            if (semaphore.tryAcquire(30, TimeUnit.SECONDS)) {
                // Execute the function
                supplier.get()
                        .thenAccept(future::complete)
                        .exceptionally(e -> {
                            future.completeExceptionally(e);
                            return null;
                        });
            } else {
                // Rate limit exceeded
                future.completeExceptionally(new RuntimeException("Rate limit exceeded"));
            }
        } catch (InterruptedException e) {
            future.completeExceptionally(e);
        }
        
        return future;
    }
    
    /**
     * Starts the token refill thread.
     */
    private void startRefillThread() {
        Thread refillThread = new Thread(() -> {
            try {
                while (true) {
                    // Sleep for the refill interval
                    Thread.sleep(refillIntervalMs);
                    
                    // Release a permit
                    if (semaphore.availablePermits() < maxRequestsPerMinute) {
                        semaphore.release();
                    }
                }
            } catch (InterruptedException e) {
                LOG.error("Rate limiter refill thread interrupted", e);
            }
        });
        
        refillThread.setDaemon(true);
        refillThread.start();
    }
}