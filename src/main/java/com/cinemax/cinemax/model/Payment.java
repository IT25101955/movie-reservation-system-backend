package com.cinemax.cinemax.model;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

// Map this class to a database table
@Entity
@Table(name = "payments")
public class Payment {

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


    @Column(
        name = "transaction_reference",
        nullable = false,
        unique = true
    )
    private String transactionReference;


    @Column(
        nullable = false,
        precision = 10,
        scale = 2
    )
    private BigDecimal amount;


    // Store the enum name in the database
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentMethod paymentMethod;


    // Store the enum name in the database
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus status;


    @Column(name = "card_last_four")
    private String cardLastFour;


    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;


    @Column(name = "paid_at")
    private LocalDateTime paidAt;


    public Payment() {
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


    public Booking getBooking() {
        return booking;
    }

    public void setBooking(Booking booking) {
        this.booking = booking;
    }


    public String getTransactionReference() {
        return transactionReference;
    }

    public void setTransactionReference(
            String transactionReference) {

        this.transactionReference =
                transactionReference;
    }


    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }


    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(
            PaymentMethod paymentMethod) {

        this.paymentMethod =
                paymentMethod;
    }


    public PaymentStatus getStatus() {
        return status;
    }

    public void setStatus(PaymentStatus status) {
        this.status = status;
    }


    public String getCardLastFour() {
        return cardLastFour;
    }

    public void setCardLastFour(
            String cardLastFour) {

        this.cardLastFour =
                cardLastFour;
    }


    public LocalDateTime getCreatedAt() {
        return createdAt;
    }


    public LocalDateTime getPaidAt() {
        return paidAt;
    }

    public void setPaidAt(LocalDateTime paidAt) {
        this.paidAt = paidAt;
    }
}
