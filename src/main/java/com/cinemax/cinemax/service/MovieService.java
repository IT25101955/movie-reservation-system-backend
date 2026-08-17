package com.cinemax.cinemax.service;

import com.cinemax.cinemax.designpattern.MovieFilter;
import com.cinemax.cinemax.designpattern.MovieFilterFactory;
import com.cinemax.cinemax.model.MovieStatus;
import com.cinemax.cinemax.model.Movie;
import com.cinemax.cinemax.repository.MovieRepository;
import com.cinemax.cinemax.repository.ReviewRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

// Spring creates this service to handle business rules
@Service
public class MovieService {

    private final MovieRepository movieRepository;
    private final ShowScheduleService showScheduleService;
    private final ReviewRepository reviewRepository;

    // DESIGN PATTERN: Factory creates the correct movie-filter product
    private final MovieFilterFactory movieFilterFactory;

    // ============================================================
    // ENTITY MANAGER
    // ============================================================
    //
    // Used for the permanent movie deletion process.
    //
    // We use native SQL here because several different tables
    // contain foreign keys connected to Movie -> Show -> Booking.
    //
    // Deleting them explicitly in the correct order prevents
    // MySQL foreign-key errors.
    //
    @PersistenceContext
    private EntityManager entityManager;

    // Constructor injection supplies the dependencies this class needs
    public MovieService(
            MovieRepository movieRepository,
            ShowScheduleService showScheduleService,
            ReviewRepository reviewRepository,
            MovieFilterFactory movieFilterFactory) {
        this.movieRepository = movieRepository;
        this.showScheduleService = showScheduleService;
        this.reviewRepository = reviewRepository;
        this.movieFilterFactory = movieFilterFactory;
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
    public Movie saveMovie(Movie movie) {

        if (movie.getTitle() == null
                || movie.getTitle().trim().isEmpty()) {

            throw new IllegalArgumentException(
                    "Movie title is required."
            );
        }


        if (movie.getStatus() == null) {

            throw new IllegalArgumentException(
                    "Movie status is required."
            );
        }


        String cleanTitle =
                movie.getTitle().trim();

        movie.setTitle(cleanTitle);


        /*
         * ADD NEW MOVIE
         */
        if (movie.getId() == null) {

            boolean duplicate =
                    movieRepository
                            .existsByTitleIgnoreCase(
                                    cleanTitle
                            );

            if (duplicate) {

                throw new IllegalArgumentException(
                        "Movie already exists with title: "
                                + cleanTitle
                );
            }

            /*
             * New movie should be active.
             */
            movie.setActive(true);
        }


        /*
         * UPDATE EXISTING MOVIE
         */
        else {

            boolean duplicate =
                    movieRepository
                            .existsByTitleIgnoreCaseAndIdNot(
                                    cleanTitle,
                                    movie.getId()
                            );


            if (duplicate) {

                throw new IllegalArgumentException(
                        "Another movie already exists with title: "
                                + cleanTitle
                );
            }


            Movie existing =
                    getMovieById(
                            movie.getId()
                    );


            /*
             * Prevent edit form from
             * accidentally changing active status.
             */
            movie.setActive(
                    existing.isActive()
            );
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
    public void deleteMovie(Long id) {

        // Permanently delete movie and all related records.
        permanentlyDeleteMovie(id);
    }


    // ============================================================
    // PERMANENTLY DELETE MOVIE
    // ============================================================
    //
    // HARD DELETE:
    //
    // This permanently removes:
    //
    // - Refund records
    // - Cancellation requests
    // - Booking seats
    // - Payments
    // - Tickets
    // - Bookings
    // - Seat holds
    // - Reviews
    // - Show schedules
    // - Movie
    //
    // Everything runs inside one transaction.
    // If any statement fails, Spring rolls everything back.
    //
    @Transactional
    public void permanentlyDeleteMovie(Long movieId) {


        // ========================================================
        // 1. CHECK MOVIE EXISTS
        // ========================================================

        if (!movieRepository.existsById(movieId)) {

            throw new IllegalArgumentException(
                    "Movie not found."
            );
        }


        // Make Hibernate finish any pending operations first.
        entityManager.flush();



        // ========================================================
        // 2. DELETE REFUNDS THROUGH CANCELLATION REQUESTS
        // ========================================================
        //
        // Actual database foreign key:
        //
        // refunds.booking_cancellation_id
        //      ->
        // booking_cancellations.id
        //
        entityManager
                .createNativeQuery("""
                    DELETE r
                    FROM refunds r

                    INNER JOIN booking_cancellations bc
                        ON r.booking_cancellation_id = bc.id

                    INNER JOIN bookings b
                        ON bc.booking_id = b.id

                    INNER JOIN show_schedules s
                        ON b.show_schedule_id = s.id

                    WHERE s.movie_id = :movieId
                """)
                .setParameter(
                        "movieId",
                        movieId
                )
                .executeUpdate();



        // ========================================================
        // 3. DELETE ANY REMAINING REFUNDS LINKED TO BOOKINGS
        // ========================================================
        //
        // Your Refund entity also contains booking_id.
        //
        // This second delete is a safety measure for refunds that
        // may not have a booking_cancellation_id.
        //
        entityManager
                .createNativeQuery("""
                    DELETE r
                    FROM refunds r

                    INNER JOIN bookings b
                        ON r.booking_id = b.id

                    INNER JOIN show_schedules s
                        ON b.show_schedule_id = s.id

                    WHERE s.movie_id = :movieId
                """)
                .setParameter(
                        "movieId",
                        movieId
                )
                .executeUpdate();



        // ========================================================
        // 4. DELETE BOOKING CANCELLATION REQUESTS
        // ========================================================
        //
        // Foreign key:
        //
        // booking_cancellations.booking_id
        //      ->
        // bookings.id
        //
        entityManager
                .createNativeQuery("""
                    DELETE bc
                    FROM booking_cancellations bc

                    INNER JOIN bookings b
                        ON bc.booking_id = b.id

                    INNER JOIN show_schedules s
                        ON b.show_schedule_id = s.id

                    WHERE s.movie_id = :movieId
                """)
                .setParameter(
                        "movieId",
                        movieId
                )
                .executeUpdate();



        // ========================================================
        // 5. DELETE BOOKING SEATS
        // ========================================================
        //
        // Your actual database constraint is:
        //
        // booking_seats.booking_id
        //      ->
        // bookings.id
        //
        entityManager
                .createNativeQuery("""
                    DELETE bs
                    FROM booking_seats bs

                    INNER JOIN bookings b
                        ON bs.booking_id = b.id

                    INNER JOIN show_schedules s
                        ON b.show_schedule_id = s.id

                    WHERE s.movie_id = :movieId
                """)
                .setParameter(
                        "movieId",
                        movieId
                )
                .executeUpdate();



        // ========================================================
        // 6. DELETE PAYMENTS
        // ========================================================

        entityManager
                .createNativeQuery("""
                    DELETE p
                    FROM payments p

                    INNER JOIN bookings b
                        ON p.booking_id = b.id

                    INNER JOIN show_schedules s
                        ON b.show_schedule_id = s.id

                    WHERE s.movie_id = :movieId
                """)
                .setParameter(
                        "movieId",
                        movieId
                )
                .executeUpdate();



        // ========================================================
        // 7. DELETE TICKETS
        // ========================================================

        entityManager
                .createNativeQuery("""
                    DELETE t
                    FROM tickets t

                    INNER JOIN bookings b
                        ON t.booking_id = b.id

                    INNER JOIN show_schedules s
                        ON b.show_schedule_id = s.id

                    WHERE s.movie_id = :movieId
                """)
                .setParameter(
                        "movieId",
                        movieId
                )
                .executeUpdate();



        // ========================================================
        // 8. DELETE BOOKINGS
        // ========================================================
        //
        // All records with foreign keys to Booking have now
        // been removed.
        //
        entityManager
                .createNativeQuery("""
                    DELETE b
                    FROM bookings b

                    INNER JOIN show_schedules s
                        ON b.show_schedule_id = s.id

                    WHERE s.movie_id = :movieId
                """)
                .setParameter(
                        "movieId",
                        movieId
                )
                .executeUpdate();



        // ========================================================
        // 9. DELETE SEAT HOLDS
        // ========================================================
        //
        // Foreign key:
        //
        // seat_holds.show_schedule_id
        //      ->
        // show_schedules.id
        //
        entityManager
                .createNativeQuery("""
                    DELETE sh
                    FROM seat_holds sh

                    INNER JOIN show_schedules s
                        ON sh.show_schedule_id = s.id

                    WHERE s.movie_id = :movieId
                """)
                .setParameter(
                        "movieId",
                        movieId
                )
                .executeUpdate();



        // ========================================================
        // 10. DELETE REVIEWS
        // ========================================================
        //
        // Foreign key:
        //
        // reviews.movie_id
        //      ->
        // movies.id
        //
        entityManager
                .createNativeQuery("""
                    DELETE FROM reviews
                    WHERE movie_id = :movieId
                """)
                .setParameter(
                        "movieId",
                        movieId
                )
                .executeUpdate();



        // ========================================================
        // 11. DELETE SHOW SCHEDULES
        // ========================================================
        //
        // Seat holds and bookings have already been removed.
        //
        entityManager
                .createNativeQuery("""
                    DELETE FROM show_schedules
                    WHERE movie_id = :movieId
                """)
                .setParameter(
                        "movieId",
                        movieId
                )
                .executeUpdate();



        // ========================================================
        // 12. DELETE MOVIE
        // ========================================================

        int deleted =
                entityManager
                        .createNativeQuery("""
                            DELETE FROM movies
                            WHERE id = :movieId
                        """)
                        .setParameter(
                                "movieId",
                                movieId
                        )
                        .executeUpdate();


        if (deleted == 0) {

            throw new IllegalStateException(
                    "Movie could not be deleted."
            );
        }


        // Native SQL bypasses Hibernate's normal entity cache.
        // Clear the cache so Hibernate does not retain old data.
        entityManager.clear();
    }


    // Run these database steps together; runtime failures roll them back




    // Search all movies for admin management, including deleted movies
    public List<Movie> searchMovies(String keyword) {

        if (keyword == null || keyword.trim().isEmpty()) {
            return movieRepository.findAllByOrderByTitleAsc();
        }

        return movieRepository
                .findByTitleContainingIgnoreCaseOrderByTitleAsc(keyword.trim());
    }


    // Search active movies for the customer catalogue.
    // Existing callers keep the same behaviour when no status is supplied.
    public List<Movie> getPublicMovies(String keyword) {
        return getPublicMovies(keyword, null);
    }

    // Factory Pattern: the factory chooses a concrete MovieFilter product
    // (all, now showing, upcoming, or ended) based on the requested status.
    public List<Movie> getPublicMovies(
            String keyword,
            MovieStatus status) {

        List<Movie> movies;

        if (keyword == null || keyword.trim().isEmpty()) {
            movies = movieRepository.findByActiveTrueOrderByTitleAsc();
        } else {
            movies = movieRepository
                    .findByActiveTrueAndTitleContainingIgnoreCaseOrderByTitleAsc(
                            keyword.trim()
                    );
        }

        MovieFilter filter = movieFilterFactory.create(status);
        return filter.filter(movies);
    }
}
