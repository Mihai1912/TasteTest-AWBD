package com.example.restaurant.saga;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Component
public class NotificationClient {

    private static final Logger log = LoggerFactory.getLogger(NotificationClient.class);
    private static final String NOTIFICATION_URL = "http://tastetest-notification-service/api/notifications";

    private final RestTemplate restTemplate;

    public NotificationClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public void send(String title, String message) {
        Map<String, String> body = Map.of(
                "title", title,
                "message", message,
                "sourceService", "restaurant-service"
        );
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        log.info("[SAGA] calling notification-service: title={}", title);
        restTemplate.postForEntity(NOTIFICATION_URL, new HttpEntity<>(body, headers), Void.class);
    }
}
