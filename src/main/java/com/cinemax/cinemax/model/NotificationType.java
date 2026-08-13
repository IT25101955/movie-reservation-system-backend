package com.cinemax.cinemax.model;

// Defines every notification category supported by CineMax
public enum NotificationType {

    // Booking and payment notifications
    BOOKING_CREATED,
    BOOKING_CONFIRMED,
    PAYMENT_SUCCESS,
    PAYMENT_FAILED,
    BOOKING_CANCELLED,
    TICKET_GENERATED,

    // Keep these because your current cancellation/refund
    // feature already uses approval and rejection.
    CANCELLATION_APPROVED,
    CANCELLATION_REJECTED,

    // New notification types used by Admin broadcasts
    PROMOTION,
    DISCOUNT,
    ANNOUNCEMENT,
    GENERAL
}
