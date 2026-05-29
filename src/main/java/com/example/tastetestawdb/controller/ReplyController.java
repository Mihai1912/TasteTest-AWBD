package com.example.tastetestawdb.controller;

import com.example.tastetestawdb.service.ReplyService;
import com.example.tastetestawdb.service.dto.ReplyDto;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/replies")
public class ReplyController implements SecuredRestController {
    public final ReplyService replyService;

    public ReplyController(ReplyService replyService) {
        this.replyService = replyService;
    }

    @RequestMapping(path = "/add/{reviewId}", method = RequestMethod.POST)
    @PreAuthorize("hasAuthority('RESTAURANT_OWNER') or hasAuthority('USER') or hasAuthority('ADMIN')")
    public ResponseEntity<ReplyDto> addReply(@Valid @RequestBody ReplyDto replyDto,
                                             @PathVariable UUID reviewId) {
        try {
            return ResponseEntity.status(201).body(replyService.addReply(replyDto, reviewId));
        } catch (Exception e) {
            return ResponseEntity.status(400).body(null);
        }
    }

    @RequestMapping(path = "/delete/{id}", method = RequestMethod.DELETE)
    @PreAuthorize("hasAuthority('RESTAURANT_OWNER') or hasAuthority('USER') or hasAuthority('ADMIN')")
    public ResponseEntity<UUID> deleteReply(@PathVariable UUID id) {
        try {
            return ResponseEntity.status(200).body(replyService.deleteReply(id));
        } catch (Exception e) {
            return ResponseEntity.status(400).body(null);
        }
    }

    @RequestMapping(path = "/update/{id}", method = RequestMethod.PUT)
    @PreAuthorize("hasAuthority('RESTAURANT_OWNER') or hasAuthority('USER') or hasAuthority('ADMIN')")
    public ResponseEntity<ReplyDto> updateReply(@PathVariable UUID id, @Valid @RequestBody ReplyDto replyDto) {
        try {
            return ResponseEntity.status(200).body(replyService.updateReply(id, replyDto));
        } catch (Exception e) {
            return ResponseEntity.status(400).body(null);
        }
    }

    @RequestMapping(path = "/get/{id}", method = RequestMethod.GET)
    @PreAuthorize("hasAuthority('RESTAURANT_OWNER') or hasAuthority('USER') or hasAuthority('ADMIN')")
    public ResponseEntity<ReplyDto> getReply(@PathVariable UUID id) {
        try {
            return ResponseEntity.status(200).body(replyService.getReply(id));
        } catch (Exception e) {
            return ResponseEntity.status(400).body(null);
        }
    }

    @RequestMapping(path = "/getAllofReview/{id}", method = RequestMethod.GET)
    @PreAuthorize("hasAuthority('RESTAURANT_OWNER') or hasAuthority('USER') or hasAuthority('ADMIN')")
    public ResponseEntity<List<ReplyDto>> getAllRepliesOfReview(@PathVariable UUID id) {
        try {
            return ResponseEntity.status(200).body(replyService.getAllRepliesOfReview(id));
        } catch (Exception e) {
            return ResponseEntity.status(400).body(null);
        }
    }

}
