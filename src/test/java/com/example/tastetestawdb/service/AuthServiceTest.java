package com.example.tastetestawdb.service;

import com.example.tastetestawdb.config.security.JwtGenerator;
import com.example.tastetestawdb.entity.Role;
import com.example.tastetestawdb.entity.User;
import com.example.tastetestawdb.exception.BadRequestException;
import com.example.tastetestawdb.repository.RoleRepository;
import com.example.tastetestawdb.repository.UserRepository;
import com.example.tastetestawdb.service.dto.LoginDto;
import com.example.tastetestawdb.service.dto.RegisterDto;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AuthServiceTest {

    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private UserRepository userRepository;
    @Mock
    private RoleRepository roleRepository;
    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private JwtGenerator jwtGenerator;

    @InjectMocks
    private AuthService authService;

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void register_emailAlreadyUsed_throws() {
        RegisterDto dto = new RegisterDto().setEmail("a@test.com").setUsername("a").setPassword("123456");
        when(userRepository.existsUserByEmail("a@test.com")).thenReturn(true);

        assertThrows(BadRequestException.class, () -> authService.register(dto));
    }

    @Test
    void register_newUser_isSaved() {
        RegisterDto dto = new RegisterDto().setEmail("a@test.com").setUsername("a").setPassword("123456");
        when(userRepository.existsUserByEmail("a@test.com")).thenReturn(false);
        when(roleRepository.findRoleByName("USER")).thenReturn(Optional.of(new Role().setName("USER")));
        when(passwordEncoder.encode("123456")).thenReturn("ENCODED");

        authService.register(dto);

        verify(userRepository).save(any(User.class));
    }

    @Test
    void login_userNotFound_throws() {
        LoginDto dto = new LoginDto().setEmail("a@test.com").setPassword("x");
        when(userRepository.findUserByEmail("a@test.com")).thenReturn(Optional.empty());

        assertThrows(BadRequestException.class, () -> authService.login(dto));
    }

    @Test
    void login_success_returnsToken() {
        LoginDto dto = new LoginDto().setEmail("a@test.com").setPassword("secret");
        when(userRepository.findUserByEmail("a@test.com")).thenReturn(Optional.of(new User().setEmail("a@test.com")));
        Authentication auth = new UsernamePasswordAuthenticationToken("a@test.com", "secret");
        when(authenticationManager.authenticate(any())).thenReturn(auth);
        when(jwtGenerator.generateToken(any())).thenReturn("jwt-token");

        assertEquals("jwt-token", authService.login(dto));
    }
}
