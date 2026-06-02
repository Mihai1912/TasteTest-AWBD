package com.example.tastetestawdb.integration.notification;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.time.Instant;

@Service
public class NotificationGatewayService {

    private static final URI NOTIFICATION_SERVICE_URI = URI.create("http://tastetest-notification-service/api/notifications");

    private final RestTemplate restTemplate;

    public NotificationGatewayService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @CircuitBreaker(name = "notificationService", fallbackMethod = "notificationFallback")
    @Retry(name = "notificationService")
    public NotificationResponse sendDemoNotification(String title, String message) {
        NotificationRequest request = new NotificationRequest(title, message, "tastetest-awdb");
        return restTemplate.postForObject(NOTIFICATION_SERVICE_URI, request, NotificationResponse.class);
    }

    public NotificationResponse notificationFallback(String title, String message, Throwable t) {
        NotificationResponse resp = new NotificationResponse();
        resp.setStatus("FAILED");
        resp.setServiceName("notification-gateway-fallback");
        resp.setInstanceId("fallback");
        resp.setMessage("Fallback response: " + t.getMessage());
        resp.setProcessedAt(Instant.now());
        return resp;
    }
}
