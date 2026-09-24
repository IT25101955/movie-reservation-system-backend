package com.cinemax.cinemax.repository;

import com.cinemax.cinemax.model.Movie;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

// JpaRepository supplies CRUD methods; method names below define database searches
public interface MovieRepository extends JpaRepository<Movie, Long> {

    // Load all movies sorted by title
    List<Movie> findAllByOrderByTitleAsc();

    // Search movie titles without matching letter case
    List<Movie> findByTitleContainingIgnoreCaseOrderByTitleAsc(String title);

    // Load active movies sorted by title
    List<Movie> findByActiveTrueOrderByTitleAsc();

    // Search active movies by title without matching letter case
    List<Movie> findByActiveTrueAndTitleContainingIgnoreCaseOrderByTitleAsc(
            String title
    );

    // Count records whose active flag is true
    long countByActiveTrue();

    // Search movies by genre without matching letter case
    List<Movie> findByGenreContainingIgnoreCase(String genre);

}
