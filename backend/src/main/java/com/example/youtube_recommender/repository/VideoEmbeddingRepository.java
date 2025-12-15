package com.example.youtube_recommender.repository;

import com.example.youtube_recommender.model.VideoEmbedding;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VideoEmbeddingRepository extends JpaRepository<VideoEmbedding, Long> {

    /**
     * Finds the closest video embeddings to the given probe vector using Cosine Similarity.
     * * @param embedding The target vector (e.g., user preference or query vector).
     * @param limit     The maximum number of results to return (K-Nearest Neighbors).
     * @return List of VideoEmbedding entities sorted by similarity (closest first).
     */
    @Query(value = """
            SELECT * FROM video_embeddings\s
            ORDER BY semantic_embedding <=> cast(:embedding as vector)\s
            LIMIT :limit
           \s""", nativeQuery = true)
    List<VideoEmbedding> findSimilarByVector(@Param("embedding") float[] embedding, @Param("limit") int limit);
    Optional<VideoEmbedding> findByVideoVideoId(String videoId);
}