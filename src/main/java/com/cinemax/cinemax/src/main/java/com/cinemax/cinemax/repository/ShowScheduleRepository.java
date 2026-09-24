package com.cinemax.cinemax.repository;

import com.cinemax.cinemax.model.ShowSchedule;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

// JpaRepository supplies CRUD methods; method names below define database searches
public interface ShowScheduleRepository
        extends JpaRepository<ShowSchedule, Long> {

    // Load active shows for this screen and date
    List<ShowSchedule>
        findByScreenIdAndShowDateAndActiveTrue(
            Long screenId,
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
