package com.cinemax.cinemax.designpattern;

import com.cinemax.cinemax.model.Movie;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

// DESIGN PATTERN: Singleton - Dewmini - Theatre and Show Management.
// This class keeps one shared object for show-time calculation rules.
public class ShowTimePolicy {

    // This variable stores the one and only ShowTimePolicy object.
    private static ShowTimePolicy instance;

    // This constant stores the minimum gap required between two shows.
    private static final long MINIMUM_GAP_MINUTES = 60;

    // The private constructor prevents other classes from using new ShowTimePolicy().
    private ShowTimePolicy() {
    }

    // This method creates the object once and returns the same object later.
    public static ShowTimePolicy getInstance() {
        // Create the object only if it has not been created before.
        if (instance == null) {
            instance = new ShowTimePolicy();
        }

        // Return the same Singleton object every time.
        return instance;
    }

    // This method calculates the ending date and time of a movie show.
    public LocalDateTime calculateEndTime(
            Movie movie,
            LocalDate date,
            LocalTime startTime) {

        // Create the starting date and time of the show.
        LocalDateTime showStart = LocalDateTime.of(date, startTime);

        // Add the movie duration and return the ending date and time.
        return showStart.plusMinutes(movie.getDurationMinutes());
    }

    // This method calculates the earliest allowed start time for the next show.
    public LocalDateTime earliestNextStart(LocalDateTime currentEndTime) {
        // Add the required 60-minute gap to the current show's ending time.
        return currentEndTime.plusMinutes(MINIMUM_GAP_MINUTES);
    }

    // This method returns the minimum gap value for display or validation.
    public long getMinimumGapMinutes() {
        // Return the 60-minute gap value.
        return MINIMUM_GAP_MINUTES;
    }
}
