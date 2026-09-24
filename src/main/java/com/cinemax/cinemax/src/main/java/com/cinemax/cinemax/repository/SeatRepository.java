package com.cinemax.cinemax.repository;

import com.cinemax.cinemax.model.Seat;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

// JpaRepository supplies CRUD methods; method names below define database searches
public interface SeatRepository
        extends JpaRepository<Seat, Long> {

    // Load screen seats sorted by row and seat number
    List<Seat> findByScreenIdOrderByRowLabelAscSeatNumberAsc(
            Long screenId
    );

    // Remove the screen's seats when rebuilding an unused layout
    void deleteByScreenId(Long screenId);
}
