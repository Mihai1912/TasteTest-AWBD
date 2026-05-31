package com.example.user.repository;

import com.example.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
    Boolean existsUserByEmail(String email);
    Optional<User> findUserByEmail(String email);
    Optional<User> findUserById(UUID id);
}
