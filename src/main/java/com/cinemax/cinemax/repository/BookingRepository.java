package com.cinemax.cinemax.repository;

import com.cinemax.cinemax.model.Booking;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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

        List<Booking> findByShowSchedule_IdIn(List<Long> showIds);

        void deleteByShowSchedule_IdIn(List<Long> showIds);

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

    // ============================================================
    // LOCK BOOKING WHILE PAYMENT IS BEING PROCESSED
    // ============================================================
    //
    // PESSIMISTIC_WRITE tells MySQL:
    //
    // "Only one transaction may process this booking at a time."
    //
    // Example:
    //
    // Request 1:
    //     locks Booking 25
    //
    // Request 2:
    //     tries Booking 25
    //     -> waits until Request 1 finishes
    //
    // This prevents two ticket/payment operations from running
    // simultaneously for the same booking.
    //
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        SELECT b
        FROM Booking b
        WHERE b.id = :bookingId
          AND b.user.id = :userId
    """)
    Optional<Booking> findForPaymentUpdate(

            @Param("bookingId")
            Long bookingId,

            @Param("userId")
            Long userId
    );

}
