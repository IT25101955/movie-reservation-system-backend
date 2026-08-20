package com.cinemax.cinemax.designpattern;

import com.cinemax.cinemax.model.Movie;
import com.cinemax.cinemax.model.MovieStatus;

import java.util.ArrayList;
import java.util.List;

// This concrete Product returns only movies that are now showing.
public class NowShowingMovieFilter implements MovieFilter {

    // This method filters the movie list using a simple for loop.
    @Override
    public List<Movie> filter(List<Movie> movies) {
        // Create a new list for matching movies.
        List<Movie> filteredMovies = new ArrayList<>();

        // Check each movie one by one.
        for (Movie movie : movies) {
            // Add the movie when its status is NOW_SHOWING.
            if (movie.getStatus() == MovieStatus.NOW_SHOWING) {
                filteredMovies.add(movie);
            }
        }

        // Return the filtered movie list.
        return filteredMovies;
    }
}
