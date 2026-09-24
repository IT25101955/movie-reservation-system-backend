package com.cinemax.cinemax.service;

import com.cinemax.cinemax.model.Movie;
import com.cinemax.cinemax.repository.MovieRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

// Spring creates this service to handle business rules
@Service
public class MovieService {

    private final MovieRepository movieRepository;

    // Constructor injection supplies the dependencies this class needs
    public MovieService(MovieRepository movieRepository) {
        this.movieRepository = movieRepository;
    }


    // Load all records for admin management, including inactive records
    public List<Movie> getAllMovies() {
        return movieRepository.findAllByOrderByTitleAsc();
    }


    // Load only active records
    public List<Movie> getActiveMovies() {
        return movieRepository.findByActiveTrueOrderByTitleAsc();
    }


    // Run these database steps together; runtime failures roll them back
    @Transactional
    // Save changes while preserving the record's active status
    public Movie saveMovie(Movie movie) {

        if (movie.getId() != null) {
            Movie existing = getMovieById(movie.getId());
            movie.setActive(existing.isActive());
        }

        return movieRepository.save(movie);
    }


    // Search the database and report when the record is missing
    public Movie getMovieById(Long id) {

        // Search for the movie using its ID
        Optional<Movie> movie = movieRepository.findById(id);

        // Check whether the database search found a record
        if (movie.isPresent()) {
            return movie.get();
        }

        // Stop if the requested record does not exist or is inaccessible
        throw new IllegalArgumentException(
                "Movie not found with id: " + id
        );
    }


    // Load the record and reject it if it is unavailable
    public Movie getActiveMovieById(Long id) {

        Movie movie = getMovieById(id);

        if (!movie.isActive()) {
            throw new IllegalArgumentException(
                    "Movie is not currently available."
            );
        }

        return movie;
    }


    // Run these database steps together; runtime failures roll them back
    @Transactional
    // Deactivate the record while preserving existing booking history
    public void deleteMovie(Long id) {

        Movie movie = getMovieById(id);
        // Deactivate this record without removing it from the database
        movie.setActive(false);
        movieRepository.save(movie);
    }


    // Run these database steps together; runtime failures roll them back
    @Transactional
    // Reactivate the saved record
    public void restoreMovie(Long id) {

        Movie movie = getMovieById(id);
        movie.setActive(true);
        movieRepository.save(movie);
    }


    // Search all movies for admin management, including deleted movies
    public List<Movie> searchMovies(String keyword) {

        if (keyword == null || keyword.trim().isEmpty()) {
            return movieRepository.findAllByOrderByTitleAsc();
        }

        return movieRepository
                .findByTitleContainingIgnoreCaseOrderByTitleAsc(keyword.trim());
    }


    // Search active movies for the customer catalogue
    public List<Movie> getPublicMovies(String keyword) {

        if (keyword == null || keyword.trim().isEmpty()) {
            return movieRepository.findByActiveTrueOrderByTitleAsc();
        }

        return movieRepository
                .findByActiveTrueAndTitleContainingIgnoreCaseOrderByTitleAsc(
                        keyword.trim()
                );
    }
}
