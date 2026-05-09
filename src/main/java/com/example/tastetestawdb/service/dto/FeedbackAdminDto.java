package com.example.tastetestawdb.service.dto;

import java.util.UUID;

public class FeedbackAdminDto {
    private UUID id;
    private String feedbackType;
    private String experience;
    private String comment;

    public FeedbackAdminDto() {
    }

    public FeedbackAdminDto(UUID id, String feedbackType, String experience, String comment) {
        this.id = id;
        this.feedbackType = feedbackType;
        this.experience = experience;
        this.comment = comment;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getFeedbackType() {
        return feedbackType;
    }

    public void setFeedbackType(String feedbackType) {
        this.feedbackType = feedbackType;
    }

    public String getExperience() {
        return experience;
    }

    public void setExperience(String experience) {
        this.experience = experience;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
}

