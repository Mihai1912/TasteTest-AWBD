package com.example.tastetestawdb.service.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public class ReviewIdDto {
    private String comment;
    private int rating;
    private String urserName;
    private LocalDateTime createdAt;
    private UUID id;

    public ReviewIdDto(String comment, int rating, String urserName, LocalDateTime createdAt, UUID id) {
        this.comment = comment;
        this.rating = rating;
        this.urserName = urserName;
        this.createdAt = createdAt;
        this.id = id;
    }

    public ReviewIdDto() {
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public String getUrserName() {
        return urserName;
    }

    public void setUrserName(String urserName) {
        this.urserName = urserName;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }
}
