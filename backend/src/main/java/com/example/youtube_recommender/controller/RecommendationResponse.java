package com.example.youtube_recommender.controller;

import com.example.youtube_recommender.model.Video;
import java.util.List;

/**
 * Data Transfer Object to send both the AI text and the Video objects to the Frontend.
 */
public record RecommendationResponse(String aiResponse, List<Video> videos) {}