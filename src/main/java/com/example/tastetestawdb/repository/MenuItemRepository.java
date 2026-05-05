package com.example.tastetestawdb.repository;

import com.example.tastetestawdb.entity.MenuItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MenuItemRepository extends JpaRepository<MenuItem, UUID> {
    Optional<MenuItem> findMenuItemById(UUID menuItemId);
    @Query("SELECT mi FROM MenuItem mi WHERE mi.menuId = :menuId")
    Optional<List<MenuItem>> findMenuItemsByMenuId(UUID menuId);

    Page<MenuItem> findByMenuId(UUID menuId, Pageable pageable);
}
