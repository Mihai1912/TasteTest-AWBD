package com.example.tastetestawdb.notificationservice.api;

import java.time.Instant;

public class NotificationResponse {

    private final String status;
    private final String serviceName;
    private final String message;
    private final Instant processedAt;

    public NotificationResponse(String status, String serviceName, String message, Instant processedAt) {
        this.status = status;
        this.serviceName = serviceName;
        this.message = message;
        this.processedAt = processedAt;
    }

    public String getStatus() {
        return status;
    }

    public String getServiceName() {
        return serviceName;
    }

    public String getMessage() {
        return message;
    }

    public Instant getProcessedAt() {
        return processedAt;
    }
}
