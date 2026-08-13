package com.cinemax.cinemax.model;

import jakarta.persistence.*;

// Map this class to a database table
@Entity
@Table(
    name = "seats",
    uniqueConstraints = {
        @UniqueConstraint(
            columnNames = {"theatre_id", "seat_code"}
        )
    }
)
public class Seat {

    // Identify the primary key of this database record
    @Id
    // Let the database generate the record ID
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "row_label", nullable = false)
    private String rowLabel;

    @Column(name = "seat_number", nullable = false)
    private Integer seatNumber;

    @Column(name = "seat_code", nullable = false)
    private String seatCode;

    @Column(name = "seat_category", nullable = false)
    @Enumerated(EnumType.STRING)
    private SeatCategory seatCategory = SeatCategory.STANDARD;

    // Link many records of this type to one related record
    @ManyToOne
    // Store the related record's ID in this foreign-key column
    @JoinColumn(name = "theatre_id", nullable = false)
    private Theatre theatre;

    @Column(nullable = false)
    private boolean active = true;

    public Seat() {
    }

    public Seat(
            String rowLabel,
            Integer seatNumber,
            String seatCode,
            Theatre theatre) {

        this.rowLabel = rowLabel;
        this.seatNumber = seatNumber;
        this.seatCode = seatCode;
        this.theatre = theatre;
        this.active = true;
    }

    // Getters read field values; setters update them
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getRowLabel() {
        return rowLabel;
    }

    public void setRowLabel(String rowLabel) {
        this.rowLabel = rowLabel;
    }

    public Integer getSeatNumber() {
        return seatNumber;
    }

    public void setSeatNumber(Integer seatNumber) {
        this.seatNumber = seatNumber;
    }

    public String getSeatCode() {
        return seatCode;
    }

    public void setSeatCode(String seatCode) {
        this.seatCode = seatCode;
    }

    public SeatCategory getSeatCategory() {
        return seatCategory;
    }

    public void setSeatCategory(SeatCategory seatCategory) {
        this.seatCategory = seatCategory;
    }

    public Theatre getTheatre() {
        return theatre;
    }

    public void setTheatre(Theatre theatre) {
        this.theatre = theatre;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
