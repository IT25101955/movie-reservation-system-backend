package com.cinemax.cinemax.repository;

import com.cinemax.cinemax.model.ShowSchedule;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

// JpaRepository supplies CRUD methods; method names below define database searches
public interface ShowScheduleRepository
        extends JpaRepository<ShowSchedule, Long> {

    boolean existsByMovieId(Long movieId);

    List<ShowSchedule> findByMovieId(Long movieId);

    void deleteByMovieId(Long movieId);

    List<ShowSchedule>
        findAllByOrderByMovieTitleAscTheatreNameAscShowDateAscStartTimeAsc();

        @Query("""
                SELECT DISTINCT s.showDate
                FROM ShowSchedule s
                WHERE s.movie.id = :movieId
                    AND s.active = true
                    AND (s.showDate > :today OR
                             (s.showDate = :today AND s.startTime > :currentTime))
                ORDER BY s.showDate
        """)
        List<LocalDate> findAvailableDates(
                        @Param("movieId") Long movieId,
                        @Param("today") LocalDate today,
                        @Param("currentTime") LocalTime currentTime);

        @Query("""
                SELECT s
                FROM ShowSchedule s
                JOIN FETCH s.theatre t
                WHERE s.movie.id = :movieId
                    AND s.showDate = :showDate
                    AND s.active = true
                ORDER BY t.name, s.startTime
        """)
        List<ShowSchedule> findAvailableShows(
                        @Param("movieId") Long movieId,
                        @Param("showDate") LocalDate showDate);

    List<ShowSchedule>
        findByTheatreIdAndShowDateAndActiveTrue(
            Long theatreId,
            LocalDate showDate
        );

    List<ShowSchedule>
        findByTheatreIdAndActiveTrue(Long theatreId);

    boolean existsByTheatreId(Long theatreId);

    List<ShowSchedule>
        findByMovieIdAndTheatreIdAndShowDateAndActiveTrue(
            Long movieId,
            Long theatreId,
            LocalDate showDate
        );


    // Load all shows in date and time order
    List<ShowSchedule>
        findAllByOrderByShowDateAscStartTimeAsc();


    // Load active shows for this movie in date and time order
    List<ShowSchedule>
        findByMovieIdAndActiveTrueOrderByShowDateAscStartTimeAsc(
            Long movieId
        );

    // Count records whose active flag is true
    long countByActiveTrue();

    // Lock the selected show until the transaction finishes
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    // Query the ShowSchedule entity using the supplied ID
    @Query("SELECT s FROM ShowSchedule s WHERE s.id = :id")
    // Optional holds the showschedule when the database finds a match
    Optional<ShowSchedule> findByIdForUpdate(
            @Param("id") Long id
    );
}
