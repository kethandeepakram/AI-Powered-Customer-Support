package com.supportai.dto;

import com.supportai.model.FaqArticle;
import com.supportai.model.SenderType;
import com.supportai.model.SentimentType;
import java.time.LocalDateTime;
import java.util.List;

public class ChatMessageResponse {
    private String sessionId;
    private SenderType senderType;
    private String content;
    private SentimentType sentiment;
    private boolean escalated;
    private Long escalatedTicketId;
    private List<FaqArticle> suggestedFaqs;
    private LocalDateTime createdAt;

    public ChatMessageResponse() {
        this.createdAt = LocalDateTime.now();
    }

    public ChatMessageResponse(String sessionId, SenderType senderType, String content,
                               SentimentType sentiment, boolean escalated, List<FaqArticle> suggestedFaqs) {
        this.sessionId = sessionId;
        this.senderType = senderType;
        this.content = content;
        this.sentiment = sentiment;
        this.escalated = escalated;
        this.suggestedFaqs = suggestedFaqs;
        this.createdAt = LocalDateTime.now();
    }

    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }
    public SenderType getSenderType() { return senderType; }
    public void setSenderType(SenderType senderType) { this.senderType = senderType; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public SentimentType getSentiment() { return sentiment; }
    public void setSentiment(SentimentType sentiment) { this.sentiment = sentiment; }
    public boolean isEscalated() { return escalated; }
    public void setEscalated(boolean escalated) { this.escalated = escalated; }
    public Long getEscalatedTicketId() { return escalatedTicketId; }
    public void setEscalatedTicketId(Long escalatedTicketId) { this.escalatedTicketId = escalatedTicketId; }
    public List<FaqArticle> getSuggestedFaqs() { return suggestedFaqs; }
    public void setSuggestedFaqs(List<FaqArticle> suggestedFaqs) { this.suggestedFaqs = suggestedFaqs; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
