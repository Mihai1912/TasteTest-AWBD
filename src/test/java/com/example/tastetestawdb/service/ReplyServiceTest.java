package com.example.tastetestawdb.service;

import com.example.tastetestawdb.entity.Reply;
import com.example.tastetestawdb.entity.Review;
import com.example.tastetestawdb.repository.ReplyRepository;
import com.example.tastetestawdb.repository.RestaurantRepository;
import com.example.tastetestawdb.repository.ReviewRepository;
import com.example.tastetestawdb.repository.UserRepository;
import com.example.tastetestawdb.service.dto.ReplyDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ReplyServiceTest {

    @Mock
    private ReplyRepository replyRepository;
    @Mock
    private ReviewRepository reviewRepository;
    @Mock
    private RestaurantRepository restaurantRepository;
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ReplyService replyService;

    @Test
    void addReply_savesAndReturnsDto() {
        UUID reviewId = UUID.randomUUID();
        Review review = new Review().setId(reviewId).setRestaurantId(UUID.randomUUID());
        when(reviewRepository.findReviewById(reviewId)).thenReturn(Optional.of(review));
        when(replyRepository.save(any(Reply.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ReplyDto input = new ReplyDto();
        input.setText("thanks for the review");
        ReplyDto result = replyService.addReply(input, reviewId);

        assertEquals("thanks for the review", result.getText());
        verify(replyRepository).save(any(Reply.class));
    }

    @Test
    void addReply_reviewNotFound_throws() {
        UUID reviewId = UUID.randomUUID();
        when(reviewRepository.findReviewById(reviewId)).thenReturn(Optional.empty());

        ReplyDto input = new ReplyDto();
        input.setText("x");
        assertThrows(RuntimeException.class, () -> replyService.addReply(input, reviewId));
    }

    @Test
    void getReply_found_returnsDto() {
        UUID id = UUID.randomUUID();
        Reply reply = new Reply();
        reply.setId(id);
        reply.setText("hello");
        when(replyRepository.findReplyById(id)).thenReturn(Optional.of(reply));

        assertEquals("hello", replyService.getReply(id).getText());
    }

    @Test
    void getReply_notFound_throws() {
        UUID id = UUID.randomUUID();
        when(replyRepository.findReplyById(id)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> replyService.getReply(id));
    }

    @Test
    void getAllRepliesOfReview_returnsList() {
        UUID reviewId = UUID.randomUUID();
        when(reviewRepository.findReviewById(reviewId)).thenReturn(Optional.of(new Review().setId(reviewId)));
        Reply reply = new Reply();
        reply.setId(UUID.randomUUID());
        reply.setText("a reply");
        when(replyRepository.findAllByReviewId(reviewId)).thenReturn(List.of(reply));

        assertEquals(1, replyService.getAllRepliesOfReview(reviewId).size());
    }
}
