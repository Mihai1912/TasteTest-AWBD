package com.example.tastetestawdb.controller;

import com.example.tastetestawdb.service.ReviewService;
import com.example.tastetestawdb.service.dto.ReviewDto;
import com.example.tastetestawdb.service.dto.ReviewIdDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/review")
public class ReviewController implements SecuredRestController {
    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    public static final Logger log = LoggerFactory.getLogger(ReviewController.class);

    @RequestMapping(path = "/add/{restaurantId}", method = RequestMethod.POST)
    @PreAuthorize("hasAuthority('USER') or hasAuthority('ADMIN') or hasAuthority('RESTAURANT_OWNER')")
    public ResponseEntity<ReviewDto> addReview(@RequestBody ReviewDto reviewDto,
                                               @PathVariable UUID restaurantId,
                                               @RequestParam int rating) {
        try {
            return ResponseEntity.status(201).body(reviewService.addReview(reviewDto, restaurantId, rating));
        } catch (Exception e) {
            return ResponseEntity.status(400).body(null);
        }
    }

    @RequestMapping(path = "/delete/{reviewId}", method = RequestMethod.DELETE)
    @PreAuthorize("hasAuthority('USER') or hasAuthority('ADMIN') or hasAuthority('RESTAURANT_OWNER')")
    public ResponseEntity<UUID> deleteReview(@PathVariable UUID reviewId) {
        try {
            return ResponseEntity.status(200).body(reviewService.deleteReview(reviewId));
        } catch (Exception e) {
            return ResponseEntity.status(400).body(null);
        }
    }

    @RequestMapping(path = "/update/{reviewId}", method = RequestMethod.PUT)
    @PreAuthorize("hasAuthority('USER') or hasAuthority('ADMIN') or hasAuthority('RESTAURANT_OWNER')")
    public ResponseEntity<ReviewDto> updateReview(@PathVariable UUID reviewId,
                                                  @RequestBody ReviewDto reviewDto) {
        try {
            return ResponseEntity.status(200).body(reviewService.updateReview(reviewId, reviewDto));
        } catch (Exception e) {
            return ResponseEntity.status(400).body(null);
        }
    }

    @RequestMapping(path = "/get/{reviewId}", method = RequestMethod.GET)
    @PreAuthorize("hasAuthority('USER') or hasAuthority('ADMIN') or hasAuthority('RESTAURANT_OWNER')")
    public ResponseEntity<ReviewDto> getReview(@PathVariable UUID reviewId) {
        try {
            return ResponseEntity.status(200).body(reviewService.getReview(reviewId));
        } catch (Exception e) {
            log.error("Error getting review: {}", e.getMessage());
            return ResponseEntity.status(400).body(null);
        }
    }

    @RequestMapping(path = "/getRestaurantReviews/{restaurantId}", method = RequestMethod.GET)
    @PreAuthorize("hasAuthority('USER') or hasAuthority('ADMIN') or hasAuthority('RESTAURANT_OWNER')")
    public ResponseEntity<List<ReviewIdDto>> getRestaurantReviews(@PathVariable UUID restaurantId) {
        try {
            return ResponseEntity.status(200).body(reviewService.getRestaurantReviews(restaurantId));
        } catch (Exception e) {
            return ResponseEntity.status(400).body(null);
        }
    }
}