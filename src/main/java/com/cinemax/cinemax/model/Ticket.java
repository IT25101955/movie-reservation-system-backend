package com.cinemax.cinemax.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

// Map this class to a database table
@Entity
@Table(name = "tickets")
public class Ticket {

    // Identify the primary key of this database record
    @Id
    // Let the database generate the record ID
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @Column(
        name = "ticket_number",
        nullable = false,
        unique = true,
        length = 30
    )
    private String ticketNumber;


    // Link this record to one related record
    @OneToOne
    // Store the related record's ID in this foreign-key column
    @JoinColumn(
        name = "booking_id",
        nullable = false,
        unique = true
    )
    private Booking booking;


    @Column(
        name = "generated_at",
        nullable = false
    )
    private LocalDateTime generatedAt;


    @Column(nullable = false)
    private boolean active = true;


    public Ticket() {
    }


    // Run this method before inserting a new record
    @PrePersist
    // Set the creation timestamp before inserting this record
    public void onCreate() {

        if (generatedAt == null) {
            generatedAt = LocalDateTime.now();
        }
    }


    // Getters read field values; setters update them
    public Long getId() {
        return id;
    }


    public String getTicketNumber() {
        return ticketNumber;
    }

    public void setTicketNumber(
            String ticketNumber) {

        this.ticketNumber = ticketNumber;
    }


    public Booking getBooking() {
        return booking;
    }

    public void setBooking(
            Booking booking) {

        this.booking = booking;
    }


    public LocalDateTime getGeneratedAt() {
        return generatedAt;
    }


    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
