package com.example.user.repository;

import com.example.user.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleRepository  extends JpaRepository<Role, Integer> {

    Optional<Role> findRoleByName(String name);
}
