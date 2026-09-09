package com.supportai.dto;

import java.util.Map;

public class AnalyticsSummaryDto {
    private long totalTickets;
    private long openTickets;
    private long inProgressTickets;
    private long escalatedTickets;
    private long resolvedTickets;
    private double resolutionRate;
    private double averageSentimentScore;
    private Map<String, Long> categoryDistribution;
    private Map<String, Long> priorityDistribution;

    public AnalyticsSummaryDto() {}

    public long getTotalTickets() { return totalTickets; }
    public void setTotalTickets(long totalTickets) { this.totalTickets = totalTickets; }
    public long getOpenTickets() { return openTickets; }
    public void setOpenTickets(long openTickets) { this.openTickets = openTickets; }
    public long getInProgressTickets() { return inProgressTickets; }
    public void setInProgressTickets(long inProgressTickets) { this.inProgressTickets = inProgressTickets; }
    public long getEscalatedTickets() { return escalatedTickets; }
    public void setEscalatedTickets(long escalatedTickets) { this.escalatedTickets = escalatedTickets; }
    public long getResolvedTickets() { return resolvedTickets; }
    public void setResolvedTickets(long resolvedTickets) { this.resolvedTickets = resolvedTickets; }
    public double getResolutionRate() { return resolutionRate; }
    public void setResolutionRate(double resolutionRate) { this.resolutionRate = resolutionRate; }
    public double getAverageSentimentScore() { return averageSentimentScore; }
    public void setAverageSentimentScore(double averageSentimentScore) { this.averageSentimentScore = averageSentimentScore; }
    public Map<String, Long> getCategoryDistribution() { return categoryDistribution; }
    public void setCategoryDistribution(Map<String, Long> categoryDistribution) { this.categoryDistribution = categoryDistribution; }
    public Map<String, Long> getPriorityDistribution() { return priorityDistribution; }
    public void setPriorityDistribution(Map<String, Long> priorityDistribution) { this.priorityDistribution = priorityDistribution; }
}
