package com.supportai.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "faq_articles")
public class FaqArticle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private TicketCategory category;

    @Column(length = 255)
    private String tags;

    @Column(nullable = false)
    private int viewCount = 0;

    @Column(nullable = false)
    private int helpfulCount = 0;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public FaqArticle() {
        this.createdAt = LocalDateTime.now();
    }

    public FaqArticle(String title, String content, TicketCategory category, String tags) {
        this.title = title;
        this.content = content;
        this.category = category;
        this.tags = tags;
        this.viewCount = 0;
        this.helpfulCount = 0;
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

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public TicketCategory getCategory() {
        return category;
    }

    public void setCategory(TicketCategory category) {
        this.category = category;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public int getViewCount() {
        return viewCount;
    }

    public void setViewCount(int viewCount) {
        this.viewCount = viewCount;
    }

    public int getHelpfulCount() {
        return helpfulCount;
    }

    public void setHelpfulCount(int helpfulCount) {
        this.helpfulCount = helpfulCount;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
