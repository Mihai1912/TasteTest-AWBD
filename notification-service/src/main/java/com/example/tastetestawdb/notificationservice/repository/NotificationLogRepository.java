package com.example.tastetestawdb.notificationservice.repository;

import com.example.tastetestawdb.notificationservice.model.NotificationLog;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface NotificationLogRepository extends MongoRepository<NotificationLog, String> {

    List<NotificationLog> findTop20ByOrderByProcessedAtDesc();
}
