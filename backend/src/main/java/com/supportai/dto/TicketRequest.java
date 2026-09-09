package com.supportai.dto;

import com.supportai.model.TicketCategory;
import com.supportai.model.TicketPriority;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class TicketRequest {
    @NotBlank
    @Size(min = 5, max = 200)
    private String title;

    @NotBlank
    @Size(min = 10)
    private String description;

    private TicketCategory category;

    private TicketPriority priority;

    public TicketRequest() {}

    public TicketRequest(String title, String description, TicketCategory category, TicketPriority priority) {
        this.title = title;
        this.description = description;
        this.category = category;
        this.priority = priority;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public TicketCategory getCategory() {
        return category;
    }

    public void setCategory(TicketCategory category) {
        this.category = category;
    }

    public TicketPriority getPriority() {
        return priority;
    }

    public void setPriority(TicketPriority priority) {
        this.priority = priority;
    }
}
