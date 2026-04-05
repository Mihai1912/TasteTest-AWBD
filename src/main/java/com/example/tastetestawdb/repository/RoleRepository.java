package com.example.tastetestawdb.repository;

import com.example.tastetestawdb.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleRepository  extends JpaRepository<Role, Integer> {

    Optional<Role> findRoleByName(String name);
}
