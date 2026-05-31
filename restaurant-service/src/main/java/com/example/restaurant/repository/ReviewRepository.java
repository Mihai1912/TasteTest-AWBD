package com.example.restaurant.repository;

import com.example.restaurant.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ReviewRepository extends JpaRepository<Review, UUID> {
    Optional<List<Review>> findAllByRestaurantId(UUID restaurantId);
}
