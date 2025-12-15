package com.example.youtube_recommender.model;

import com.example.youtube_recommender.enums.LikeStatus;
import com.example.youtube_recommender.enums.WatchStatus;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_interactions",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "video_id"}))
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserInteraction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long interactionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "video_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Video video;

    // --- NEW SEPARATE FIELDS ---

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private LikeStatus likeStatus = LikeStatus.NONE; // NONE, LIKE, DISLIKE

    @Column(nullable = false)
    @Builder.Default
    private boolean watchLater = false; // true / false

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private WatchStatus watchStatus = WatchStatus.NOT_WATCHED; // NOT_WATCHED, PARTIAL, FULL

    @Column(nullable = false)
    @Builder.Default
    private boolean clicked = false; // true / false

    @UpdateTimestamp
    private LocalDateTime lastModified;
}