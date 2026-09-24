package com.cinemax.cinemax.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

// Map this class to a database table
@Entity
@Table(name = "reviews")
public class Review {

    // Identify the primary key of this database record
    @Id
    // Let the database generate the record ID
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


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
    // Store the related record's ID in this foreign-key column
    @JoinColumn(
        name = "movie_id",
        nullable = false
    )
    private Movie movie;


    @Column(nullable = false)
    private Integer rating;


    @Column(
        nullable = false,
        length = 1000
    )
    private String comment;


    // Store the enum name in the database
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReviewStatus status;


    @Column(
        name = "created_at",
        nullable = false
    )
    private LocalDateTime createdAt;


    @Column(
        name = "moderated_at"
    )
    private LocalDateTime moderatedAt;


    public Review() {
    }


    // Run this method before inserting a new record
    @PrePersist
    // Set the creation timestamp before inserting this record
    public void onCreate() {

        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }

        // New reviews wait for administrator approval
        if (status == null) {
            status = ReviewStatus.PENDING;
        }

        if (status == null) {
            status = ReviewStatus.PENDING;
        }
    }


    // Getters read field values; setters update them
    public Long getId() {
        return id;
    }


    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }


    public Movie getMovie() {
        return movie;
    }

    public void setMovie(Movie movie) {
        this.movie = movie;
    }


    public Integer getRating() {
        return rating;
    }

    public void setRating(Integer rating) {
        this.rating = rating;
    }


    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }


    public ReviewStatus getStatus() {
        return status;
    }

    public void setStatus(
            ReviewStatus status) {

        this.status = status;
    }


    public LocalDateTime getCreatedAt() {
        return createdAt;
    }


    public LocalDateTime getModeratedAt() {
        return moderatedAt;
    }

    public void setModeratedAt(
            LocalDateTime moderatedAt) {

        this.moderatedAt =
                moderatedAt;
    }
}
