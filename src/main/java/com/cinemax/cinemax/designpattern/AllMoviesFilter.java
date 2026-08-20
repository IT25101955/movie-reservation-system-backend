package com.cinemax.cinemax.designpattern;

import com.cinemax.cinemax.model.Movie;

import java.util.List;

// This concrete Product keeps all active movies without filtering by status.
public class AllMoviesFilter implements MovieFilter {

    // This method returns the same movie list because no status filter is needed.
    @Override
    public List<Movie> filter(List<Movie> movies) {
        // Return all movies received by this filter.
        return movies;
    }
}
