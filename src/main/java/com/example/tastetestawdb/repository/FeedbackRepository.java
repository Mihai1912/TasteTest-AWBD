package com.example.tastetestawdb.repository;

import com.example.tastetestawdb.entity.Feedback;
import org.springframework.data.repository.CrudRepository;

import java.util.UUID;

public interface FeedbackRepository extends CrudRepository<Feedback, UUID> {
}

