package com.example.tastetestawdb.service;

import com.example.tastetestawdb.entity.Restaurant;
import com.example.tastetestawdb.entity.Review;
import com.example.tastetestawdb.entity.User;
import com.example.tastetestawdb.repository.RestaurantRepository;
import com.example.tastetestawdb.repository.ReviewRepository;
import com.example.tastetestawdb.repository.UserRepository;
import com.example.tastetestawdb.service.dto.RestaurantDto;
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
class RestaurantServiceTest {

    @Mock
    private RestaurantRepository restaurantRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private ReviewRepository reviewRepository;

    @InjectMocks
    private RestaurantService restaurantService;

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private Restaurant restaurant(UUID id, String name) {
        Restaurant r = new Restaurant();
        r.setId(id);
        r.setName(name);
        r.setAddress("addr");
        r.setPhone("123");
        r.setWebsite("web");
        r.setSchedule("sched");
        return r;
    }

    @Test
    void getRestaurant_found_returnsDto() {
        UUID id = UUID.randomUUID();
        when(restaurantRepository.findRestaurantById(id)).thenReturn(Optional.of(restaurant(id, "Pizza")));

        RestaurantDto dto = restaurantService.getRestaurant(id);

        assertEquals("Pizza", dto.getName());
        assertEquals(id, dto.getId());
    }

    @Test
    void getRestaurant_notFound_throws() {
        UUID id = UUID.randomUUID();
        when(restaurantRepository.findRestaurantById(id)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> restaurantService.getRestaurant(id));
    }

    @Test
    void getAllRestaurants_returnsAll() {
        when(restaurantRepository.findAll()).thenReturn(List.of(
                restaurant(UUID.randomUUID(), "A"),
                restaurant(UUID.randomUUID(), "B")));

        List<RestaurantDto> all = restaurantService.getAllRestaurants();

        assertEquals(2, all.size());
    }

    @Test
    void getRatings_returnsAverage() {
        UUID id = UUID.randomUUID();
        Review r1 = new Review();
        r1.setRating(4);
        Review r2 = new Review();
        r2.setRating(2);
        when(reviewRepository.findAllByRestaurantId(id)).thenReturn(Optional.of(List.of(r1, r2)));

        assertEquals(3.0, restaurantService.getRatings(id), 0.001);
    }

    @Test
    void getRatings_noReviews_throws() {
        UUID id = UUID.randomUUID();
        when(reviewRepository.findAllByRestaurantId(id)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> restaurantService.getRatings(id));
    }

    @Test
    void getRestaurantId_found_returnsId() {
        UUID id = UUID.randomUUID();
        when(restaurantRepository.findRestaurantByName("Pizza")).thenReturn(Optional.of(restaurant(id, "Pizza")));

        assertEquals(id, restaurantService.getRestaurantId("Pizza"));
    }

    @Test
    void getRestaurantId_notFound_throws() {
        when(restaurantRepository.findRestaurantByName("X")).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> restaurantService.getRestaurantId("X"));
    }

    @Test
    void addRestaurant_savesAndReturnsDto() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("owner@test.com", null,
                        List.of(new SimpleGrantedAuthority("RESTAURANT_OWNER"))));
        User owner = new User().setId(UUID.randomUUID()).setEmail("owner@test.com");
        when(userRepository.findUserByEmail("owner@test.com")).thenReturn(Optional.of(owner));

        RestaurantDto input = new RestaurantDto(null, "New", "addr", "phone", "web", "sched");
        RestaurantDto result = restaurantService.addRestaurant(input);

        assertEquals("New", result.getName());
        verify(restaurantRepository).save(any(Restaurant.class));
    }
}
