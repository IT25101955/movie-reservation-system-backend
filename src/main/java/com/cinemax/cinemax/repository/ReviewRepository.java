package com.cinemax.cinemax.repository;

import com.cinemax.cinemax.model.Review;
import com.cinemax.cinemax.model.ReviewStatus;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

// JpaRepository supplies CRUD methods; method names below define database searches
public interface ReviewRepository
        extends JpaRepository<Review, Long> {


    // Load this movie's reviews with the selected moderation status
    List<Review>
        findByMovie_IdAndStatusOrderByCreatedAtDesc(
            Long movieId,
            ReviewStatus status
        );


    // Load all records with the newest first
    List<Review>
        findAllByOrderByCreatedAtDesc();
}
