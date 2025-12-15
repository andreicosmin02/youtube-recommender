package com.example.youtube_recommender.service;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.SearchListResponse;
import com.google.api.services.youtube.model.SearchResult;
import com.google.api.services.youtube.model.VideoListResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.List;

@Service
public class YouTubeService {

    @Value("${youtube.api.key}")
    private String apiKey;

    private static final String APPLICATION_NAME = "SpringAI-Recommender";
    private YouTube youtubeService;

    // Initialize service once
    private YouTube getService() throws GeneralSecurityException, IOException {
        if (youtubeService == null) {
            youtubeService = new YouTube.Builder(
                    GoogleNetHttpTransport.newTrustedTransport(),
                    GsonFactory.getDefaultInstance(),
                    null)
                    .setApplicationName(APPLICATION_NAME)
                    .build();
        }
        return youtubeService;
    }

    public List<SearchResult> searchVideos(String query, long maxResults) {
        try {
            YouTube.Search.List request = getService().search().list(Collections.singletonList("snippet"));
            SearchListResponse response = request.setKey(apiKey)
                    .setQ(query)
                    .setType(Collections.singletonList("video"))
                    .setMaxResults(maxResults)
                    .execute();
            return response.getItems();
        } catch (Exception e) {
            throw new RuntimeException("Failed to search YouTube", e);
        }
    }

    /**
     * NEW METHOD: Fetches full details (Statistics, ContentDetails) for a list of video IDs.
     */
    public List<com.google.api.services.youtube.model.Video> getVideoDetails(List<String> videoIds) {
        try {
            YouTube.Videos.List request = getService().videos()
                    .list(List.of("snippet", "contentDetails", "statistics"));

            VideoListResponse response = request.setKey(apiKey)
                    .setId(videoIds)
                    .execute();

            return response.getItems();
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch video details", e);
        }
    }
}