package com.example.tastetestawdb.integration.notification;

import org.springframework.stereotype.Service;

@Service
public class NotificationGatewayService {

    private final NotificationServiceClient notificationServiceClient;

    public NotificationGatewayService(NotificationServiceClient notificationServiceClient) {
        this.notificationServiceClient = notificationServiceClient;
    }

    public NotificationResponse sendDemoNotification(String title, String message) {
        NotificationRequest request = new NotificationRequest(title, message, "tastetest-awdb");
        return notificationServiceClient.publish(request);
    }
}
