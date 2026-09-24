package com.cinemax.cinemax.model;

import jakarta.persistence.*;
import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;

import java.math.BigDecimal;
import java.time.LocalDateTime;

// Map this class to a database table
@Entity
@Table(name = "bookings")
public class Booking {

    // Identify the primary key of this database record
    @Id
    // Let the database generate the record ID
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(
        name = "booking_reference",
        nullable = false,
        unique = true,
        length = 30
    )
    private String bookingReference;

    // Link many records of this type to one related record
    @ManyToOne
    // Store the related record's ID in this foreign-key column
    @JoinColumn(
        name = "user_id",
        nullable = false
    )
    private User user;

    // Link many records of this type to one related record
    @ManyToOne
    // Treat a missing legacy show as null so old booking history can load
    @NotFound(action = NotFoundAction.IGNORE)
    // Store the related record's ID in this foreign-key column
    @JoinColumn(
        name = "show_schedule_id",
        nullable = false
    )
    private ShowSchedule showSchedule;

    @Column(
        name = "total_amount",
        nullable = false,
        precision = 10,
        scale = 2
    )
    private BigDecimal totalAmount;

    // Store the enum name in the database
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BookingStatus status;

    @Column(
        name = "created_at",
        nullable = false
    )
    private LocalDateTime createdAt;


    public Booking() {
    }


    // Run this method before inserting a new record
    @PrePersist
    // Set the creation timestamp before inserting this record
    public void onCreate() {

        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }


    // Getters read field values; setters update them
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }


    public String getBookingReference() {
        return bookingReference;
    }

    public void setBookingReference(
            String bookingReference) {

        this.bookingReference =
                bookingReference;
    }


    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }


    public ShowSchedule getShowSchedule() {
        return showSchedule;
    }

    public void setShowSchedule(
            ShowSchedule showSchedule) {

        this.showSchedule =
                showSchedule;
    }


    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(
            BigDecimal totalAmount) {

        this.totalAmount =
                totalAmount;
    }


    public BookingStatus getStatus() {
        return status;
    }

    public void setStatus(
            BookingStatus status) {

        this.status = status;
    }


    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(
            LocalDateTime createdAt) {

        this.createdAt = createdAt;
    }
}
