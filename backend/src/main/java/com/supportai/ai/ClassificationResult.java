package com.supportai.ai;

import com.supportai.model.TicketCategory;
import java.util.ArrayList;
import java.util.List;

public class ClassificationResult {
    private TicketCategory category;
    private double confidence;
    private List<String> tags = new ArrayList<>();
    private String reasoning;

    public ClassificationResult() {}

    public ClassificationResult(TicketCategory category, double confidence, List<String> tags, String reasoning) {
        this.category = category;
        this.confidence = confidence;
        this.tags = tags != null ? tags : new ArrayList<>();
        this.reasoning = reasoning;
    }

    public TicketCategory getCategory() { return category; }
    public void setCategory(TicketCategory category) { this.category = category; }
    public double getConfidence() { return confidence; }
    public void setConfidence(double confidence) { this.confidence = confidence; }
    public List<String> getTags() { return tags; }
    public void setTags(List<String> tags) { this.tags = tags; }
    public String getReasoning() { return reasoning; }
    public void setReasoning(String reasoning) { this.reasoning = reasoning; }
}
