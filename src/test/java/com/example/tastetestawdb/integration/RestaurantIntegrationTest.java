package com.example.tastetestawdb.integration;

import com.example.tastetestawdb.entity.Restaurant;
import com.example.tastetestawdb.entity.Role;
import com.example.tastetestawdb.entity.User;
import com.example.tastetestawdb.repository.MenuItemRepository;
import com.example.tastetestawdb.repository.MenuRepository;
import com.example.tastetestawdb.repository.ReplyRepository;
import com.example.tastetestawdb.repository.RestaurantRepository;
import com.example.tastetestawdb.repository.ReviewRepository;
import com.example.tastetestawdb.repository.RoleRepository;
import com.example.tastetestawdb.repository.UserRepository;
import com.example.tastetestawdb.service.dto.RestaurantDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Teste de integrare end-to-end (controller -> service -> repository -> H2)
 * rulate pe profilul "test" (baza H2 in-memory, schema generata din entitati).
 *
 * Acopera: citire paginata si nepaginata, creare cu persistenta reala,
 * validare server-side (400) si handler global pentru ruta inexistenta (404).
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class RestaurantIntegrationTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private RestaurantRepository restaurantRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private ReviewRepository reviewRepository;
    @Autowired
    private MenuRepository menuRepository;
    @Autowired
    private MenuItemRepository menuItemRepository;
    @Autowired
    private ReplyRepository replyRepository;

    private UUID ownerId;

    @BeforeEach
    void setUp() {
        // curatare in ordine copil -> parinte
        replyRepository.deleteAll();
        reviewRepository.deleteAll();
        menuItemRepository.deleteAll();
        menuRepository.deleteAll();
        restaurantRepository.deleteAll();
        userRepository.deleteAll();
        roleRepository.deleteAll();

        roleRepository.save(new Role().setName("USER"));
        roleRepository.save(new Role().setName("ADMIN"));
        Role ownerRole = roleRepository.save(new Role().setName("RESTAURANT_OWNER"));

        User owner = userRepository.save(new User()
                .setUsername("owner")
                .setEmail("owner@test.com")
                .setPassword("secret")
                .setRoles(List.of(ownerRole)));
        ownerId = owner.getId();

        restaurantRepository.save(buildRestaurant("Alpha"));
        restaurantRepository.save(buildRestaurant("Beta"));
    }

    private Restaurant buildRestaurant(String name) {
        Restaurant r = new Restaurant();
        r.setName(name);
        r.setAddress("Str. Exemplu 1");
        r.setPhone("0712345678");
        r.setWebsite("https://example.com");
        r.setSchedule("9-17");
        r.setOwnerId(ownerId);
        return r;
    }

    @Test
    void getAllRestaurants_returnsSeededData() throws Exception {
        mockMvc.perform(get("/api/v1/restaurant/getAll")
                        .with(user("owner@test.com").authorities(new SimpleGrantedAuthority("USER"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void getAllRestaurantsPaged_returnsPageMetadata() throws Exception {
        mockMvc.perform(get("/api/v1/restaurant/paged")
                        .param("page", "0")
                        .param("size", "1")
                        .with(user("owner@test.com").authorities(new SimpleGrantedAuthority("USER"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.totalElements").value(2));
    }

    @Test
    void addRestaurant_asOwner_isPersisted() throws Exception {
        RestaurantDto dto = new RestaurantDto(null, "Gamma", "Str. Noua 2",
                "0722333444", "https://gamma.ro", "10-22");

        mockMvc.perform(post("/api/v1/restaurant/add")
                        .with(user("owner@test.com").authorities(new SimpleGrantedAuthority("RESTAURANT_OWNER")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated());

        assertTrue(restaurantRepository.findRestaurantByName("Gamma").isPresent());
    }

    @Test
    void register_invalidBody_returnsBadRequest() throws Exception {
        String invalidPayload = "{\"email\":\"\",\"username\":\"\",\"password\":\"\"}";

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidPayload))
                .andExpect(status().isBadRequest());
    }

    @Test
    void unknownEndpoint_returnsNotFound() throws Exception {
        mockMvc.perform(get("/api/v1/aceasta-ruta-nu-exista")
                        .with(user("owner@test.com").authorities(new SimpleGrantedAuthority("USER"))))
                .andExpect(status().isNotFound());
    }
}
