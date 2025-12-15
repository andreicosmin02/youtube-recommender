package com.example.youtube_recommender.service;

import com.example.youtube_recommender.model.Video;
import com.example.youtube_recommender.model.VideoEmbedding;
import com.example.youtube_recommender.repository.VideoEmbeddingRepository;
import com.example.youtube_recommender.repository.VideoRepository;
import com.google.api.services.youtube.model.SearchResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration; // Important Import
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class VideoIngestionService {

    private static final Logger log = LoggerFactory.getLogger(VideoIngestionService.class);

    private final YouTubeService youTubeService;
    private final VideoRepository videoRepository;
    private final VideoEmbeddingRepository videoEmbeddingRepository;
    private final ChatModel chatModel;
    private final EmbeddingModel embeddingModel;

    public VideoIngestionService(YouTubeService youTubeService,
                                 VideoRepository videoRepository,
                                 VideoEmbeddingRepository videoEmbeddingRepository,
                                 ChatModel chatModel,
                                 EmbeddingModel embeddingModel) {
        this.youTubeService = youTubeService;
        this.videoRepository = videoRepository;
        this.videoEmbeddingRepository = videoEmbeddingRepository;
        this.chatModel = chatModel;
        this.embeddingModel = embeddingModel;
    }

    @Transactional
    public int ingestVideos(String topic, long maxResults) {
        log.info("Starting ingestion for topic: {}", topic);

        // 1. Search for Video IDs
        List<SearchResult> searchResults = youTubeService.searchVideos(topic, maxResults);
        List<String> videoIdsToFetch = new ArrayList<>();

        for (SearchResult searchResult : searchResults) {
            String videoId = searchResult.getId().getVideoId();
            // Filter duplicates early
            if (!videoRepository.existsById(videoId)) {
                videoIdsToFetch.add(videoId);
            }
        }

        if (videoIdsToFetch.isEmpty()) {
            return 0;
        }

        // 2. Fetch Full Details (ContentDetails, Statistics) from YouTube
        List<com.google.api.services.youtube.model.Video> fullVideos = youTubeService.getVideoDetails(videoIdsToFetch);
        int savedCount = 0;

        // 3. Process and Save
        for (com.google.api.services.youtube.model.Video googleVideo : fullVideos) {
            try {
                processAndSaveVideo(googleVideo);
                savedCount++;
            } catch (Exception e) {
                log.error("Failed to ingest video {}: {}", googleVideo.getId(), e.getMessage());
            }
        }

        log.info("Ingestion complete. Saved {} new videos.", savedCount);
        return savedCount;
    }

    private void processAndSaveVideo(com.google.api.services.youtube.model.Video googleVideo) {
        // 1. Convert Google Video to Our Entity
        Video video = new Video();
        video.setVideoId(googleVideo.getId());
        video.setTitle(googleVideo.getSnippet().getTitle());
        video.setDescription(googleVideo.getSnippet().getDescription());
        video.setChannelName(googleVideo.getSnippet().getChannelTitle());

        // --- NEW FIELDS MAPPING ---

        // Duration (ISO 8601 format like "PT1H" -> Seconds)
        if (googleVideo.getContentDetails() != null && googleVideo.getContentDetails().getDuration() != null) {
            try {
                Duration d = Duration.parse(googleVideo.getContentDetails().getDuration());
                video.setDurationSeconds((int) d.getSeconds());
            } catch (Exception e) {
                log.warn("Could not parse duration for video {}", video.getVideoId());
                video.setDurationSeconds(0);
            }
        }

        // View Count (BigInteger -> Long)
        if (googleVideo.getStatistics() != null && googleVideo.getStatistics().getViewCount() != null) {
            video.setViewCount(googleVideo.getStatistics().getViewCount().longValue());
        } else {
            video.setViewCount(0L);
        }

        // Tags (List<String> -> Comma Separated String)
        if (googleVideo.getSnippet().getTags() != null) {
            video.setTags(String.join(",", googleVideo.getSnippet().getTags()));
        }

        // Thumbnail
        if (googleVideo.getSnippet().getThumbnails() != null && googleVideo.getSnippet().getThumbnails().getHigh() != null) {
            video.setThumbnailUrl(googleVideo.getSnippet().getThumbnails().getHigh().getUrl());
        }

        // Published Date
        if (googleVideo.getSnippet().getPublishedAt() != null) {
            long epoch = googleVideo.getSnippet().getPublishedAt().getValue();
            video.setPublishedAt(LocalDateTime.ofInstant(Instant.ofEpochMilli(epoch), ZoneId.systemDefault()));
        }

        // Save Video Parent
        video = videoRepository.save(video);

        // 2. Generate Summary using LLM
        String rawDescription = video.getTitle() + "\n" + video.getDescription();
        String summaryPrompt = "Summarize the following YouTube video description in 2 sentences, focusing on the key topics taught: \n\n" + rawDescription;
        String summary = chatModel.call(summaryPrompt);

        // 3. Generate Embedding
        String textToEmbed = "Title: " + video.getTitle() + "\nTags: " + video.getTags() + "\nSummary: " + summary;
        float[] vector = embeddingModel.embed(textToEmbed);

        // 4. Save Embedding Child
        VideoEmbedding embedding = VideoEmbedding.builder()
                .video(video)
                .contentSummary(summary)
                .semanticEmbedding(vector)
                .build();

        videoEmbeddingRepository.save(embedding);
    }
}