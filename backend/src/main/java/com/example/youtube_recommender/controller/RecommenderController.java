package com.example.youtube_recommender.controller;

import com.example.youtube_recommender.enums.InteractionAction;
import com.example.youtube_recommender.model.User;
import com.example.youtube_recommender.repository.UserRepository;
import com.example.youtube_recommender.service.RecommendationService;
import com.example.youtube_recommender.service.VideoIngestionService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class RecommenderController {

    private final VideoIngestionService ingestionService;
    private final RecommendationService recommendationService;
    private final UserRepository userRepository;

    public RecommenderController(VideoIngestionService ingestionService,
                                 RecommendationService recommendationService,
                                 UserRepository userRepository) {
        this.ingestionService = ingestionService;
        this.recommendationService = recommendationService;
        this.userRepository = userRepository;
    }

    /**
     * 1. Ingest Videos
     * Example: POST /api/ingest?topic=Spring%20Boot&max=5
     */
    @PostMapping("/ingest")
    public String ingestVideos(@RequestParam String topic, @RequestParam(defaultValue = "5") int max) {
        int count = ingestionService.ingestVideos(topic, max);
        return "Successfully ingested " + count + " videos about: " + topic;
    }

    /**
     * 2. Create a Dummy User (Helper for testing)
     * Example: POST /api/user/create?username=alex
     */
    @PostMapping("/user/create")
    public User createUser(@RequestParam String username) {
        // Check if exists
        return userRepository.findByUsername(username)
                .orElseGet(() -> userRepository.save(User.builder()
                        .username(username)
                        .email(username + "@example.com")
                        .build()));
    }

    /**
     * 3. Simulate Interaction (Click/Watch/Like)
     * Example: POST /api/interact?userId=1&videoId=...&type=LIKE
     */
    @PostMapping("/interact")
    public String interact(@RequestParam Long userId,
                           @RequestParam String videoId,
                           @RequestParam InteractionAction type) {
        recommendationService.recordInteraction(userId, videoId, type);
        return "Recorded " + type + " for video " + videoId;
    }

    /**
     * 4. Get Recommendations (The RAG Magic)
     * Example: GET /api/recommend?userId=1&query=I want to learn java
     */
    @GetMapping("/recommend")
    public Map<String, String> getRecommendations(@RequestParam Long userId,
                                                  @RequestParam String query) {
        String response = String.valueOf(recommendationService.getRecommendations(userId, query));
        return Map.of("query", query, "recommendation", response);
    }
}