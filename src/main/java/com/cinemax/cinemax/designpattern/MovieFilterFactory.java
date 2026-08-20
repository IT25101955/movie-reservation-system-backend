package com.cinemax.cinemax.designpattern;

import com.cinemax.cinemax.model.MovieStatus;
import org.springframework.stereotype.Component;

// Spring creates one factory object that MovieService can use.
@Component
// This is the Factory class that creates the correct MovieFilter object.
public class MovieFilterFactory {

    // This method creates a filter object depending on the requested movie status.
    public MovieFilter create(MovieStatus status) {
        // Return the all-movies filter when no status is selected.
        if (status == null) {
            return new AllMoviesFilter();
        }

        // Create the now-showing filter when the status is NOW_SHOWING.
        if (status == MovieStatus.NOW_SHOWING) {
            return new NowShowingMovieFilter();
        }

        // Create the upcoming filter when the status is UPCOMING.
        if (status == MovieStatus.UPCOMING) {
            return new UpcomingMovieFilter();
        }

        // Create the ended filter when the status is ENDED.
        if (status == MovieStatus.ENDED) {
            return new EndedMovieFilter();
        }

        // Use the all-movies filter as a simple fallback.
        return new AllMoviesFilter();
    }
}
