package com.example.tastetestawdb.service.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public class ReviewDto {
    @NotBlank(message = "Comentariul este obligatoriu")
    @Size(max = 2000, message = "Comentariul poate avea maximum 2000 de caractere")
    private String comment;

    @Min(value = 1, message = "Nota minima este 1")
    @Max(value = 5, message = "Nota maxima este 5")
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
