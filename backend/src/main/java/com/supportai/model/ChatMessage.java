package com.supportai.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "chat_messages")
public class ChatMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String sessionId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private SenderType senderType;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private SentimentType sentiment = SentimentType.NEUTRAL;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public ChatMessage() {
        this.createdAt = LocalDateTime.now();
    }

    public ChatMessage(String sessionId, SenderType senderType, String content, SentimentType sentiment) {
        this.sessionId = sessionId;
        this.senderType = senderType;
        this.content = content;
        this.sentiment = sentiment != null ? sentiment : SentimentType.NEUTRAL;
        this.createdAt = LocalDateTime.now();
    }

    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public SenderType getSenderType() {
        return senderType;
    }

    public void setSenderType(SenderType senderType) {
        this.senderType = senderType;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public SentimentType getSentiment() {
        return sentiment;
    }

    public void setSentiment(SentimentType sentiment) {
        this.sentiment = sentiment;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
