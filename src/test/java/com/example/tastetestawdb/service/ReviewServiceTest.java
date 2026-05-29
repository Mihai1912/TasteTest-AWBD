package com.example.tastetestawdb.service;

import com.example.tastetestawdb.entity.Restaurant;
import com.example.tastetestawdb.entity.Review;
import com.example.tastetestawdb.entity.User;
import com.example.tastetestawdb.repository.RestaurantRepository;
import com.example.tastetestawdb.repository.ReviewRepository;
import com.example.tastetestawdb.repository.UserRepository;
import com.example.tastetestawdb.service.dto.ReviewDto;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
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
class ReviewServiceTest {

    @Mock
    private ReviewRepository reviewRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private RestaurantRepository restaurantRepository;

    @InjectMocks
    private ReviewService reviewService;

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private void authenticateAs(String email) {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(email, null,
                        List.of(new SimpleGrantedAuthority("USER"))));
    }

    @Test
    void getReview_found_returnsDto() {
        UUID reviewId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        Review review = new Review()
                .setId(reviewId).setComment("good").setRating(5)
                .setUserId(userId).setCreatedAt(LocalDateTime.now());
        when(reviewRepository.findReviewById(reviewId)).thenReturn(Optional.of(review));
        when(userRepository.findUserById(userId)).thenReturn(Optional.of(new User().setId(userId).setUsername("john")));

        ReviewDto dto = reviewService.getReview(reviewId);

        assertEquals("good", dto.getComment());
        assertEquals("john", dto.getUrserName());
    }

    @Test
    void getReview_notFound_throws() {
        UUID reviewId = UUID.randomUUID();
        when(reviewRepository.findReviewById(reviewId)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> reviewService.getReview(reviewId));
    }

    @Test
    void addReview_savesAndReturnsDto() {
        authenticateAs("user@test.com");
        UUID restaurantId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        Restaurant restaurant = new Restaurant();
        restaurant.setId(restaurantId);
        User user = new User().setId(userId).setEmail("user@test.com").setUsername("john");

        when(restaurantRepository.findRestaurantById(restaurantId)).thenReturn(Optional.of(restaurant));
        when(userRepository.existsUserByEmail("user@test.com")).thenReturn(true);
        when(userRepository.findUserByEmail("user@test.com")).thenReturn(Optional.of(user));
        when(userRepository.findUserById(userId)).thenReturn(Optional.of(user));

        ReviewDto input = new ReviewDto();
        input.setComment("tasty");
        ReviewDto result = reviewService.addReview(input, restaurantId, 5);

        assertEquals("tasty", result.getComment());
        assertEquals(5, result.getRating());
        assertEquals("john", result.getUrserName());
        verify(reviewRepository).save(any(Review.class));
    }

    @Test
    void deleteReview_byAuthor_deletes() {
        authenticateAs("user@test.com");
        UUID reviewId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        Review review = new Review().setId(reviewId).setUserId(userId);

        when(userRepository.existsUserByEmail("user@test.com")).thenReturn(true);
        when(reviewRepository.findReviewById(reviewId)).thenReturn(Optional.of(review));
        when(userRepository.findUserByEmail("user@test.com"))
                .thenReturn(Optional.of(new User().setId(userId).setEmail("user@test.com")));

        UUID result = reviewService.deleteReview(reviewId);

        assertEquals(reviewId, result);
        verify(reviewRepository).delete(review);
    }

    @Test
    void deleteReview_notAuthor_throws() {
        authenticateAs("user@test.com");
        UUID reviewId = UUID.randomUUID();
        UUID reviewOwnerId = UUID.fromString("11111111-1111-1111-1111-111111111111");
        UUID currentUserId = UUID.fromString("22222222-2222-2222-2222-222222222222");
        Review review = new Review().setId(reviewId).setUserId(reviewOwnerId);

        when(userRepository.existsUserByEmail("user@test.com")).thenReturn(true);
        when(reviewRepository.findReviewById(reviewId)).thenReturn(Optional.of(review));
        when(userRepository.findUserByEmail("user@test.com"))
                .thenReturn(Optional.of(new User().setId(currentUserId).setEmail("user@test.com")));

        assertThrows(RuntimeException.class, () -> reviewService.deleteReview(reviewId));
    }
}
