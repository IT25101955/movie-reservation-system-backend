package com.cinemax.cinemax.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

// Map this class to a database table
@Entity
@Table(name = "show_schedules")
public class ShowSchedule {

    // Identify the primary key of this database record
    @Id
    // Let the database generate the record ID
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

   
    // Link many records of this type to one related record
    @ManyToOne
    // Store the related record's ID in this foreign-key column
    @JoinColumn(name = "movie_id", nullable = false)
    private Movie movie;


    // Link many records of this type to one related record
    @ManyToOne
    // Store the related record's ID in this foreign-key column
    @JoinColumn(name = "theatre_id", nullable = false)
    private Theatre theatre;

    @NotNull(message = "Show date is required")
    @Column(nullable = false)
    private LocalDate showDate;

    @NotNull(message = "Show time is required")
    @Column(nullable = false)
    private LocalTime startTime;

    @NotNull
    @Column(nullable = false)
    private LocalTime endTime;

    @NotNull(message = "Adult price is required")
    @DecimalMin(value = "0.01", message = "Adult price must be greater than 0")
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal adultPrice;

    @NotNull(message = "Child price is required")
    @DecimalMin(value = "0.01", message = "Child price must be greater than 0")
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal childPrice;

    @NotNull(message = "VIP price is required")
    @DecimalMin(value = "0.01", message = "VIP price must be greater than 0")
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal vipPrice;

    @Column(nullable = false)
    private boolean active = true;

    public ShowSchedule() {
    }

    // Getters read field values; setters update them
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Movie getMovie() {
        return movie;
    }

    public void setMovie(Movie movie) {
        this.movie = movie;
    }

    public Theatre getTheatre() {
        return theatre;
    }

    public void setTheatre(Theatre theatre) {
        this.theatre = theatre;
    }

    public LocalDate getShowDate() {
        return showDate;
    }

    public void setShowDate(LocalDate showDate) {
        this.showDate = showDate;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalTime startTime) {
        this.startTime = startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalTime endTime) {
        this.endTime = endTime;
    }

    public BigDecimal getAdultPrice() {
        return adultPrice;
    }

    public void setAdultPrice(BigDecimal adultPrice) {
        this.adultPrice = adultPrice;
    }

    public BigDecimal getChildPrice() {
        return childPrice;
    }

    public void setChildPrice(BigDecimal childPrice) {
        this.childPrice = childPrice;
    }

    public BigDecimal getVipPrice() {
        return vipPrice;
    }

    public void setVipPrice(BigDecimal vipPrice) {
        this.vipPrice = vipPrice;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
