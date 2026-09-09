package com.supportai.dto;

import com.supportai.model.SenderType;
import com.supportai.model.SentimentType;
import com.supportai.model.TicketMessage;
import java.time.LocalDateTime;

public class TicketMessageDto {
    private Long id;
    private Long senderId;
    private SenderType senderType;
    private String senderName;
    private String message;
    private SentimentType sentiment;
    private LocalDateTime createdAt;

    public TicketMessageDto() {}

    public static TicketMessageDto fromEntity(TicketMessage entity) {
        TicketMessageDto dto = new TicketMessageDto();
        dto.setId(entity.getId());
        dto.setSenderId(entity.getSenderId());
        dto.setSenderType(entity.getSenderType());
        dto.setSenderName(entity.getSenderName());
        dto.setMessage(entity.getMessage());
        dto.setSentiment(entity.getSentiment());
        dto.setCreatedAt(entity.getCreatedAt());
        return dto;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getSenderId() { return senderId; }
    public void setSenderId(Long senderId) { this.senderId = senderId; }
    public SenderType getSenderType() { return senderType; }
    public void setSenderType(SenderType senderType) { this.senderType = senderType; }
    public String getSenderName() { return senderName; }
    public void setSenderName(String senderName) { this.senderName = senderName; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public SentimentType getSentiment() { return sentiment; }
    public void setSentiment(SentimentType sentiment) { this.sentiment = sentiment; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
