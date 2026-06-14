package com.example.tastetestawdb.notificationservice.api;

import com.example.tastetestawdb.notificationservice.model.NotificationLog;
import com.example.tastetestawdb.notificationservice.repository.NotificationLogRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final String serviceName;
    private final String instanceId;
    private final NotificationLogRepository notificationLogRepository;

    public NotificationController(@Value("${spring.application.name}") String serviceName,
                                  @Value("${HOSTNAME:unknown}") String hostname,
                                  @Value("${server.port}") String serverPort,
                                  NotificationLogRepository notificationLogRepository) {
        this.serviceName = serviceName;
        this.instanceId = "unknown".equals(hostname) ? serviceName + ":" + serverPort : hostname + ":" + serverPort;
        this.notificationLogRepository = notificationLogRepository;
    }

    @PostMapping
    public ResponseEntity<NotificationResponse> publish(@Valid @RequestBody NotificationRequest request) {
        Instant processedAt = Instant.now();
        NotificationResponse response = new NotificationResponse(
                "DELIVERED",
                serviceName,
            instanceId,
                request.getTitle() + ": " + request.getMessage(),
                processedAt
        );

        notificationLogRepository.save(new NotificationLog(
                request.getTitle(),
                request.getMessage(),
                request.getSourceService(),
                response.getStatus(),
                serviceName,
                instanceId,
                processedAt
        ));

        return ResponseEntity.ok(response);
    }

    @GetMapping("/history")
    public ResponseEntity<List<NotificationLog>> history() {
        return ResponseEntity.ok(notificationLogRepository.findTop20ByOrderByProcessedAtDesc());
    }
}
