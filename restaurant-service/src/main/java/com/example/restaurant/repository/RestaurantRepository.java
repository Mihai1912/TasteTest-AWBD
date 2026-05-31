package com.example.restaurant.repository;

import com.example.restaurant.entity.Restaurant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface RestaurantRepository extends JpaRepository<Restaurant, UUID> {
    Optional<Restaurant> findRestaurantById(UUID id);
    Optional<Restaurant> findRestaurantByName(String name);
}
