package com.cline.services;

import com.cline.services.api.ApiMetrics;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import java.time.Instant;
import java.util.List;

/**
 * Service for tracking metrics.
 */
@Service
public final class ClineMetricsService {
    private static final Logger LOG = Logger.getInstance(ClineMetricsService.class);
    
    private final Project project;
    private final ApiMetrics apiMetrics = new ApiMetrics();
    
    /**
     * Creates a new metrics service.
     *
     * @param project The project
     */
    public ClineMetricsService(Project project) {
        this.project = project;
    }
    
    /**
     * Gets the metrics service instance.
     *
     * @param project The project
     * @return The metrics service instance
     */
    public static ClineMetricsService getInstance(@NotNull Project project) {
        return project.getService(ClineMetricsService.class);
    }
    
    /**
     * Records an API request.
     *
     * @param startTime    The start time of the request
     * @param endTime      The end time of the request
     * @param success      Whether the request was successful
     * @param inputTokens  The number of input tokens
     * @param outputTokens The number of output tokens
     */
    public void recordApiRequest(Instant startTime, Instant endTime, boolean success, int inputTokens, int outputTokens) {
        apiMetrics.recordRequest(startTime, endTime, success, inputTokens, outputTokens);
    }
    
    /**
     * Gets the total number of API requests.
     *
     * @return The total number of API requests
     */
    public int getTotalApiRequests() {
        return apiMetrics.getTotalRequests();
    }
    
    /**
     * Gets the number of successful API requests.
     *
     * @return The number of successful API requests
     */
    public int getSuccessfulApiRequests() {
        return apiMetrics.getSuccessfulRequests();
    }
    
    /**
     * Gets the number of failed API requests.
     *
     * @return The number of failed API requests
     */
    public int getFailedApiRequests() {
        return apiMetrics.getFailedRequests();
    }
    
    /**
     * Gets the average API request latency in milliseconds.
     *
     * @return The average API request latency in milliseconds
     */
    public double getAverageApiLatencyMs() {
        return apiMetrics.getAverageLatencyMs();
    }
    
    /**
     * Gets the total number of input tokens.
     *
     * @return The total number of input tokens
     */
    public int getTotalInputTokens() {
        return apiMetrics.getTotalInputTokens();
    }
    
    /**
     * Gets the total number of output tokens.
     *
     * @return The total number of output tokens
     */
    public int getTotalOutputTokens() {
        return apiMetrics.getTotalOutputTokens();
    }
    
    /**
     * Gets the recent API requests.
     *
     * @return The recent API requests
     */
    public List<ApiMetrics.RequestMetrics> getRecentApiRequests() {
        return apiMetrics.getRecentRequests();
    }
    
    /**
     * Resets the API metrics.
     */
    public void resetApiMetrics() {
        apiMetrics.reset();
    }
    
    /**
     * Gets the estimated cost of API usage.
     *
     * @return The estimated cost in USD
     */
    public double getEstimatedCost() {
        // Cost per 1000 tokens (example rates)
        double inputCostPer1000 = 0.01;
        double outputCostPer1000 = 0.03;
        
        // Calculate the cost
        double inputCost = (apiMetrics.getTotalInputTokens() / 1000.0) * inputCostPer1000;
        double outputCost = (apiMetrics.getTotalOutputTokens() / 1000.0) * outputCostPer1000;
        
        return inputCost + outputCost;
    }
}