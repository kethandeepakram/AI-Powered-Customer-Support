package com.supportai.ai;

import com.supportai.model.SentimentType;
import java.util.ArrayList;
import java.util.List;

public class SentimentResult {
    private SentimentType label;
    private double score;
    private List<String> detectedKeywords = new ArrayList<>();
    private boolean escalationTriggered;

    public SentimentResult() {}

    public SentimentResult(SentimentType label, double score, List<String> detectedKeywords, boolean escalationTriggered) {
        this.label = label;
        this.score = score;
        this.detectedKeywords = detectedKeywords != null ? detectedKeywords : new ArrayList<>();
        this.escalationTriggered = escalationTriggered;
    }

    public SentimentType getLabel() { return label; }
    public void setLabel(SentimentType label) { this.label = label; }
    public double getScore() { return score; }
    public void setScore(double score) { this.score = score; }
    public List<String> getDetectedKeywords() { return detectedKeywords; }
    public void setDetectedKeywords(List<String> detectedKeywords) { this.detectedKeywords = detectedKeywords; }
    public boolean isEscalationTriggered() { return escalationTriggered; }
    public void setEscalationTriggered(boolean escalationTriggered) { this.escalationTriggered = escalationTriggered; }
}
