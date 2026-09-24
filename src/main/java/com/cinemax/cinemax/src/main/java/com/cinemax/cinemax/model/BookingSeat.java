package com.cinemax.cinemax.model;

import jakarta.persistence.*;
import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;

import java.math.BigDecimal;

// Map this class to a database table
@Entity
@Table(
    name = "booking_seats",
    uniqueConstraints = {
        // Prevent the same seat from being booked twice for one show
        @UniqueConstraint(
            columnNames = {
                "show_schedule_id",
                "seat_id"
            }
        )
    }
)
public class BookingSeat {

    // Identify the primary key of this database record
    @Id
    // Let the database generate the record ID
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    // Link many records of this type to one related record
    @ManyToOne
    // Store the related record's ID in this foreign-key column
    @JoinColumn(
        name = "booking_id",
        nullable = false
    )
    private Booking booking;


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


    // Link many records of this type to one related record
    @ManyToOne
    // Store the related record's ID in this foreign-key column
    @JoinColumn(
        name = "seat_id",
        nullable = false
    )
    private Seat seat;


    @Column(
        name = "seat_price",
        nullable = false,
        precision = 10,
        scale = 2
    )
    private BigDecimal seatPrice;


    public BookingSeat() {
    }


    // Getters read field values; setters update them
    public Long getId() {
        return id;
    }


    public Booking getBooking() {
        return booking;
    }

    public void setBooking(
            Booking booking) {

        this.booking = booking;
    }


    public ShowSchedule getShowSchedule() {
        return showSchedule;
    }

    public void setShowSchedule(
            ShowSchedule showSchedule) {

        this.showSchedule =
                showSchedule;
    }


    public Seat getSeat() {
        return seat;
    }

    public void setSeat(Seat seat) {
        this.seat = seat;
    }


    public BigDecimal getSeatPrice() {
        return seatPrice;
    }

    public void setSeatPrice(
            BigDecimal seatPrice) {

        this.seatPrice =
                seatPrice;
    }
}
