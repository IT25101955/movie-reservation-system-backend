package com.cinemax.cinemax.repository;

import com.cinemax.cinemax.model.Seat;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

// JpaRepository supplies CRUD methods; method names below define database searches
public interface SeatRepository
        extends JpaRepository<Seat, Long> {

    List<Seat> findByTheatreIdAndActiveTrueOrderByRowLabelAscSeatNumberAsc(
            Long theatreId
    );

    List<Seat> findByTheatreIdAndRowLabel(
            Long theatreId,
            String rowLabel
    );

    List<Seat> findByTheatreIdOrderByRowLabelAscSeatNumberAsc(Long theatreId);

    void deleteByTheatreId(Long theatreId);
}
