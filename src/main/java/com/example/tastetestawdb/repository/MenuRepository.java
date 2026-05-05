package com.example.tastetestawdb.repository;

import com.example.tastetestawdb.entity.Menu;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MenuRepository extends JpaRepository<Menu, UUID> {
    Optional<Menu> findMenuById(UUID id);

    List<Menu> findMenuByRestaurantId(UUID restaurantId);

    Page<Menu> findByRestaurantId(UUID restaurantId, Pageable pageable);
}

