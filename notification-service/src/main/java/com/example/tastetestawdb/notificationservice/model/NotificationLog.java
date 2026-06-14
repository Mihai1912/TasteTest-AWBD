package com.example.tastetestawdb.notificationservice.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "notifications")
public class NotificationLog {

    @Id
    private String id;
    private String title;
    private String message;
    private String sourceService;
    private String status;
    private String serviceName;
    private String instanceId;
    private Instant processedAt;

    public NotificationLog() {
    }

    public NotificationLog(String title, String message, String sourceService, String status,
                            String serviceName, String instanceId, Instant processedAt) {
        this.title = title;
        this.message = message;
        this.sourceService = sourceService;
        this.status = status;
        this.serviceName = serviceName;
        this.instanceId = instanceId;
        this.processedAt = processedAt;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getInstanceId() {
        return instanceId;
    }

    public void setInstanceId(String instanceId) {
        this.instanceId = instanceId;
    }

    public Instant getProcessedAt() {
        return processedAt;
    }

    public void setProcessedAt(Instant processedAt) {
        this.processedAt = processedAt;
    }
}
