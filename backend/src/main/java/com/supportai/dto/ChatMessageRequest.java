package com.supportai.dto;

import jakarta.validation.constraints.NotBlank;

public class ChatMessageRequest {
    private String sessionId;

    @NotBlank
    private String message;

    private String customerName;

    public ChatMessageRequest() {}

    public ChatMessageRequest(String sessionId, String message, String customerName) {
        this.sessionId = sessionId;
        this.message = message;
        this.customerName = customerName;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }
}
