package com.supportai.dto;

import com.supportai.model.*;
import java.time.LocalDateTime;
import java.util.List;

public class TicketResponse {
    private Long id;
    private String ticketNumber;
    private String title;
    private String description;
    private TicketCategory category;
    private TicketPriority priority;
    private TicketStatus status;
    private Double sentimentScore;
    private SentimentType sentimentLabel;
    private Long customerId;
    private String customerName;
    private String customerEmail;
    private Long assignedAgentId;
    private String assignedAgentName;
    private LocalDateTime escalatedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<TicketMessageDto> messages;

    public TicketResponse() {}

    public static TicketResponse fromEntity(Ticket ticket) {
        TicketResponse res = new TicketResponse();
        res.setId(ticket.getId());
        res.setTicketNumber(ticket.getTicketNumber());
        res.setTitle(ticket.getTitle());
        res.setDescription(ticket.getDescription());
        res.setCategory(ticket.getCategory());
        res.setPriority(ticket.getPriority());
        res.setStatus(ticket.getStatus());
        res.setSentimentScore(ticket.getSentimentScore());
        res.setSentimentLabel(ticket.getSentimentLabel());
        if (ticket.getCustomer() != null) {
            res.setCustomerId(ticket.getCustomer().getId());
            res.setCustomerName(ticket.getCustomer().getFullName());
            res.setCustomerEmail(ticket.getCustomer().getEmail());
        }
        if (ticket.getAssignedAgent() != null) {
            res.setAssignedAgentId(ticket.getAssignedAgent().getId());
            res.setAssignedAgentName(ticket.getAssignedAgent().getFullName());
        }
        res.setEscalatedAt(ticket.getEscalatedAt());
        res.setCreatedAt(ticket.getCreatedAt());
        res.setUpdatedAt(ticket.getUpdatedAt());
        return res;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTicketNumber() { return ticketNumber; }
    public void setTicketNumber(String ticketNumber) { this.ticketNumber = ticketNumber; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public TicketCategory getCategory() { return category; }
    public void setCategory(TicketCategory category) { this.category = category; }
    public TicketPriority getPriority() { return priority; }
    public void setPriority(TicketPriority priority) { this.priority = priority; }
    public TicketStatus getStatus() { return status; }
    public void setStatus(TicketStatus status) { this.status = status; }
    public Double getSentimentScore() { return sentimentScore; }
    public void setSentimentScore(Double sentimentScore) { this.sentimentScore = sentimentScore; }
    public SentimentType getSentimentLabel() { return sentimentLabel; }
    public void setSentimentLabel(SentimentType sentimentLabel) { this.sentimentLabel = sentimentLabel; }
    public Long getCustomerId() { return customerId; }
    public void setCustomerId(Long customerId) { this.customerId = customerId; }
    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }
    public String getCustomerEmail() { return customerEmail; }
    public void setCustomerEmail(String customerEmail) { this.customerEmail = customerEmail; }
    public Long getAssignedAgentId() { return assignedAgentId; }
    public void setAssignedAgentId(Long assignedAgentId) { this.assignedAgentId = assignedAgentId; }
    public String getAssignedAgentName() { return assignedAgentName; }
    public void setAssignedAgentName(String assignedAgentName) { this.assignedAgentName = assignedAgentName; }
    public LocalDateTime getEscalatedAt() { return escalatedAt; }
    public void setEscalatedAt(LocalDateTime escalatedAt) { this.escalatedAt = escalatedAt; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    public List<TicketMessageDto> getMessages() { return messages; }
    public void setMessages(List<TicketMessageDto> messages) { this.messages = messages; }
}
