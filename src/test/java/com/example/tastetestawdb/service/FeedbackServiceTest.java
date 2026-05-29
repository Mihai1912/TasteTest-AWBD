package com.example.tastetestawdb.service;

import com.example.tastetestawdb.entity.Feedback;
import com.example.tastetestawdb.repository.FeedbackRepository;
import com.example.tastetestawdb.service.dto.FeedbackAdminDto;
import com.example.tastetestawdb.service.dto.FeedbackDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class FeedbackServiceTest {

    @Mock
    private FeedbackRepository feedbackRepository;

    @InjectMocks
    private FeedbackService feedbackService;

    @Test
    void addFeedback_savesAndReturnsDto() {
        FeedbackDto dto = feedbackService.addFeedback("bug", "bad", "some details");

        assertEquals("bug", dto.getFeedbackType());
        assertEquals("bad", dto.getExperience());
        assertEquals("some details", dto.getComment());
        verify(feedbackRepository).save(any(Feedback.class));
    }

    @Test
    void getAllFeedback_returnsList() {
        Feedback f = new Feedback();
        f.setId(UUID.randomUUID());
        f.setFeedbackType("bug");
        f.setExperience("bad");
        f.setComment("c");
        when(feedbackRepository.findAll()).thenReturn(List.of(f));

        List<FeedbackAdminDto> all = feedbackService.getAllFeedback();

        assertEquals(1, all.size());
        assertEquals("bug", all.get(0).getFeedbackType());
    }
}
