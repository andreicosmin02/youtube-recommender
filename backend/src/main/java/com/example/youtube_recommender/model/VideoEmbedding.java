package com.example.youtube_recommender.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;

@Entity
@Table(name = "video_embeddings")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VideoEmbedding {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "embedding_id")
    private Long embeddingId;

    @OneToOne
    @JoinColumn(name = "video_id", referencedColumnName = "video_id", nullable = false)
    private Video video;

    /**
     * Semantic vector generated from title + summary.
     * Used for similarity search against user preference or query.
     */
    @Column(name = "semantic_embedding", columnDefinition = "vector")
    @JdbcTypeCode(SqlTypes.VECTOR)
    private float[] semanticEmbedding;

    /**
     * LLM-generated summary (Llama 3.2).
     * Used for RAG context injection.
     */
    @Column(name = "content_summary", columnDefinition = "TEXT")
    private String contentSummary;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}