package com.example.tastetestawdb.integration.notification;

import jakarta.validation.constraints.NotBlank;

public class NotificationRequest {

    @NotBlank
    private String title;

    @NotBlank
    private String message;

    @NotBlank
    private String sourceService;

    public NotificationRequest() {
    }

    public NotificationRequest(String title, String message, String sourceService) {
        this.title = title;
        this.message = message;
        this.sourceService = sourceService;
    }

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

    public String getSourceService() {
        return sourceService;
    }

    public void setSourceService(String sourceService) {
        this.sourceService = sourceService;
    }
}
