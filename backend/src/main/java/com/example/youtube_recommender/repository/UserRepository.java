package com.example.youtube_recommender.repository;

import com.example.youtube_recommender.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    Optional<User> findByUsername(String username);

    // Helper to check existence before registration
    boolean existsByEmail(String email);
    boolean existsByUsername(String username);
}