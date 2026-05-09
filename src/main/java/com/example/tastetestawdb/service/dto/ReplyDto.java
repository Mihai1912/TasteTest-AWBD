package com.example.tastetestawdb.service.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public class ReplyDto {
    private UUID id;
    private String text;
    private LocalDateTime createdAt;

    public ReplyDto(UUID id, String text, LocalDateTime createdAt) {
        this.id = id;
        this.text = text;
        this.createdAt = createdAt;
    }

    public ReplyDto() {
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
