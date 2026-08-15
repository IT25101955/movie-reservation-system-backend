package com.cinemax.cinemax.repository;

import com.cinemax.cinemax.model.Review;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

// JpaRepository supplies CRUD methods; method names below define database searches
public interface ReviewRepository
        extends JpaRepository<Review, Long> {

    void deleteByMovie_Id(Long movieId);


    List<Review> findByMovie_IdOrderByCreatedAtDesc(Long movieId);

    List<Review> findByUser_IdOrderByCreatedAtDesc(Long userId);


    // Load all records with the newest first
    List<Review>
        findAllByOrderByCreatedAtDesc();
}
