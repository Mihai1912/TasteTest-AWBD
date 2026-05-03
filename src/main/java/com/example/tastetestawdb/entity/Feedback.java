package com.example.tastetestawdb.entity;

import jakarta.persistence.*;

import java.util.UUID;

@Entity
@Table(name = "feedback", schema = "project")
public class Feedback {
    @Id
    @GeneratedValue(generator = "UUID")
    private UUID id;
    @Column (name = "feedback_type")
    private String feedbackType;
    @Column (name = "experience")
    private String experience;
    @Column (name = "comment")
    private String comment;

    public Feedback(UUID id, String feedbackType, String experience, String comment) {
        this.id = id;
        this.feedbackType = feedbackType;
        this.experience = experience;
        this.comment = comment;
    }

    public Feedback() {
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
