package com.cinemax.cinemax.repository;

import com.cinemax.cinemax.model.Ticket;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

// JpaRepository supplies CRUD methods; method names below define database searches
public interface TicketRepository
        extends JpaRepository<Ticket, Long> {

    // Optional holds the ticket when the database finds a match
    // Load records linked to this booking
    Optional<Ticket>
        findByBooking_Id(
            Long bookingId
        );


    // Optional holds the ticket when the database finds a match
    // Find an active ticket owned by the specified customer
    Optional<Ticket>
        findByIdAndBooking_User_IdAndActiveTrue(
            Long ticketId,
            Long userId
        );

    // Count active tickets belonging to this customer
    long countByBooking_User_IdAndActiveTrue(
            Long userId
    );
}
