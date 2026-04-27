package com.example.tastetestawdb.entity;

import jakarta.persistence.*;

import java.security.Timestamp;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "reviews", schema = "project")
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    private UUID id;
    @Column(name = "rating")
    private int rating;
    @Column(name = "comment")
    private String comment;
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    @Column(name = "user_id")
    private UUID userId;
    @Column(name = "restaurant_id")
    private UUID restaurantId;

    public Review(UUID id, int rating, String comment, LocalDateTime createdAt, UUID userId, UUID restaurantId) {
        this.id = id;
        this.rating = rating;
        this.comment = comment;
        this.createdAt = createdAt;
        this.userId = userId;
        this.restaurantId = restaurantId;
    }

    public Review() {
    }

    public UUID getId() {
        return id;
    }

    public int getRating() {
        return rating;
    }

    public String getComment() {
        return comment;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public UUID getUserId() {
        return userId;
    }

    public UUID getRestaurantId() {
        return restaurantId;
    }

    public Review setId(UUID id) {
        this.id = id;
        return this;
    }

    public Review setRating(int rating) {
        this.rating = rating;
        return this;
    }

    public Review setComment(String comment) {
        this.comment = comment;
        return this;
    }

    public Review setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
        return this;
    }

    public Review setUserId(UUID userId) {
        this.userId = userId;
        return this;
    }

    public Review setRestaurantId(UUID restaurantId) {
        this.restaurantId = restaurantId;
        return this;
    }
}
