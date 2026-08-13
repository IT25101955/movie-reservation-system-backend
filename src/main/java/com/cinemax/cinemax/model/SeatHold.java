package com.cinemax.cinemax.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

// Map this class to a database table
@Entity
@Table(
    name = "seat_holds",
    uniqueConstraints = {
        @UniqueConstraint(
            columnNames = {
                "show_schedule_id",
                "seat_id"
            }
        )
    }
)
public class SeatHold {

    // Identify the primary key of this database record
    @Id
    // Let the database generate the record ID
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    // Link many records of this type to one related record
    @ManyToOne
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


    // Link many records of this type to one related record
    @ManyToOne
    // Store the related record's ID in this foreign-key column
    @JoinColumn(
        name = "user_id",
        nullable = false
    )
    private User user;


    @Column(
        name = "held_until",
        nullable = false
    )
    private LocalDateTime heldUntil;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TicketType ticketType = TicketType.ADULT;


    public SeatHold() {
    }


    // Getters read field values; setters update them
    public Long getId() {
        return id;
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

    public void setSeat(
            Seat seat) {

        this.seat = seat;
    }


    public User getUser() {
        return user;
    }

    public void setUser(
            User user) {

        this.user = user;
    }


    public LocalDateTime getHeldUntil() {
        return heldUntil;
    }

    public void setHeldUntil(
            LocalDateTime heldUntil) {

        this.heldUntil =
                heldUntil;
    }

    public TicketType getTicketType() {
        return ticketType;
    }

    public void setTicketType(TicketType ticketType) {
        this.ticketType = ticketType;
    }
}
