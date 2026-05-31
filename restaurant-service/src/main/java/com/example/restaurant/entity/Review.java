package com.example.restaurant.entity;

import jakarta.persistence.*;

import java.util.UUID;

@Entity
@Table(name = "reviews", schema = "project")
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "comment")
    private String comment;

    @Column(name = "rating")
    private Integer rating;

    @Column(name = "restaurant_id")
    private UUID restaurantId;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }
    public Integer getRating() { return rating; }
    public void setRating(Integer rating) { this.rating = rating; }
    public UUID getRestaurantId() { return restaurantId; }
    public void setRestaurantId(UUID restaurantId) { this.restaurantId = restaurantId; }
}
