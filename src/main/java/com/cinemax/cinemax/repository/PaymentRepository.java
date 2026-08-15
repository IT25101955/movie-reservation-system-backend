package com.cinemax.cinemax.repository;

import com.cinemax.cinemax.model.Payment;
import com.cinemax.cinemax.model.PaymentStatus;
import com.cinemax.cinemax.model.BookingStatus;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

// JpaRepository supplies CRUD methods; method names below define database searches
public interface PaymentRepository
        extends JpaRepository<Payment, Long> {

        void deleteByBooking_IdIn(List<Long> bookingIds);

    // Load payment attempts for this booking with the newest first
    List<Payment>
        findByBooking_IdOrderByCreatedAtDesc(
            Long bookingId
        );
        
    List<Payment> findByBooking_Id(Long bookingId);

    // Optional holds the payment when the database finds a match
    // Find a payment only when its booking belongs to this customer
    Optional<Payment>
    findByIdAndBooking_User_Id(
            Long paymentId,
            Long userId
    );

    // Load all records with the newest first
    List<Payment>
    findAllByOrderByCreatedAtDesc();

    // Load records with the selected status, newest first
    List<Payment>
    findByStatusOrderByCreatedAtDesc(
            PaymentStatus status
    );

    // Count records with the selected status
    long countByStatus(
            PaymentStatus status
    );

    // Sum matching payments; COALESCE returns zero when none match
    @Query("""
        SELECT COALESCE(SUM(p.amount), 0)
        FROM Payment p
        WHERE p.status = :status
    """)
    BigDecimal sumAmountByStatus(
            @Param("status")
            PaymentStatus status
    );

        // Sum successful payments for bookings that are still confirmed
        @Query("""
                SELECT COALESCE(SUM(p.amount), 0)
                FROM Payment p
                WHERE p.status = :paymentStatus
                AND p.booking.status = :bookingStatus
        """)
        BigDecimal calculateRevenue(
                        @Param("paymentStatus")
                        PaymentStatus paymentStatus,

                        @Param("bookingStatus")
                        BookingStatus bookingStatus
        );
}
