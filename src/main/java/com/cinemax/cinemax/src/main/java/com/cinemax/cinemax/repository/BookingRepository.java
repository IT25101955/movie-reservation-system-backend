package com.cinemax.cinemax.repository;

import com.cinemax.cinemax.model.Booking;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import com.cinemax.cinemax.model.BookingStatus;

// JpaRepository supplies CRUD methods; method names below define database searches
public interface BookingRepository
        extends JpaRepository<Booking, Long> {

    // Load this user's records with the newest first
    List<Booking>
        findByUser_IdOrderByCreatedAtDesc(
            Long userId
        );


    // Optional holds the booking when the database finds a match
    // Find a booking using both its ID and the customer ID
    Optional<Booking>
        findByIdAndUser_Id(
            Long bookingId,
            Long userId
        );

    // Custom query to find bookings by user ID, movie ID, and booking status
    List<Booking>
    findByUser_IdAndShowSchedule_Movie_IdAndStatus(
        Long userId,
        Long movieId,
        BookingStatus status
    );

    // Check whether any booking references this show
    boolean existsByShowSchedule_Id(
            Long showScheduleId
    );

    // Load all records with the newest first
    List<Booking>
    findAllByOrderByCreatedAtDesc();

    // Load records with the selected status, newest first
    List<Booking>
    findByStatusOrderByCreatedAtDesc(
            BookingStatus status
    );

    // Count records with the selected status
    long countByStatus(
            BookingStatus status
    );

    // Count bookings belonging to this customer
    long countByUser_Id(
            Long userId
    );

    // Count this customer's bookings with the selected status
    long countByUser_IdAndStatus(
            Long userId,
            BookingStatus status
    );

}
