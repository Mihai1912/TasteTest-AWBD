package com.example.tastetestawdb.integration.notification;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URI;

@Service
public class NotificationGatewayService {

    private static final URI NOTIFICATION_SERVICE_URI = URI.create("http://tastetest-notification-service/api/notifications");

    private final RestTemplate restTemplate;

    public NotificationGatewayService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public NotificationResponse sendDemoNotification(String title, String message) {
        NotificationRequest request = new NotificationRequest(title, message, "tastetest-awdb");
        return restTemplate.postForObject(NOTIFICATION_SERVICE_URI, request, NotificationResponse.class);
    }
}
