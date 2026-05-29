package com.example.tastetestawdb.service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.UUID;

public class ReplyDto {
    private UUID id;

    @NotBlank(message = "Textul raspunsului este obligatoriu")
    @Size(max = 2000, message = "Raspunsul poate avea maximum 2000 de caractere")
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
