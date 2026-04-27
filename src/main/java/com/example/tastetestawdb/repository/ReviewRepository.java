package com.example.tastetestawdb.repository;

import com.example.tastetestawdb.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ReviewRepository extends JpaRepository<Review, UUID> {
    Optional<Review> findReviewById(UUID id);
    @Query("SELECT r FROM Review r WHERE r.restaurantId = :restaurantId")
    Optional<List<Review>> findAllByRestaurantId(@Param("restaurantId") UUID restaurantId);
    @Query("SELECT r FROM Review r WHERE r.userId = :userId")
    Optional<List<Review>> findAllByUserId(@Param("userId") UUID userId);
}
