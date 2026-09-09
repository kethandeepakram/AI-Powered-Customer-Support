package com.supportai.dto;

import jakarta.validation.constraints.NotBlank;

public class ChatEscalateRequest {
    @NotBlank
    private String sessionId;

    private String customerName;
    private String customerEmail;
    private String title;
    private String reason;

    public ChatEscalateRequest() {}

    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }
    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }
    public String getCustomerEmail() { return customerEmail; }
    public void setCustomerEmail(String customerEmail) { this.customerEmail = customerEmail; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
}
