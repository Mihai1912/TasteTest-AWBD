package com.example.tastetestawdb.service;

import com.example.tastetestawdb.controller.AuthController;
import com.example.tastetestawdb.entity.Restaurant;
import com.example.tastetestawdb.entity.Review;
import com.example.tastetestawdb.entity.User;
import com.example.tastetestawdb.repository.RestaurantRepository;
import com.example.tastetestawdb.repository.ReviewRepository;
import com.example.tastetestawdb.repository.UserRepository;
import com.example.tastetestawdb.service.dto.RestaurantDto;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@Transactional
public class RestaurantService {
    private final RestaurantRepository restaurantRepository;
    private final UserRepository userRepository;
    private final ReviewRepository reviewRepository;

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    public RestaurantService(RestaurantRepository restaurantRepository,
                             UserRepository userRepository, ReviewRepository reviewRepository) {
        this.restaurantRepository = restaurantRepository;
        this.userRepository = userRepository;
        this.reviewRepository = reviewRepository;
    }

    public RestaurantDto addRestaurant(RestaurantDto restaurantDto) {
        Restaurant restaurant = new Restaurant();
        Optional<User> owner = userRepository.findUserByEmail(SecurityContextHolder.getContext().getAuthentication().getName());

        restaurant.setName(restaurantDto.getName());
        restaurant.setAddress(restaurantDto.getAddress());
        restaurant.setPhone(restaurantDto.getPhone());
        restaurant.setWebsite(restaurantDto.getWebsite());
        restaurant.setSchedule(restaurantDto.getSchedule());
        restaurant.setOwnerId(owner.get().getId());

        restaurantRepository.save(restaurant);

        return restaurantDto;
    }

    public void deleteRestaurant(UUID id) {
        logger.info("Deleting restaurant with id: " + id);
        Restaurant restaurant = checkRestaurant(id);
        logger.info("Restaurant found: " + restaurant.getName());
        checkOwner(restaurant);
        logger.info("Owner found: " + restaurant.getOwnerId());
        restaurantRepository.delete(restaurant);
    }

    public RestaurantDto updateRestaurant(UUID id, RestaurantDto restaurantDto) {
        Restaurant restaurant = checkRestaurant(id);
        checkOwner(restaurant);

        restaurant.setName(restaurantDto.getName());
        restaurant.setAddress(restaurantDto.getAddress());
        restaurant.setPhone(restaurantDto.getPhone());
        restaurant.setWebsite(restaurantDto.getWebsite());
        restaurant.setSchedule(restaurantDto.getSchedule());

        restaurantRepository.save(restaurant);

        return restaurantDto;
    }

    public RestaurantDto getRestaurant(UUID id) {
        Restaurant restaurantEntity = checkRestaurant(id);
        return new RestaurantDto(restaurantEntity.getName(),
                restaurantEntity.getAddress(),
                restaurantEntity.getPhone(),
                restaurantEntity.getWebsite(),
                restaurantEntity.getSchedule());
    }

    public List<RestaurantDto> getAllRestaurants() {
        List<Restaurant> restaurants = restaurantRepository.findAll();
        return restaurants.stream().map(restaurant -> new RestaurantDto(
                restaurant.getName(),
                restaurant.getAddress(),
                restaurant.getPhone(),
                restaurant.getWebsite(),
                restaurant.getSchedule()
        )).toList();
    }

    public double getRatings(UUID id) {
        Optional<List<Review>> reviews = reviewRepository.findAllByRestaurantId(id);
        if (reviews.isEmpty()) {
            throw new RuntimeException("No reviews found for this restaurant");
        }

        double sum = reviews.get().stream().mapToDouble(Review::getRating).sum();
        return sum / reviews.get().size();
    }

    private Restaurant checkRestaurant(UUID id) {
        Optional<Restaurant> restaurant = restaurantRepository.findRestaurantById(id);
        if (restaurant.isEmpty()) {
            throw new RuntimeException("Restaurant not found");
        }
        return restaurant.get();
    }

    private void checkOwner(Restaurant restaurant) {
        Optional<User> owner = userRepository.findUserByEmail(SecurityContextHolder.getContext().getAuthentication().getName());
        logger.info("Owner found: " + owner.get().getId());
        if (!restaurant.getOwnerId().equals(owner.get().getId())) {
            logger.error("You are not the owner of this restaurant");
            logger.error(restaurant.getOwnerId() + " " + owner.get().getId());
            throw new RuntimeException("You are not the owner of this restaurant");
        }
        owner.get();
    }

    public List<RestaurantDto> getTopRatedRestaurants() {
        List<Restaurant> restaurants = restaurantRepository.findAll();
        Map<UUID, Double> restaurantRatings = new HashMap<>();
        for (Restaurant restaurant : restaurants) {
            List<Review> reviews = reviewRepository.findAllByRestaurantId(restaurant.getId()).orElse(Collections.emptyList());
            double averageRating = reviews.stream().mapToDouble(Review::getRating).average().orElse(0.0);
            restaurantRatings.put(restaurant.getId(), averageRating);
        }
        List<RestaurantDto> topRatedRestaurants = new ArrayList<>();
        restaurantRatings.entrySet().stream()
                .sorted(Map.Entry.<UUID, Double>comparingByValue().reversed())
                .limit(3)
                .forEach(entry -> {
                    Restaurant restaurant = restaurantRepository.findRestaurantById(entry.getKey()).orElse(null);
                    if (restaurant != null) {
                        topRatedRestaurants.add(new RestaurantDto(
                                restaurant.getName(),
                                restaurant.getAddress(),
                                restaurant.getPhone(),
                                restaurant.getWebsite(),
                                restaurant.getSchedule()
                        ));
                    }
                });
        return topRatedRestaurants;
    }

    public UUID getRestaurantId (String name) {
        Optional<Restaurant> restaurant = restaurantRepository.findRestaurantByName(name);
        if (restaurant.isEmpty()) {
            throw new RuntimeException("Restaurant not found");
        }
        return restaurant.get().getId();
    }
}
