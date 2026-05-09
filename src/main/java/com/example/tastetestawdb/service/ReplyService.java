package com.example.tastetestawdb.service;

import com.example.tastetestawdb.entity.Reply;
import com.example.tastetestawdb.entity.Restaurant;
import com.example.tastetestawdb.entity.Review;
import com.example.tastetestawdb.entity.User;
import com.example.tastetestawdb.repository.ReplyRepository;
import com.example.tastetestawdb.repository.RestaurantRepository;
import com.example.tastetestawdb.repository.ReviewRepository;
import com.example.tastetestawdb.repository.UserRepository;
import com.example.tastetestawdb.service.dto.ReplyDto;
import jakarta.transaction.Transactional;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class ReplyService {
    public final ReplyRepository replyRepository;
    public final ReviewRepository reviewRepository;
    public final RestaurantRepository restaurantRepository;
    public final UserRepository userRepository;

    public ReplyService(ReplyRepository replyRepository,
                        ReviewRepository reviewRepository,
                        RestaurantRepository restaurantRepository,
                        UserRepository userRepository) {
        this.replyRepository = replyRepository;
        this.reviewRepository = reviewRepository;
        this.restaurantRepository = restaurantRepository;
        this.userRepository = userRepository;
    }

    public ReplyDto addReply(ReplyDto replyDto, UUID reviewId) {
        Review review = checkReview(reviewId);
        Reply reply = new Reply();
        reply.setText(replyDto.getText());
        reply.setCreatedAt(LocalDateTime.now());
        reply.setReviewId(review.getId());
        reply.setRestaurantId(review.getRestaurantId());

        Reply saved = replyRepository.save(reply);

        return toDto(saved);
    }

    public UUID deleteReply(UUID replyId) {
        Reply reply = checkReply(replyId);
        checkReplyOwner(reply);
        replyRepository.delete(reply);
        return replyId;
    }

    public ReplyDto updateReply(UUID replyId, ReplyDto replyDto) {
        Reply reply = checkReply(replyId);
        checkReplyOwner(reply);
        reply.setText(replyDto.getText());
        Reply saved = replyRepository.save(reply);
        return toDto(saved);
    }

    public List<ReplyDto> getAllRepliesOfReview(UUID reviewId) {
        Review review = checkReview(reviewId);
        List<Reply> replies = replyRepository.findAllByReviewId(review.getId());
        return replies.stream().map(this::toDto).toList();
    }

    public ReplyDto getReply(UUID replyId) {
        return toDto(checkReply(replyId));
    }

    private ReplyDto toDto(Reply reply) {
        ReplyDto dto = new ReplyDto();
        dto.setId(reply.getId());
        dto.setText(reply.getText());
        dto.setCreatedAt(reply.getCreatedAt());
        return dto;
    }

    private Restaurant checkRestaurantById(UUID restaurantId) {
        Optional<Restaurant> restaurant = restaurantRepository.findRestaurantById(restaurantId);
        if (restaurant.isEmpty()) {
            throw new RuntimeException("Restaurant not found");
        }
        return restaurant.get();
    }

    private Review checkReview(UUID reviewId) {
        Optional<Review> review = reviewRepository.findReviewById(reviewId);
        if (review.isEmpty()) {
            throw new RuntimeException("Review not found");
        }
        return review.get();
    }

    private Reply checkReply(UUID replyId) {
        Optional<Reply> reply = replyRepository.findReplyById(replyId);
        if (reply.isEmpty()) {
            throw new RuntimeException("Reply not found");
        }
        return reply.get();
    }

    private void checkReplyOwner(Reply reply) {
        if (isAdmin()) {
            return;
        }

        Restaurant restaurant = checkRestaurantById(reply.getRestaurantId());
        User user = userRepository.findUserByEmail(SecurityContextHolder.getContext().getAuthentication().getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
        if (!restaurant.getOwnerId().equals(user.getId())) {
            throw new RuntimeException("You are not the owner of this reply");
        }
    }

    private boolean isAdmin() {
        return SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch("ADMIN"::equals);
    }
}
