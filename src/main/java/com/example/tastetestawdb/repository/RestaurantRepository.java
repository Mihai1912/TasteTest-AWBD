package com.example.tastetestawdb.repository;

import com.example.tastetestawdb.entity.Restaurant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface RestaurantRepository extends JpaRepository<Restaurant, UUID> {
    Optional<Restaurant> findRestaurantByName(String name);
    Optional<Restaurant> findRestaurantById(UUID id);
}
