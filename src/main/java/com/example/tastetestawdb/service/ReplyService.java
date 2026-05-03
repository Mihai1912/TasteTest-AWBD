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
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

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
        reply.setReviewId(review.getId());
        reply.setRestaurantId(review.getRestaurantId());

        replyRepository.save(reply);

        return replyDto;
    }

    public UUID deleteReply(UUID replyId) {
        Reply reply = checkReply(replyId);
        checkReplyOwner(replyId);
        replyRepository.delete(reply);
        return replyId;
    }

    public ReplyDto updateReply(UUID replyId, ReplyDto replyDto) {
        Reply reply = checkReply(replyId);
        checkReplyOwner(replyId);
        reply.setText(replyDto.getText());
        replyRepository.save(reply);
        return replyDto;
    }

    public List<ReplyDto> getAllRepliesOfReview(UUID reviewId) {
        Review review = checkReview(reviewId);
        List<Reply> replies = replyRepository.findAllByReviewId(review.getId());
        return replies.stream().map(reply -> new ReplyDto(reply.getText())).toList();
    }

    public ReplyDto getReply(UUID replyId) {
        return new ReplyDto(checkReply(replyId).getText());
    }

    private Restaurant checkRestaurant(String restaurantName) {
        Optional<Restaurant> restaurant = restaurantRepository.findRestaurantByName(restaurantName);
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

    private void checkReplyOwner(UUID replyId) {
        Reply reply = checkReply(replyId);
        Optional<User> user = userRepository.findUserByEmail(SecurityContextHolder.getContext().getAuthentication().getName());
        if (!reply.getRestaurantId().equals(user.get().getId())) {
            throw new RuntimeException("You are not the owner of this reply");
        }
    }
}
