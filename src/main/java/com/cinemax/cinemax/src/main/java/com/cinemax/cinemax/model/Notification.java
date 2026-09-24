package com.cinemax.cinemax.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

// Map this class to a database table
@Entity
@Table(name = "notifications")
public class Notification {

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


    @Column(nullable = false)
    private String title;


    @Column(
        nullable = false,
        length = 1500
    )
    private String message;


    // Store the enum name in the database
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationType type;


    // Store the enum name in the database
    @Enumerated(EnumType.STRING)
    @Column(
        name = "email_status",
        nullable = false
    )
    private EmailStatus emailStatus;


    @Column(
        name = "is_read",
        nullable = false
    )
    private boolean read = false;


    @Column(
        name = "created_at",
        nullable = false
    )
    private LocalDateTime createdAt;


    public Notification() {
    }


    // Run this method before inserting a new record
    @PrePersist
    // Set the creation timestamp before inserting this record
    public void onCreate() {

        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }

        if (emailStatus == null) {
            emailStatus =
                    EmailStatus.NOT_ATTEMPTED;
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


    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }


    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }


    public NotificationType getType() {
        return type;
    }

    public void setType(
            NotificationType type) {

        this.type = type;
    }


    public EmailStatus getEmailStatus() {
        return emailStatus;
    }

    public void setEmailStatus(
            EmailStatus emailStatus) {

        this.emailStatus = emailStatus;
    }


    public boolean isRead() {
        return read;
    }

    public void setRead(boolean read) {
        this.read = read;
    }


    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
