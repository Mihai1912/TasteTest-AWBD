package com.example.tastetestawdb.controller;

import com.example.tastetestawdb.service.FeedbackService;
import com.example.tastetestawdb.service.dto.FeedbackAdminDto;
import com.example.tastetestawdb.service.dto.FeedbackDto;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/feedback")
public class FeedbackController implements SecuredRestController{
    public final FeedbackService feedbackService;

    public FeedbackController(FeedbackService feedbackService) {
        this.feedbackService = feedbackService;
    }

    @RequestMapping(path = "/add", method = RequestMethod.POST)
    @PreAuthorize("hasAuthority('USER') or hasAuthority('ADMIN') or hasAuthority('RESTAURANT_OWNER')")
    public ResponseEntity<FeedbackDto> addFeedback(@RequestBody FeedbackDto feedbackDto) {
        try {
            return ResponseEntity.status(201).body(feedbackService.addFeedback(feedbackDto.getFeedbackType(), feedbackDto.getExperience(), feedbackDto.getComment()));
        } catch (Exception e) {
            return ResponseEntity.status(400).body(null);
        }
    }

    @RequestMapping(path = "/all", method = RequestMethod.GET)
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<java.util.List<FeedbackAdminDto>> getAllFeedback() {
        try {
            return ResponseEntity.status(200).body(feedbackService.getAllFeedback());
        } catch (Exception e) {
            return ResponseEntity.status(400).body(null);
        }
    }
}
