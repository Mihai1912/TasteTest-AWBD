package com.example.tastetestawdb.integration.notification;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "tastetest-notification-service")
public interface NotificationServiceClient {

    @PostMapping("/api/notifications")
    NotificationResponse publish(@RequestBody NotificationRequest request);
}
