package com.supportai.dto;

import jakarta.validation.constraints.NotBlank;

public class TicketMessageRequest {
    @NotBlank
    private String message;

    public TicketMessageRequest() {}

    public TicketMessageRequest(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
