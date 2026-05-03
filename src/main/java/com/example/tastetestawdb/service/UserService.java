package com.example.tastetestawdb.service;

import com.example.tastetestawdb.entity.Review;
import com.example.tastetestawdb.repository.ReplyRepository;
import com.example.tastetestawdb.repository.ReviewRepository;
import com.example.tastetestawdb.repository.UserRepository;
import com.example.tastetestawdb.service.dto.ReplyDto;
import com.example.tastetestawdb.service.dto.ReviewDto;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class UserService {
    private final UserRepository userRepository;
    private final ReviewRepository reviewRepository;
    private final ReplyRepository replyRepository;

    public UserService(UserRepository userRepository, ReviewRepository reviewRepository, ReplyRepository replyRepository) {
        this.userRepository = userRepository;
        this.reviewRepository = reviewRepository;
        this.replyRepository = replyRepository;
    }

    public List<ReviewDto> getUserReviews(UUID id) {
        Optional<List<Review>> reviews = reviewRepository.findAllByUserId(id);
        if (reviews.isEmpty()){
            throw new RuntimeException("No reviews found for user with id: " + id);
        }
        List<ReviewDto> reviewDtos = new ArrayList<>();
        for (Review review: reviews.get()) {
            ReviewDto reviewDto = new ReviewDto();
            reviewDto.setComment(review.getComment());
            reviewDtos.add(reviewDto);
        }
        return reviewDtos;
    }
}
