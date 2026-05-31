package com.example.user.service;

import com.example.user.entity.Role;
import com.example.user.entity.Review;
import com.example.user.entity.User;
import com.example.user.repository.RoleRepository;
import com.example.user.repository.ReviewRepository;
import com.example.user.repository.UserRepository;
import com.example.user.service.dto.UserAdminDto;
import com.example.user.service.dto.ReviewDto;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class UserService {
    private final UserRepository userRepository;
    private final ReviewRepository reviewRepository;
    private final RoleRepository roleRepository;

    public UserService(UserRepository userRepository, ReviewRepository reviewRepository, RoleRepository roleRepository) {
        this.userRepository = userRepository;
        this.reviewRepository = reviewRepository;
        this.roleRepository = roleRepository;
    }

    public List<UserAdminDto> getAllUsers() {
        return userRepository.findAll().stream().map(this::toAdminDto).toList();
    }

    public UserAdminDto updateUserRoles(UUID userId, List<String> roleNames) {
        User user = checkUser(userId);
        List<Role> roles = resolveRoles(roleNames);
        user.setRoles(roles);
        userRepository.save(user);
        return toAdminDto(user);
    }

    public List<ReviewDto> getUserReviews(UUID id) {
        Optional<List<Review>> reviews = reviewRepository.findAllByUserId(id);
        if (reviews.isEmpty()){
            throw new RuntimeException("No reviews found for user with id: " + id);
        }
        List<ReviewDto> reviewDtos = new ArrayList<>();
        for (Review review: reviews.get()) {
            ReviewDto reviewDto = new ReviewDto();
            reviewDto.setComment(review.getComment());
            reviewDtos.add(reviewDto);
        }
        return reviewDtos;
    }

    private User checkUser(UUID id) {
        Optional<User> user = userRepository.findUserById(id);
        if (user.isEmpty()) {
            throw new RuntimeException("User not found");
        }
        return user.get();
    }

    private List<Role> resolveRoles(List<String> roleNames) {
        if (roleNames == null) {
            return new ArrayList<>();
        }

        return roleNames.stream()
                .map(String::trim)
                .filter(name -> !name.isEmpty())
                .map(name -> roleRepository.findRoleByName(name)
                        .orElseThrow(() -> new RuntimeException("Role not found: " + name)))
                .collect(Collectors.toCollection(ArrayList::new));
    }

    private UserAdminDto toAdminDto(User user) {
        List<String> roles = user.getRoles() == null
                ? new ArrayList<>()
                : user.getRoles().stream().map(Role::getName).toList();
        return new UserAdminDto(user.getId(), user.getUsername(), user.getEmail(), roles);
    }
}
