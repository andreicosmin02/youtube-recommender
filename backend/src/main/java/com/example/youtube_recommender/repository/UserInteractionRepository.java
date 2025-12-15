package com.example.youtube_recommender.repository;

import com.example.youtube_recommender.model.UserInteraction;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserInteractionRepository extends JpaRepository<UserInteraction, Long> {

    Optional<UserInteraction> findByUserUserIdAndVideoVideoId(Long userId, String videoId);

    // Get History (Sorted by last modified)
    @Query("SELECT ui FROM UserInteraction ui WHERE ui.user.userId = :userId ORDER BY ui.lastModified DESC")
    List<UserInteraction> findHistory(@Param("userId") Long userId, Pageable pageable);

    // Get Watch Later list (Using the new boolean field 'watchLater')
    @Query("SELECT ui FROM UserInteraction ui WHERE ui.user.userId = :userId AND ui.watchLater = true ORDER BY ui.lastModified DESC")
    List<UserInteraction> findWatchLater(@Param("userId") Long userId);

    void deleteByUserUserIdAndVideoVideoId(Long userId, String videoId);
}