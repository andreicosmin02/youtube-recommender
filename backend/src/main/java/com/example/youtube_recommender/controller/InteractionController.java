package com.example.youtube_recommender.controller;

import com.example.youtube_recommender.enums.InteractionAction;
import com.example.youtube_recommender.model.UserInteraction;
import com.example.youtube_recommender.service.RecommendationService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/interactions")
public class InteractionController {

    private final RecommendationService recommendationService;

    public InteractionController(RecommendationService recommendationService) {
        this.recommendationService = recommendationService;
    }

    @PostMapping
    public void recordInteraction(@RequestBody InteractionRequest request) {
        recommendationService.recordInteraction(request.userId(), request.videoId(), request.action());
    }

    @DeleteMapping
    public void deleteInteraction(@RequestParam Long userId, @RequestParam String videoId) {
        recommendationService.deleteInteraction(userId, videoId);
    }

    @GetMapping("/history")
    public List<UserInteraction> getUserHistory(@RequestParam Long userId) {
        return recommendationService.getUserHistory(userId, 50);
    }

    @GetMapping("/watch-later")
    public List<UserInteraction> getWatchLater(@RequestParam Long userId) {
        return recommendationService.getUserWatchLaterList(userId);
    }

    // Updated DTO with 'action' instead of 'type'
    public record InteractionRequest(Long userId, String videoId, InteractionAction action) {}
}