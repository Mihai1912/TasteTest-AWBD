package com.example.tastetestawdb.service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class FeedbackDto {
    @NotBlank(message = "Tipul de feedback este obligatoriu")
    private String feedbackType;

    @NotBlank(message = "Experienta este obligatorie")
    private String experience;

    @NotBlank(message = "Comentariul este obligatoriu")
    @Size(max = 2000, message = "Comentariul poate avea maximum 2000 de caractere")
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
