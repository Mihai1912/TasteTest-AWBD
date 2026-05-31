package com.example.auth.config;

import com.example.auth.entity.Role;
import com.example.auth.entity.User;
import com.example.auth.repository.RoleRepository;
import com.example.auth.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
@Profile("!test")
public class ApplicationInitializer implements CommandLineRunner {

    private final BCryptPasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    @Value("${admin.username}")
    private String adminUsername;
    @Value("${admin.password}")
    private String adminPassword;
    @Value("${admin.email}")
    private String adminEmail;

    public ApplicationInitializer(UserRepository userRepository, RoleRepository roleRepository) {
        this.passwordEncoder = new BCryptPasswordEncoder();
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        if (roleRepository.findRoleByName("ADMIN").isEmpty()) {
            Role admin = new Role();
            admin.setName("ADMIN");
            roleRepository.save(admin);
        }
        if (roleRepository.findRoleByName("USER").isEmpty()) {
            Role user = new Role();
            user.setName("USER");
            roleRepository.save(user);
        }

        Optional<User> admin = userRepository.findUserByEmail(adminEmail);
        if (admin.isEmpty()) {
            List<Role> roleList = new ArrayList<>();
            Role adminRole = roleRepository.findRoleByName("ADMIN").get();
            roleList.add(adminRole);
            User initAdmin = new User()
                    .setUsername(adminUsername)
                    .setEmail(adminEmail)
                    .setPassword(passwordEncoder.encode(adminPassword))
                    .setRoles(roleList);
            userRepository.save(initAdmin);
        }
    }
}
