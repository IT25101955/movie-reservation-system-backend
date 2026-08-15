package com.cinemax.cinemax.repository;

import com.cinemax.cinemax.model.SeatHold;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

// JpaRepository supplies CRUD methods; method names below define database searches
public interface SeatHoldRepository
        extends JpaRepository<SeatHold, Long> {

    // Load holds for this show that have not expired at the given time
    List<SeatHold>
        findByShowSchedule_IdAndHeldUntilAfter(
            Long showId,
            LocalDateTime time
        );


    // Load this customer's unexpired holds for the selected show
    List<SeatHold>
        findByUser_IdAndShowSchedule_IdAndHeldUntilAfter(
            Long userId,
            Long showId,
            LocalDateTime time
        );


    // Release holds that expired before the given time
    void deleteByHeldUntilBefore(
            LocalDateTime time
    );


    // Release this customer's holds for the selected show
    void deleteByUser_IdAndShowSchedule_Id(
            Long userId,
            Long showId
    );

    // Release all temporary holds for the selected show
    void deleteByShowSchedule_Id(
            Long showScheduleId
    );

        void deleteByShowSchedule_IdIn(List<Long> showIds);

        boolean existsBySeat_Theatre_Id(
            Long theatreId
    );
}
