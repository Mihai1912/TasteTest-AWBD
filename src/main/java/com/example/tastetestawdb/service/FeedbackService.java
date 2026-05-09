package com.example.tastetestawdb.service;

import com.example.tastetestawdb.entity.Feedback;
import com.example.tastetestawdb.repository.FeedbackRepository;
import com.example.tastetestawdb.service.dto.FeedbackAdminDto;
import com.example.tastetestawdb.service.dto.FeedbackDto;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class FeedbackService {
    public final FeedbackRepository feedbackRepository;

    public FeedbackService(FeedbackRepository feedbackRepository) {
        this.feedbackRepository = feedbackRepository;
    }

    public FeedbackDto addFeedback(String feedbackType, String experience, String comment) {
        Feedback feedback = new Feedback();
        feedback.setFeedbackType(feedbackType);
        feedback.setExperience(experience);
        feedback.setComment(comment);
        feedbackRepository.save(feedback);
        return new FeedbackDto(feedback.getFeedbackType(), feedback.getExperience(), feedback.getComment());
    }

    public List<FeedbackAdminDto> getAllFeedback() {
        List<FeedbackAdminDto> feedbacks = new ArrayList<>();
        feedbackRepository.findAll().forEach(feedback ->
                feedbacks.add(new FeedbackAdminDto(
                        feedback.getId(),
                        feedback.getFeedbackType(),
                        feedback.getExperience(),
                        feedback.getComment())));
        return feedbacks;
    }
}
