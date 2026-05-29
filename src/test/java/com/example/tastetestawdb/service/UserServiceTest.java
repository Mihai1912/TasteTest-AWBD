package com.example.tastetestawdb.service;

import com.example.tastetestawdb.entity.Review;
import com.example.tastetestawdb.entity.Role;
import com.example.tastetestawdb.entity.User;
import com.example.tastetestawdb.repository.RoleRepository;
import com.example.tastetestawdb.repository.ReviewRepository;
import com.example.tastetestawdb.repository.UserRepository;
import com.example.tastetestawdb.service.dto.ReviewDto;
import com.example.tastetestawdb.service.dto.UserAdminDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private ReviewRepository reviewRepository;
    @Mock
    private RoleRepository roleRepository;

    @InjectMocks
    private UserService userService;

    @Test
    void getAllUsers_returnsList() {
        User u = new User().setId(UUID.randomUUID()).setUsername("john")
                .setEmail("j@test.com").setRoles(List.of(new Role().setName("USER")));
        when(userRepository.findAll()).thenReturn(List.of(u));

        List<UserAdminDto> all = userService.getAllUsers();

        assertEquals(1, all.size());
        assertEquals("john", all.get(0).getUsername());
        assertEquals(List.of("USER"), all.get(0).getRoles());
    }

    @Test
    void updateUserRoles_updatesAndReturnsDto() {
        UUID userId = UUID.randomUUID();
        User u = new User().setId(userId).setUsername("john").setEmail("j@test.com");
        when(userRepository.findUserById(userId)).thenReturn(Optional.of(u));
        when(roleRepository.findRoleByName("ADMIN")).thenReturn(Optional.of(new Role().setName("ADMIN")));

        UserAdminDto dto = userService.updateUserRoles(userId, List.of("ADMIN"));

        assertEquals(List.of("ADMIN"), dto.getRoles());
        verify(userRepository).save(u);
    }

    @Test
    void getUserReviews_noReviews_throws() {
        UUID id = UUID.randomUUID();
        when(reviewRepository.findAllByUserId(id)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> userService.getUserReviews(id));
    }

    @Test
    void getUserReviews_returnsList() {
        UUID id = UUID.randomUUID();
        Review r = new Review().setComment("nice place");
        when(reviewRepository.findAllByUserId(id)).thenReturn(Optional.of(List.of(r)));

        List<ReviewDto> result = userService.getUserReviews(id);

        assertEquals(1, result.size());
        assertEquals("nice place", result.get(0).getComment());
    }
}
