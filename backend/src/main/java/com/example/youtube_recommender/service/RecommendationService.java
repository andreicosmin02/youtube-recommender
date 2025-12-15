package com.example.youtube_recommender.service;

import com.example.youtube_recommender.controller.RecommendationResponse;
import com.example.youtube_recommender.enums.InteractionAction;
import com.example.youtube_recommender.enums.LikeStatus;
import com.example.youtube_recommender.enums.WatchStatus;
import com.example.youtube_recommender.model.User;
import com.example.youtube_recommender.model.UserInteraction;
import com.example.youtube_recommender.model.Video;
import com.example.youtube_recommender.model.VideoEmbedding;
import com.example.youtube_recommender.repository.UserInteractionRepository;
import com.example.youtube_recommender.repository.UserRepository;
import com.example.youtube_recommender.repository.VideoEmbeddingRepository;
import com.example.youtube_recommender.repository.VideoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class RecommendationService {

    private static final Logger log = LoggerFactory.getLogger(RecommendationService.class);

    private final UserRepository userRepository;
    private final VideoRepository videoRepository;
    private final VideoEmbeddingRepository videoEmbeddingRepository;
    private final UserInteractionRepository interactionRepository;
    private final EmbeddingModel embeddingModel;
    private final ChatModel chatModel;

    private static final float ALPHA = 0.85f;

    public RecommendationService(UserRepository userRepository,
                                 VideoRepository videoRepository,
                                 VideoEmbeddingRepository videoEmbeddingRepository,
                                 UserInteractionRepository interactionRepository,
                                 EmbeddingModel embeddingModel,
                                 ChatModel chatModel) {
        this.userRepository = userRepository;
        this.videoRepository = videoRepository;
        this.videoEmbeddingRepository = videoEmbeddingRepository;
        this.interactionRepository = interactionRepository;
        this.embeddingModel = embeddingModel;
        this.chatModel = chatModel;
    }

    /**
     * Records an interaction by updating the specific state field
     * corresponding to the action, preserving other states.
     */
    @Transactional
    public void recordInteraction(Long userId, String videoId, InteractionAction action) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Video video = videoRepository.findById(videoId)
                .orElseThrow(() -> new RuntimeException("Video not found"));
        VideoEmbedding videoEmbedding = videoEmbeddingRepository.findByVideoVideoId(videoId)
                .orElseThrow(() -> new RuntimeException("Video has no embedding"));

        // 1. Get existing interaction or create default
        UserInteraction interaction = interactionRepository.findByUserUserIdAndVideoVideoId(userId, videoId)
                .orElse(UserInteraction.builder()
                        .user(user)
                        .video(video)
                        .likeStatus(LikeStatus.NONE)
                        .watchStatus(WatchStatus.NOT_WATCHED)
                        .watchLater(false)
                        .clicked(false)
                        .build());

        // 2. Update specific fields based on Action
        switch (action) {
            case TOGGLE_LIKE:
                // If already liked, remove like. Else set like (overwriting dislike).
                if (interaction.getLikeStatus() == LikeStatus.LIKE) {
                    interaction.setLikeStatus(LikeStatus.NONE);
                } else {
                    interaction.setLikeStatus(LikeStatus.LIKE);
                }
                break;

            case TOGGLE_DISLIKE:
                if (interaction.getLikeStatus() == LikeStatus.DISLIKE) {
                    interaction.setLikeStatus(LikeStatus.NONE);
                } else {
                    interaction.setLikeStatus(LikeStatus.DISLIKE);
                }
                break;

            case TOGGLE_WATCH_LATER:
                interaction.setWatchLater(!interaction.isWatchLater());
                break;

            case MARK_PARTIAL:
                interaction.setWatchStatus(WatchStatus.PARTIAL);
                // Implicitly implies the user clicked it
                interaction.setClicked(true);
                break;

            case MARK_FULL:
                interaction.setWatchStatus(WatchStatus.FULL);
                interaction.setClicked(true);
                break;

            case CLICK:
                interaction.setClicked(true);
                break;
        }

        interactionRepository.save(interaction);

        log.info("User {} on video {}: Like={}, Watch={}, WL={}, Clicked={}",
                userId, videoId, interaction.getLikeStatus(), interaction.getWatchStatus(),
                interaction.isWatchLater(), interaction.isClicked());

        // 3. Update User Preferences (Vector) based on the specific action weight
        updateUserPreference(user, videoEmbedding.getSemanticEmbedding(), getWeight(action));
    }

    @Transactional
    public void deleteInteraction(Long userId, String videoId) {
        interactionRepository.deleteByUserUserIdAndVideoVideoId(userId, videoId);
        log.info("Deleted interaction history for user {} on video {}", userId, videoId);
    }

    public List<UserInteraction> getUserHistory(Long userId, int limit) {
        // We only want to show interactions that have some meaningful activity
        // i.e., at least clicked, watched, liked, or watch later.
        // For now, we return everything sorted by date.
        return interactionRepository.findHistory(userId, PageRequest.of(0, limit));
    }

    public List<UserInteraction> getUserWatchLaterList(Long userId) {
        return interactionRepository.findWatchLater(userId);
    }

    // --- Recommendation Logic ---
    public RecommendationResponse getRecommendations(Long userId, String userQuery) {
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
        float[] queryVector = embeddingModel.embed(userQuery);
        float[] searchVector = (user.getPreferenceEmbedding() != null)
                ? combineVectors(queryVector, 0.7f, user.getPreferenceEmbedding(), 0.3f)
                : queryVector;

        List<VideoEmbedding> selected = videoEmbeddingRepository.findSimilarByVector(searchVector, 4);

        if (selected.isEmpty()) return new RecommendationResponse("No videos found.", new ArrayList<>());

        List<Video> videos = selected.stream().map(VideoEmbedding::getVideo).toList();

        String context = selected.stream()
                .map(m -> String.format("- Title: %s\n  Summary: %s", m.getVideo().getTitle(), m.getContentSummary()))
                .collect(Collectors.joining("\n\n"));

        String prompt = String.format("""
                Expert curator here. User wants: "%s"
                Selected videos:
                %s
                
                Write a short, engaging paragraph explaining WHY these fit. 
                Use **Bold** for titles. Do NOT list them again.
                """, userQuery, context);

        return new RecommendationResponse(chatModel.call(prompt), videos);
    }

    // --- Helpers ---
    private void updateUserPreference(User user, float[] videoVector, float weight) {
        float[] currentPref = user.getPreferenceEmbedding();
        if (currentPref == null) {
            user.setPreferenceEmbedding(scalarMultiply(videoVector, weight > 0 ? 1.0f : 0.5f));
        } else {
            float[] weightedVideo = scalarMultiply(videoVector, weight);
            float[] newPref = combineVectors(currentPref, ALPHA, weightedVideo, 1 - ALPHA);
            user.setPreferenceEmbedding(newPref);
        }
        userRepository.save(user);
    }

    private float getWeight(InteractionAction action) {
        return switch (action) {
            case TOGGLE_LIKE -> 1.0f;
            case MARK_FULL -> 0.8f;
            case MARK_PARTIAL, TOGGLE_WATCH_LATER -> 0.5f;
            case CLICK -> 0.2f;
            case TOGGLE_DISLIKE -> -0.5f;
        };
    }

    private float[] combineVectors(float[] vecA, float scalarA, float[] vecB, float scalarB) {
        int size = vecA.length;
        float[] result = new float[size];
        for (int i = 0; i < size; i++) {
            result[i] = (vecA[i] * scalarA) + (vecB[i] * scalarB);
        }
        return result;
    }

    private float[] scalarMultiply(float[] vec, float scalar) {
        float[] result = new float[vec.length];
        for (int i = 0; i < vec.length; i++) {
            result[i] = vec[i] * scalar;
        }
        return result;
    }
}