package com.example.tastetestawdb.service.dto;

public class FeedbackDto {
    private String feedbackType;
    private String experience;
    private String comment;

    public FeedbackDto(String feedbackType, String experience, String comment) {
        this.feedbackType = feedbackType;
        this.experience = experience;
        this.comment = comment;
    }

    public String getFeedbackType() {
        return feedbackType;
    }

    public FeedbackDto setFeedbackType(String feedbackType) {
        this.feedbackType = feedbackType;
        return this;
    }

    public String getExperience() {
        return experience;
    }

    public FeedbackDto setExperience(String experience) {
        this.experience = experience;
        return this;
    }

    public String getComment() {
        return comment;
    }

    public FeedbackDto setComment(String comment) {
        this.comment = comment;
        return this;
    }
}
