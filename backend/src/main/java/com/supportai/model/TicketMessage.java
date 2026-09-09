package com.supportai.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "ticket_messages")
public class TicketMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ticket_id", nullable = false)
    @JsonBackReference
    private Ticket ticket;

    private Long senderId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private SenderType senderType;

    @Column(nullable = false, length = 100)
    private String senderName;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private SentimentType sentiment = SentimentType.NEUTRAL;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public TicketMessage() {
        this.createdAt = LocalDateTime.now();
    }

    public TicketMessage(Ticket ticket, Long senderId, SenderType senderType, String senderName, String message, SentimentType sentiment) {
        this.ticket = ticket;
        this.senderId = senderId;
        this.senderType = senderType;
        this.senderName = senderName;
        this.message = message;
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

    public Ticket getTicket() {
        return ticket;
    }

    public void setTicket(Ticket ticket) {
        this.ticket = ticket;
    }

    public Long getSenderId() {
        return senderId;
    }

    public void setSenderId(Long senderId) {
        this.senderId = senderId;
    }

    public SenderType getSenderType() {
        return senderType;
    }

    public void setSenderType(SenderType senderType) {
        this.senderType = senderType;
    }

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
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
