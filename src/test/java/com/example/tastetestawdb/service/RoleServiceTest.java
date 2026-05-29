package com.example.tastetestawdb.service;

import com.example.tastetestawdb.entity.Role;
import com.example.tastetestawdb.exception.BadRequestException;
import com.example.tastetestawdb.repository.RoleRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class RoleServiceTest {

    @Mock
    private RoleRepository roleRepository;

    @InjectMocks
    private RoleService roleService;

    @Test
    void addRoles_emptyList_throws() {
        assertThrows(BadRequestException.class, () -> roleService.addRoles(List.of()));
    }

    @Test
    void addRoles_newRole_isSaved() {
        when(roleRepository.findRoleByName("USER")).thenReturn(Optional.empty());

        List<String> result = roleService.addRoles(List.of("USER"));

        assertEquals(List.of("USER"), result);
        verify(roleRepository).save(any(Role.class));
    }

    @Test
    void getAllRoles_returnsNames() {
        when(roleRepository.findAll()).thenReturn(List.of(
                new Role().setName("ADMIN"),
                new Role().setName("USER")));

        assertEquals(List.of("ADMIN", "USER"), roleService.getAllRoles());
    }
}
