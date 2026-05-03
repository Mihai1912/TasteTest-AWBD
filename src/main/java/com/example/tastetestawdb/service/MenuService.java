package com.example.tastetestawdb.service;

import com.example.tastetestawdb.entity.Menu;
import com.example.tastetestawdb.repository.MenuRepository;
import com.example.tastetestawdb.repository.RestaurantRepository;
import com.example.tastetestawdb.repository.UserRepository;
import com.example.tastetestawdb.entity.Restaurant;
import com.example.tastetestawdb.entity.User;
import com.example.tastetestawdb.service.dto.MenuDto;

import jakarta.transaction.Transactional;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class MenuService {
    public final MenuRepository menuRepository;
    public final RestaurantRepository restaurantRepository;
    public final UserRepository userRepository;

    public MenuService(MenuRepository menuRepository,
                       RestaurantRepository restaurantRepository,
                       UserRepository userRepository) {
        this.menuRepository = menuRepository;
        this.restaurantRepository = restaurantRepository;
        this.userRepository = userRepository;
    }

    public MenuDto addMenu(String name, String restaurantName) {
        Restaurant restaurant = checkRestaurant(restaurantName);
        checkRestaurantOwner(restaurantName);
        Menu menu = new Menu();
        menu.setName(name);
        menu.setRestaurantId(restaurant.getId());
        menuRepository.save(menu);
        return new MenuDto(menu.getName(), menu.getId());
    }

    public UUID deleteMenu(UUID id) {
        Menu menu = checkMenu(id);
        menuRepository.delete(menu);
        return id;
    }

    public MenuDto updateMenu(UUID id, String name) {
        Menu menu = checkMenu(id);
        menu.setName(name);
        menuRepository.save(menu);
        return new MenuDto(menu.getName(), menu.getId());
    }

    public MenuDto getMenu(UUID id) {
        return new MenuDto(checkMenu(id).getName(), id);
    }

    private void checkRestaurantOwner(String restaurantName) {
        Optional<User> user = userRepository.findUserByEmail(SecurityContextHolder.getContext().getAuthentication().getName());
        if (user.isEmpty()) {
            throw new RuntimeException("User not found");
        }
        Restaurant restaurant = checkRestaurant(restaurantName);

        if (!restaurant.getOwnerId().equals(user.get().getId())) {
            throw new RuntimeException("User is not the owner of the restaurant");
        }
    }

    private Restaurant checkRestaurant(String restaurantName) {
        Optional<Restaurant> restaurant = restaurantRepository.findRestaurantByName(restaurantName);
        if (restaurant.isEmpty()) {
            throw new RuntimeException("Restaurant not found");
        }
        return restaurant.get();
    }

    private Menu checkMenu(UUID id) {
        Optional<Menu> menu = menuRepository.findMenuById(id);
        if (menu.isEmpty()) {
            throw new RuntimeException("Menu not found");
        }
        return menu.get();
    }

    public List<MenuDto> getRestaurantMenus(UUID restaurantId) {
        List<Menu> menus = menuRepository.findMenuByRestaurantId(restaurantId);
        return menus.stream()
                .map(menu -> new MenuDto(menu.getName(), menu.getId()))
                .collect(Collectors.toList());
    }
}
