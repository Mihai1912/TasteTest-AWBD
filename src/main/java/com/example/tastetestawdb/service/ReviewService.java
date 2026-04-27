package com.example.tastetestawdb.service;

import com.example.tastetestawdb.entity.Restaurant;
import com.example.tastetestawdb.entity.Review;
import com.example.tastetestawdb.entity.User;
import com.example.tastetestawdb.repository.RestaurantRepository;
import com.example.tastetestawdb.repository.ReviewRepository;
import com.example.tastetestawdb.repository.UserRepository;
import com.example.tastetestawdb.service.dto.ReviewDto;
import com.example.tastetestawdb.service.dto.ReviewIdDto;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class ReviewService {
    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;
    private final RestaurantRepository restaurantRepository;

    public static final Logger log = LoggerFactory.getLogger(ReviewService.class);

    public ReviewService(ReviewRepository reviewRepository, UserRepository userRepository, RestaurantRepository restaurantRepository) {
        this.reviewRepository = reviewRepository;
        this.userRepository = userRepository;
        this.restaurantRepository = restaurantRepository;
    }

    public ReviewDto addReview(ReviewDto reviewDto, UUID restaurantId, int rating) {
        Restaurant restaurant = checkRestaurant(restaurantId);
        checkUser(SecurityContextHolder.getContext().getAuthentication().getName());

        Review review = new Review();
        review.setRating(rating);
        review.setComment(reviewDto.getComment());
        review.setUserId(userRepository.findUserByEmail(SecurityContextHolder.getContext().getAuthentication().getName()).get().getId());
        review.setRestaurantId(restaurantRepository.findRestaurantById(restaurantId).get().getId());
        review.setCreatedAt(java.time.LocalDateTime.now());

        reviewRepository.save(review);

        ReviewDto reviewDto2 = new ReviewDto();
        reviewDto2.setComment(review.getComment());
        reviewDto2.setRating(review.getRating());
        reviewDto2.setUrserName(userRepository.findUserById(review.getUserId()).get().getUsername());
        reviewDto2.setCreatedAt(review.getCreatedAt());

        return reviewDto2;
    }

    public UUID deleteReview(UUID reviewId) {
        checkUser(SecurityContextHolder.getContext().getAuthentication().getName());

        Optional<Review> review = reviewRepository.findReviewById(reviewId);
        if (review.isEmpty())
            throw new RuntimeException("Review not found in the database");
        if (!review.get().getUserId().equals(userRepository.findUserByEmail(SecurityContextHolder.getContext().getAuthentication().getName()).get().getId()))
            throw new RuntimeException("You are not the author of this review");

        reviewRepository.delete(review.get());
        return reviewId;
    }

    public ReviewDto updateReview(UUID reviewId, ReviewDto reviewDto) {
        checkUser(SecurityContextHolder.getContext().getAuthentication().getName());

        Optional<Review> review = reviewRepository.findReviewById(reviewId);
        if (review.isEmpty())
            throw new RuntimeException("Review not found in the database");
        if (!review.get().getUserId().equals(userRepository.findUserByEmail(SecurityContextHolder.getContext().getAuthentication().getName()).get().getId()))
            throw new RuntimeException("You are not the author of this review");

        review.get().setComment(reviewDto.getComment());
        review.get().setRating(reviewDto.getRating());
        review.get().setCreatedAt(java.time.LocalDateTime.now());
        review.get().setUserId(userRepository.findUserByEmail(SecurityContextHolder.getContext().getAuthentication().getName()).get().getId());
        reviewRepository.save(review.get());

        ReviewDto reviewDto2 = new ReviewDto();
        reviewDto2.setComment(review.get().getComment());
        reviewDto2.setRating(review.get().getRating());
        reviewDto2.setUrserName(userRepository.findUserById(review.get().getUserId()).get().getUsername());
        reviewDto2.setCreatedAt(review.get().getCreatedAt());

        return reviewDto2;
    }

    public ReviewDto getReview(UUID reviewId) {
        log.info("Request to get review {}", reviewId);
        Optional<Review> review = reviewRepository.findReviewById(reviewId);
        log.info("Review ID: {}", reviewId);
        if (review.isEmpty())
            throw new RuntimeException("Review not found in the database");

        log.info("Review comment: {}", review.get().getComment());
        User user = userRepository.findUserById(review.get().getUserId()).orElseThrow(() -> new RuntimeException("User not found in the database"));
        log.info("User ID: {}", user.getId());
        return new ReviewDto(review.get().getComment(), review.get().getRating(), user.getUsername(), review.get().getCreatedAt());
    }

    public List<ReviewIdDto> getRestaurantReviews(UUID restaurantId) {

        Restaurant restaurant = checkRestaurant(restaurantId);
        Optional<List<Review>> reviews = reviewRepository.findAllByRestaurantId(restaurantId);
        return reviews.get().stream()
                .map(review -> {
                    User user = userRepository.findUserById(review.getUserId()).orElseThrow(() -> new RuntimeException("User not found in the database"));
                    return new ReviewIdDto(review.getComment(), review.getRating(), user.getUsername(), review.getCreatedAt(), review.getId());
                })
                .collect(Collectors.toList());
    }

    private Restaurant checkRestaurant(UUID restaurantId) {
        return restaurantRepository.findRestaurantById(restaurantId).orElseThrow(() -> new RuntimeException("Restaurant not found"));
    }

    private void checkUser(String email) {
        if (!userRepository.existsUserByEmail(email))
            throw new RuntimeException("User not found in the database");
    }
}
