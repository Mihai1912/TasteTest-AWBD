package com.example.tastetestawdb.service;

import com.example.tastetestawdb.entity.Menu;
import com.example.tastetestawdb.entity.Restaurant;
import com.example.tastetestawdb.repository.MenuRepository;
import com.example.tastetestawdb.repository.RestaurantRepository;
import com.example.tastetestawdb.repository.UserRepository;
import com.example.tastetestawdb.service.dto.MenuDto;
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
class MenuServiceTest {

    @Mock
    private MenuRepository menuRepository;
    @Mock
    private RestaurantRepository restaurantRepository;
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private MenuService menuService;

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
    void addMenu_asAdmin_saves() {
        authenticateAsAdmin();
        UUID restId = UUID.randomUUID();
        Restaurant r = new Restaurant();
        r.setId(restId);
        r.setName("Pizza");
        when(restaurantRepository.findRestaurantByName("Pizza")).thenReturn(Optional.of(r));

        MenuDto dto = menuService.addMenu("Lunch", "Pizza");

        assertEquals("Lunch", dto.getName());
        verify(menuRepository).save(any(Menu.class));
    }

    @Test
    void getMenu_found_returnsDto() {
        UUID id = UUID.randomUUID();
        Menu m = new Menu();
        m.setId(id);
        m.setName("Lunch");
        when(menuRepository.findMenuById(id)).thenReturn(Optional.of(m));

        assertEquals("Lunch", menuService.getMenu(id).getName());
    }

    @Test
    void getMenu_notFound_throws() {
        UUID id = UUID.randomUUID();
        when(menuRepository.findMenuById(id)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> menuService.getMenu(id));
    }

    @Test
    void deleteMenu_asAdmin_deletes() {
        authenticateAsAdmin();
        UUID id = UUID.randomUUID();
        UUID restId = UUID.randomUUID();
        Menu m = new Menu();
        m.setId(id);
        m.setRestaurantId(restId);
        when(menuRepository.findMenuById(id)).thenReturn(Optional.of(m));
        Restaurant r = new Restaurant();
        r.setId(restId);
        when(restaurantRepository.findRestaurantById(restId)).thenReturn(Optional.of(r));

        UUID result = menuService.deleteMenu(id);

        assertEquals(id, result);
        verify(menuRepository).delete(m);
    }

    @Test
    void getRestaurantMenus_returnsList() {
        UUID restId = UUID.randomUUID();
        Menu m1 = new Menu();
        m1.setId(UUID.randomUUID());
        m1.setName("A");
        when(menuRepository.findMenuByRestaurantId(restId)).thenReturn(List.of(m1));

        assertEquals(1, menuService.getRestaurantMenus(restId).size());
    }
}
