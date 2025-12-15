package com.example.youtube_recommender.controller;

import com.example.youtube_recommender.service.VideoIngestionService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ingestion")
public class IngestionController {

    private final VideoIngestionService ingestionService;

    public IngestionController(VideoIngestionService ingestionService) {
        this.ingestionService = ingestionService;
    }

    /**
     * Triggers the YouTube scraper + LLM Summarizer + Vectorizer.
     * Usage: POST /api/ingestion/trigger?topic=Spring+Boot&max=10
     */
    @PostMapping("/trigger")
    public String triggerIngestion(@RequestParam String topic, @RequestParam(defaultValue = "5") int max) {
        int count = ingestionService.ingestVideos(topic, max);
        return String.format("Successfully ingested %d videos about '%s'.", count, topic);
    }
}