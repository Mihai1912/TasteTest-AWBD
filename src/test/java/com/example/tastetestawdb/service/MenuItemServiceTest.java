package com.example.tastetestawdb.service;

import com.example.tastetestawdb.entity.Menu;
import com.example.tastetestawdb.entity.MenuItem;
import com.example.tastetestawdb.repository.MenuItemRepository;
import com.example.tastetestawdb.repository.MenuRepository;
import com.example.tastetestawdb.repository.RestaurantRepository;
import com.example.tastetestawdb.repository.UserRepository;
import com.example.tastetestawdb.service.dto.MenuItemDto;
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

import java.math.BigDecimal;
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
class MenuItemServiceTest {

    @Mock
    private MenuItemRepository menuItemRepository;
    @Mock
    private MenuRepository menuRepository;
    @Mock
    private RestaurantRepository restaurantRepository;
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private MenuItemService menuItemService;

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private void authenticateAsAdmin() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("admin@test.com", null,
                        List.of(new SimpleGrantedAuthority("ADMIN"))));
    }

    @Test
    void addMenuItem_asAdmin_saves() {
        authenticateAsAdmin();
        UUID menuId = UUID.randomUUID();
        Menu menu = new Menu();
        menu.setId(menuId);
        menu.setRestaurantId(UUID.randomUUID());
        when(menuRepository.findMenuById(menuId)).thenReturn(Optional.of(menu));
        when(menuItemRepository.save(any(MenuItem.class))).thenAnswer(invocation -> {
            MenuItem mi = invocation.getArgument(0);
            mi.setId(UUID.randomUUID());
            return mi;
        });

        MenuItemDto input = new MenuItemDto("Soup", new BigDecimal("12.50"), "hot soup");
        MenuItemDto result = menuItemService.addMenuItem(input, menuId);

        assertEquals("Soup", result.getName());
        assertEquals(new BigDecimal("12.50"), result.getPrice());
        verify(menuItemRepository).save(any(MenuItem.class));
    }

    @Test
    void getMenuItem_found_returnsDto() {
        UUID id = UUID.randomUUID();
        MenuItem mi = new MenuItem();
        mi.setId(id);
        mi.setMenuId(UUID.randomUUID());
        mi.setName("Soup");
        mi.setPrice(new BigDecimal("10.0"));
        when(menuItemRepository.findMenuItemById(id)).thenReturn(Optional.of(mi));

        assertEquals("Soup", menuItemService.getMenuItem(id).getName());
    }

    @Test
    void getMenuItem_notFound_throws() {
        UUID id = UUID.randomUUID();
        when(menuItemRepository.findMenuItemById(id)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> menuItemService.getMenuItem(id));
    }

    @Test
    void updateMenuItem_asAdmin_updates() {
        authenticateAsAdmin();
        UUID id = UUID.randomUUID();
        UUID menuId = UUID.randomUUID();
        MenuItem existing = new MenuItem();
        existing.setId(id);
        existing.setMenuId(menuId);
        existing.setName("Old");
        existing.setPrice(new BigDecimal("5.0"));
        Menu menu = new Menu();
        menu.setId(menuId);
        menu.setRestaurantId(UUID.randomUUID());

        when(menuItemRepository.findMenuItemById(id)).thenReturn(Optional.of(existing));
        when(menuRepository.findMenuById(menuId)).thenReturn(Optional.of(menu));

        MenuItemDto input = new MenuItemDto("New", new BigDecimal("9.0"), "desc");
        MenuItemDto result = menuItemService.updateMenuItem(input, id);

        assertEquals("New", result.getName());
        assertEquals(new BigDecimal("9.0"), result.getPrice());
        verify(menuItemRepository).save(existing);
    }
}
