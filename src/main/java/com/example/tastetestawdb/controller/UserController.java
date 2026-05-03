package com.example.tastetestawdb.controller;

import com.example.tastetestawdb.service.UserService;
import com.example.tastetestawdb.service.dto.ReviewDto;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/user")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @RequestMapping(path = "/{id}/reviews", method = RequestMethod.GET)
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<List<ReviewDto>> getUserReviews(@PathVariable UUID id) {
        try {
            return ResponseEntity.status(200).body(userService.getUserReviews(id));
        } catch (Exception e) {
            return ResponseEntity.status(400).body(null);
        }
    }
}
