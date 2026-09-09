package com.supportai.dto;

import com.supportai.model.TicketCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class FaqRequest {
    @NotBlank
    private String title;

    @NotBlank
    private String content;

    @NotNull
    private TicketCategory category;

    private String tags;

    public FaqRequest() {}

    public FaqRequest(String title, String content, TicketCategory category, String tags) {
        this.title = title;
        this.content = content;
        this.category = category;
        this.tags = tags;
    }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public TicketCategory getCategory() { return category; }
    public void setCategory(TicketCategory category) { this.category = category; }
    public String getTags() { return tags; }
    public void setTags(String tags) { this.tags = tags; }
}
