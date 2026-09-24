package com.cinemax.cinemax.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

// Map this class to a database table
@Entity
@Table(name = "screens")
public class Screen {

    // Identify the primary key of this database record
    @Id
    // Let the database generate the record ID
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Screen name is required")
    @Column(nullable = false)
    private String name;

    @NotNull(message = "Number of rows is required")
    @Min(value = 1, message = "Screen must contain at least one row")
    @Column(nullable = false)
    private Integer rowCount;

    @NotNull(message = "Seats per row is required")
    @Min(value = 1, message = "There must be at least one seat per row")
    @Column(nullable = false)
    private Integer seatsPerRow;

    @Column(nullable = false)
    private Integer capacity;

    // Link many records of this type to one related record
    @ManyToOne
    // Store the related record's ID in this foreign-key column
    @JoinColumn(name = "theatre_id", nullable = false)
    private Theatre theatre;

    @Column(nullable = false)
    private boolean active = true;

    public Screen() {
    }

    // Run this method before inserting a new record
    @PrePersist
    // Run this method before updating a saved record
    @PreUpdate
    // Calculate capacity from the number of rows and seats per row
    public void calculateCapacity() {
        if (rowCount != null && seatsPerRow != null) {
            this.capacity = rowCount * seatsPerRow;
        }
    }

    // Getters read field values; setters update them
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getRowCount() {
        return rowCount;
    }

    public void setRowCount(Integer rowCount) {
        this.rowCount = rowCount;
    }

    public Integer getSeatsPerRow() {
        return seatsPerRow;
    }

    public void setSeatsPerRow(Integer seatsPerRow) {
        this.seatsPerRow = seatsPerRow;
    }

    public Integer getCapacity() {
        return capacity;
    }

    public void setCapacity(Integer capacity) {
        this.capacity = capacity;
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
