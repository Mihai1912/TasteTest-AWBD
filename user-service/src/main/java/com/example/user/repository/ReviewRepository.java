package com.example.user.repository;

import com.example.user.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ReviewRepository extends JpaRepository<Review, UUID> {
    Optional<List<Review>> findAllByUserId(UUID userId);
}
