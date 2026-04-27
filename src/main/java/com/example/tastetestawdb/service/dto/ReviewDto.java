package com.example.tastetestawdb.service.dto;

import java.time.LocalDateTime;

public class ReviewDto {
    private String comment;
    private int rating;
    private String urserName;
    private LocalDateTime createdAt;

    public ReviewDto(String comment, int rating, String urserName, LocalDateTime createdAt) {
        this.comment = comment;
        this.rating = rating;
        this.urserName = urserName;
        this.createdAt = createdAt;
    }

    public ReviewDto() {
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
}
