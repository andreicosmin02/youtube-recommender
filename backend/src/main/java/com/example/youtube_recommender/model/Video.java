package com.example.youtube_recommender.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties; // <--- IMPORT THIS
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "youtube_videos")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
// --- FIX: Ignore Hibernate Proxy fields during JSON serialization ---
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Video {

    @Id
    @Column(name = "video_id")
    private String videoId;

    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "channel_name")
    private String channelName;

    @Column(name = "duration_seconds")
    private Integer durationSeconds;

    @Column(name = "view_count")
    private Long viewCount;

    @Column(name = "published_at")
    private LocalDateTime publishedAt;

    @Column(columnDefinition = "TEXT")
    private String tags;

    @Column(name = "thumbnail_url", columnDefinition = "TEXT")
    private String thumbnailUrl;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}