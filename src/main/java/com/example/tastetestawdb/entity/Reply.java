package com.example.tastetestawdb.entity;

import jakarta.persistence.*;

import java.security.Timestamp;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "replies", schema = "project")
public class Reply {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    private UUID id;
    @Column(name = "text")
    private String text;
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    @Column(name = "review_id")
    private UUID reviewId;
    @Column(name = "restaurant_id")
    private UUID restaurantId;

    public Reply(UUID id, String text, LocalDateTime createdAt, UUID reviewId, UUID restaurantId) {
        this.id = id;
        this.text = text;
        this.createdAt = createdAt;
        this.reviewId = reviewId;
        this.restaurantId = restaurantId;
    }

    public Reply() {
    }

    public UUID getId() {
        return id;
    }

    public String getText() {
        return text;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public UUID getReviewId() {
        return reviewId;
    }

    public UUID getRestaurantId() {
        return restaurantId;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public void setText(String text) {
        this.text = text;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public void setReviewId(UUID reviewId) {
        this.reviewId = reviewId;
    }

    public void setRestaurantId(UUID restaurantId) {
        this.restaurantId = restaurantId;
    }
}
