package com.cline.services.api;

import com.intellij.openapi.diagnostic.Logger;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Metrics for API requests.
 */
public class ApiMetrics {
    private static final Logger LOG = Logger.getInstance(ApiMetrics.class);
    
    private final AtomicInteger totalRequests = new AtomicInteger(0);
    private final AtomicInteger successfulRequests = new AtomicInteger(0);
    private final AtomicInteger failedRequests = new AtomicInteger(0);
    private final AtomicLong totalLatencyMs = new AtomicLong(0);
    private final AtomicInteger totalInputTokens = new AtomicInteger(0);
    private final AtomicInteger totalOutputTokens = new AtomicInteger(0);
    private final List<RequestMetrics> recentRequests = new ArrayList<>();
    
    /**
     * Records a request.
     *
     * @param startTime    The start time of the request
     * @param endTime      The end time of the request
     * @param success      Whether the request was successful
     * @param inputTokens  The number of input tokens
     * @param outputTokens The number of output tokens
     */
    public void recordRequest(Instant startTime, Instant endTime, boolean success, int inputTokens, int outputTokens) {
        // Calculate the latency
        long latencyMs = endTime.toEpochMilli() - startTime.toEpochMilli();
        
        // Update the metrics
        totalRequests.incrementAndGet();
        if (success) {
            successfulRequests.incrementAndGet();
        } else {
            failedRequests.incrementAndGet();
        }
        totalLatencyMs.addAndGet(latencyMs);
        totalInputTokens.addAndGet(inputTokens);
        totalOutputTokens.addAndGet(outputTokens);
        
        // Add to recent requests
        synchronized (recentRequests) {
            recentRequests.add(new RequestMetrics(startTime, endTime, success, inputTokens, outputTokens));
            
            // Keep only the last 100 requests
            if (recentRequests.size() > 100) {
                recentRequests.remove(0);
            }
        }
    }
    
    /**
     * Gets the total number of requests.
     *
     * @return The total number of requests
     */
    public int getTotalRequests() {
        return totalRequests.get();
    }
    
    /**
     * Gets the number of successful requests.
     *
     * @return The number of successful requests
     */
    public int getSuccessfulRequests() {
        return successfulRequests.get();
    }
    
    /**
     * Gets the number of failed requests.
     *
     * @return The number of failed requests
     */
    public int getFailedRequests() {
        return failedRequests.get();
    }
    
    /**
     * Gets the average latency in milliseconds.
     *
     * @return The average latency in milliseconds
     */
    public double getAverageLatencyMs() {
        int total = totalRequests.get();
        if (total == 0) {
            return 0;
        }
        return (double) totalLatencyMs.get() / total;
    }
    
    /**
     * Gets the total number of input tokens.
     *
     * @return The total number of input tokens
     */
    public int getTotalInputTokens() {
        return totalInputTokens.get();
    }
    
    /**
     * Gets the total number of output tokens.
     *
     * @return The total number of output tokens
     */
    public int getTotalOutputTokens() {
        return totalOutputTokens.get();
    }
    
    /**
     * Gets the recent requests.
     *
     * @return The recent requests
     */
    public List<RequestMetrics> getRecentRequests() {
        synchronized (recentRequests) {
            return new ArrayList<>(recentRequests);
        }
    }
    
    /**
     * Resets the metrics.
     */
    public void reset() {
        totalRequests.set(0);
        successfulRequests.set(0);
        failedRequests.set(0);
        totalLatencyMs.set(0);
        totalInputTokens.set(0);
        totalOutputTokens.set(0);
        synchronized (recentRequests) {
            recentRequests.clear();
        }
    }
    
    /**
     * Metrics for a single request.
     */
    public static class RequestMetrics {
        private final Instant startTime;
        private final Instant endTime;
        private final boolean success;
        private final int inputTokens;
        private final int outputTokens;
        
        /**
         * Creates new request metrics.
         *
         * @param startTime    The start time of the request
         * @param endTime      The end time of the request
         * @param success      Whether the request was successful
         * @param inputTokens  The number of input tokens
         * @param outputTokens The number of output tokens
         */
        public RequestMetrics(Instant startTime, Instant endTime, boolean success, int inputTokens, int outputTokens) {
            this.startTime = startTime;
            this.endTime = endTime;
            this.success = success;
            this.inputTokens = inputTokens;
            this.outputTokens = outputTokens;
        }
        
        /**
         * Gets the start time of the request.
         *
         * @return The start time of the request
         */
        public Instant getStartTime() {
            return startTime;
        }
        
        /**
         * Gets the end time of the request.
         *
         * @return The end time of the request
         */
        public Instant getEndTime() {
            return endTime;
        }
        
        /**
         * Gets whether the request was successful.
         *
         * @return Whether the request was successful
         */
        public boolean isSuccess() {
            return success;
        }
        
        /**
         * Gets the number of input tokens.
         *
         * @return The number of input tokens
         */
        public int getInputTokens() {
            return inputTokens;
        }
        
        /**
         * Gets the number of output tokens.
         *
         * @return The number of output tokens
         */
        public int getOutputTokens() {
            return outputTokens;
        }
        
        /**
         * Gets the latency in milliseconds.
         *
         * @return The latency in milliseconds
         */
        public long getLatencyMs() {
            return endTime.toEpochMilli() - startTime.toEpochMilli();
        }
    }
}