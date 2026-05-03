package com.example.tastetestawdb.repository;

import com.example.tastetestawdb.entity.Reply;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ReplyRepository extends JpaRepository<Reply, UUID> {
    Optional<Reply> findReplyById(UUID id);
    List<Reply> findAllByReviewId(UUID reviewId);
}

