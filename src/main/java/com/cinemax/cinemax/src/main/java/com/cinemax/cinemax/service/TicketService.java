package com.cinemax.cinemax.service;

import com.cinemax.cinemax.model.*;

import com.cinemax.cinemax.repository.TicketRepository;

import jakarta.transaction.Transactional;

import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

// Spring creates this service to handle business rules
@Service
public class TicketService {

    private final TicketRepository
            ticketRepository;


    // Constructor injection supplies the dependencies this class needs
    public TicketService(
            TicketRepository ticketRepository) {

        this.ticketRepository =
                ticketRepository;
    }


    // Run these database steps together; runtime failures roll them back
    @Transactional
    // Create a ticket for a confirmed booking unless one already exists
    public Ticket generateTicket(
            Booking booking) {

        if (booking.getStatus()
                != BookingStatus.CONFIRMED) {

            throw new IllegalArgumentException(
                "Ticket can only be generated for a confirmed booking."
            );
        }


        // Return the existing ticket if one was already created
        Optional<Ticket> existingTicket =
                ticketRepository.findByBooking_Id(booking.getId());

        // Check whether the database search found a record
        if (existingTicket.isPresent()) {
            return existingTicket.get();
        }

        // Create a new ticket
        Ticket ticket = new Ticket();
        ticket.setTicketNumber(generateTicketNumber());
        ticket.setBooking(booking);
        ticket.setActive(true);

        return ticketRepository.save(ticket);
    }


    // Create a short random ticket reference
    private String generateTicketNumber() {

        return "TKT-"
                + UUID.randomUUID()
                    .toString()
                    .substring(0, 10)
                    .toUpperCase();
    }


    // Load an active ticket only when it belongs to this customer
    public Ticket getTicketForUser(
            Long ticketId,
            Long userId) {

        // Optional holds the ticket when the database finds a match
        Optional<Ticket> ticket =
                ticketRepository
                        .findByIdAndBooking_User_IdAndActiveTrue(
                                ticketId,
                                userId
                        );

        // Check whether the database search found a record
        if (ticket.isPresent()) {
            return ticket.get();
        }

        // Stop if the requested record does not exist or is inaccessible
        throw new IllegalArgumentException(
                "Active ticket not found or access denied."
        );
    }


    // Return the booking's ticket or null if none exists
    public Ticket getTicketForBooking(
            Long bookingId) {

        // Optional holds the ticket when the database finds a match
        Optional<Ticket> ticket =
                ticketRepository.findByBooking_Id(bookingId);

        // Check whether the database search found a record
        if (ticket.isPresent()) {
            return ticket.get();
        }

        return null;
    }

    // Run these database steps together; runtime failures roll them back
    @Transactional
    // Make the booking's ticket invalid after cancellation
    public void deactivateTicketForBooking(
            Long bookingId) {

        // Optional holds the ticket when the database finds a match
        Optional<Ticket> ticket =
                ticketRepository.findByBooking_Id(bookingId);

        // Check whether the database search found a record
        if (ticket.isPresent()) {
            Ticket savedTicket = ticket.get();
            // Deactivate this record without removing it from the database
            savedTicket.setActive(false);
            ticketRepository.save(savedTicket);
        }
    }
}
