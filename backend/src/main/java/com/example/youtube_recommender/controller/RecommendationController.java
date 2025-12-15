package com.example.youtube_recommender.controller;

import com.example.youtube_recommender.service.RecommendationService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/recommendations")
public class RecommendationController {

    private final RecommendationService recommendationService;

    public RecommendationController(RecommendationService recommendationService) {
        this.recommendationService = recommendationService;
    }

    /**
     * Hybrid RAG Search.
     * Usage: GET /api/recommendations/ask?userId=1&query=I want to learn java
     *
     * Logic:
     * 1. Retrieve user preference vector.
     * 2. Combine with query vector (0.7 / 0.3 split).
     * 3. Find top videos in DB.
     * 4. LLM generates a personalized response.
     * 5. Return both the response and the actual Video objects.
     */
    @GetMapping("/ask")
    public RecommendationResponse askForRecommendation(@RequestParam Long userId,
                                                       @RequestParam String query) {
        // Now returns the structured DTO containing text + video list
        return recommendationService.getRecommendations(userId, query);
    }
}