package com.cinemax.cinemax.repository;

import com.cinemax.cinemax.model.BookingSeat;
import com.cinemax.cinemax.model.BookingStatus;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

// JpaRepository supplies CRUD methods; method names below define database searches
public interface BookingSeatRepository
        extends JpaRepository<BookingSeat, Long> {

    // Load seats already booked for this show
    List<BookingSeat>
        findByShowSchedule_Id(
            Long showId
        );

    // Load records linked to this booking
    List<BookingSeat>
        findByBooking_Id(
            Long bookingId
        );

    void deleteByBooking_IdIn(List<Long> bookingIds);

    // Count seats whose booking has the selected status
    long countByBooking_Status(
            BookingStatus status
    );

        boolean existsBySeat_Theatre_Id(
            Long theatreId
    );
}
