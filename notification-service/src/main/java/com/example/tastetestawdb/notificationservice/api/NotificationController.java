package com.example.tastetestawdb.notificationservice.api;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final String serviceName;

    public NotificationController(@Value("${spring.application.name}") String serviceName) {
        this.serviceName = serviceName;
    }

    @PostMapping
    public ResponseEntity<NotificationResponse> publish(@Valid @RequestBody NotificationRequest request) {
        NotificationResponse response = new NotificationResponse(
                "DELIVERED",
                serviceName,
                request.getTitle() + ": " + request.getMessage(),
                Instant.now()
        );
        return ResponseEntity.ok(response);
    }
}
