package com.example.youtube_recommender.repository;

import com.example.youtube_recommender.model.Video;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VideoRepository extends JpaRepository<Video, String> {
    // Basic CRUD is handled by JpaRepository.
    // videoId is a String (e.g., "dQw4w9WgXcQ"), so the ID type is String.
}