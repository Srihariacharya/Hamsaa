package com.contactpro.contactpro.dto;

import java.util.List;
import java.util.Map;

public class AnalyticsResponse {
    private long totalInteractions;
    private double avgDuration;
    private double taskCompletionRate;
    private long activeContacts;
    private List<Map<String, Object>> interactionTrends;
    private List<Map<String, Object>> taskDistribution;

    // Getters and Setters
    public long getTotalInteractions() { return totalInteractions; }
    public void setTotalInteractions(long totalInteractions) { this.totalInteractions = totalInteractions; }

    public double getAvgDuration() { return avgDuration; }
    public void setAvgDuration(double avgDuration) { this.avgDuration = avgDuration; }

    public double getTaskCompletionRate() { return taskCompletionRate; }
    public void setTaskCompletionRate(double taskCompletionRate) { this.taskCompletionRate = taskCompletionRate; }

    public long getActiveContacts() { return activeContacts; }
    public void setActiveContacts(long activeContacts) { this.activeContacts = activeContacts; }

    public List<Map<String, Object>> getInteractionTrends() { return interactionTrends; }
    public void setInteractionTrends(List<Map<String, Object>> interactionTrends) { this.interactionTrends = interactionTrends; }

    public List<Map<String, Object>> getTaskDistribution() { return taskDistribution; }
    public void setTaskDistribution(List<Map<String, Object>> taskDistribution) { this.taskDistribution = taskDistribution; }
}
