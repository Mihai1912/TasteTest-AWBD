package com.example.tastetestawdb.controller;

import com.example.tastetestawdb.integration.notification.NotificationGatewayService;
import com.example.tastetestawdb.integration.notification.NotificationResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/v1/integrations")
public class ServiceDiscoveryController implements SecuredRestController {

    private final NotificationGatewayService notificationGatewayService;

    public ServiceDiscoveryController(NotificationGatewayService notificationGatewayService) {
        this.notificationGatewayService = notificationGatewayService;
    }

    @PostMapping("/notifications/demo")
    @PreAuthorize("hasAuthority('USER') or hasAuthority('ADMIN') or hasAuthority('RESTAURANT_OWNER')")
    public ResponseEntity<NotificationResponse> sendDemoNotification(@Valid @RequestBody DemoNotificationRequest request) {
        return ResponseEntity.ok(notificationGatewayService.sendDemoNotification(request.getTitle(), request.getMessage()));
    }

    public static class DemoNotificationRequest {
        @NotBlank
        private String title;

        @NotBlank
        private String message;

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }
}
