package com.example.tastetestawdb.service;

import com.example.tastetestawdb.entity.Menu;
import com.example.tastetestawdb.entity.MenuItem;
import com.example.tastetestawdb.entity.Restaurant;
import com.example.tastetestawdb.entity.User;
import com.example.tastetestawdb.repository.MenuItemRepository;
import com.example.tastetestawdb.repository.MenuRepository;
import com.example.tastetestawdb.repository.RestaurantRepository;
import com.example.tastetestawdb.repository.UserRepository;
import com.example.tastetestawdb.service.dto.MenuItemDto;

import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class MenuItemService {
    public final MenuItemRepository menuItemRepository;
    public final MenuRepository menuRepository;
    public final RestaurantRepository restaurantRepository;
    public final UserRepository userRepository;

    private static final Logger logger = LoggerFactory.getLogger(MenuItemService.class);

    public MenuItemService(MenuItemRepository menuItemRepository,
                           MenuRepository menuRepository,
                           RestaurantRepository restaurantRepository,
                           UserRepository userRepository) {
        this.menuItemRepository = menuItemRepository;
        this.menuRepository = menuRepository;
        this.restaurantRepository = restaurantRepository;
        this.userRepository = userRepository;
    }

    public MenuItemDto addMenuItem(MenuItemDto menuItemDto, UUID menuId) {
        Menu menu = checkMenu(menuId);
        checkRestaurantOwnerOrAdmin(menu);
        MenuItem menuItem = new MenuItem();
        menuItem.setName(menuItemDto.getName());
        menuItem.setPrice(menuItemDto.getPrice());
        menuItem.setDescription(menuItemDto.getDescription());
        menuItem.setMenuId(menuId);
        menuItemRepository.save(menuItem);
        return new MenuItemDto(menuItem.getId().toString(), menuItem.getMenuId().toString(), menuItem.getName(), menuItem.getPrice(), menuItem.getDescription());
    }

    public UUID deleteMenuItem(UUID menuItemId) {
        MenuItem menuItem = checkMenuItem(menuItemId);
        Menu menu = checkMenu(menuItem.getMenuId());
        checkRestaurantOwnerOrAdmin(menu);
        menuItemRepository.delete(menuItem);
        return menuItemId;
    }

    public MenuItemDto updateMenuItem(MenuItemDto menuItemDto, UUID menuItemId) {
        MenuItem menuItem = checkMenuItem(menuItemId);
        Menu menu = checkMenu(menuItem.getMenuId());
        checkRestaurantOwnerOrAdmin(menu);
        menuItem.setName(menuItemDto.getName());
        menuItem.setPrice(menuItemDto.getPrice());
        menuItem.setDescription(menuItemDto.getDescription());
        menuItemRepository.save(menuItem);
        return new MenuItemDto(menuItem.getId().toString(), menuItem.getMenuId().toString(), menuItem.getName(), menuItem.getPrice(), menuItem.getDescription());
    }

    public MenuItemDto getMenuItem(UUID menuItemId) {
        MenuItem mi = checkMenuItem(menuItemId);
        return new MenuItemDto(mi.getId().toString(), mi.getMenuId().toString(), mi.getName(), mi.getPrice(), mi.getDescription());
    }

    private Menu checkMenu(UUID menuId) {
        Optional<Menu> menu = menuRepository.findMenuById(menuId);
        if (menu.isEmpty()) {
            throw new IllegalArgumentException("Menu not found");
        }
        return menu.get();
    }

    private void checkRestaurantOwnerOrAdmin(Menu menu) {
        if (isAdmin()) {
            return;
        }

        Optional<Restaurant> restaurant = restaurantRepository.findRestaurantById(menu.getRestaurantId());
        if (restaurant.isEmpty()) {
            throw new IllegalArgumentException("Restaurant not found");
        }
        Optional<User> user = userRepository.findUserByEmail(SecurityContextHolder.getContext().getAuthentication().getName());
        if (user.isEmpty()) {
            throw new IllegalArgumentException("User not found");
        }
        if (!restaurant.get().getOwnerId().equals(user.get().getId())) {
            throw new IllegalArgumentException("User is not the owner of the restaurant");
        }
        user.get();
    }

    private boolean isAdmin() {
        return SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch("ADMIN"::equals);
    }

    private MenuItem checkMenuItem(UUID menuItemId) {
        Optional<MenuItem> menuItem = menuItemRepository.findMenuItemById(menuItemId);
        if (menuItem.isEmpty()) {
            throw new IllegalArgumentException("Menu item not found");
        }
        return menuItem.get();
    }

    public List<MenuItemDto> getMenuItemsByMenu(UUID menuId) {
        logger.info("Checking if menu exists for menu ID: {}", menuId);
        checkMenu(menuId);
        logger.info("Fetching menu items for menu ID: {}", menuId);
        Optional<List<MenuItem>> menuItems = menuItemRepository.findMenuItemsByMenuId(menuId);
        logger.info("Menu items: {}", menuItems);
        if (menuItems.isEmpty()) {
            throw new IllegalArgumentException("Menu items not found");
        }
        return menuItems.get().stream()
                .map(menuItem -> new MenuItemDto(menuItem.getId().toString(), menuItem.getMenuId().toString(), menuItem.getName(), menuItem.getPrice(), menuItem.getDescription()))
                .collect(Collectors.toList());
    }

    public Page<MenuItemDto> getMenuItemsByMenu(UUID menuId, Pageable pageable) {
        checkMenu(menuId);
        return menuItemRepository.findByMenuId(menuId, pageable)
                .map(menuItem -> new MenuItemDto(
                        menuItem.getId().toString(),
                        menuItem.getMenuId().toString(),
                        menuItem.getName(),
                        menuItem.getPrice(),
                        menuItem.getDescription()));
    }
}
