package com.cinemax.cinemax.service;

import com.cinemax.cinemax.model.*;

import com.cinemax.cinemax.repository.NotificationRepository;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

// Spring creates this service to handle business rules
@Service
public class NotificationService {

    private final NotificationRepository
            notificationRepository;

    private final EmailService
            emailService;


    // Constructor injection supplies the dependencies this class needs
    public NotificationService(
            NotificationRepository notificationRepository,
            EmailService emailService) {

        this.notificationRepository =
                notificationRepository;

        this.emailService =
                emailService;
    }


    // Save the ticket notification and attempt to email the customer
    public Notification
    createTicketNotification(
            Ticket ticket) {

        Booking booking =
                ticket.getBooking();

        User customer =
                booking.getUser();


        String title =
                "Your CineMax E-Ticket is ready";


        String message =
                "Booking "
                + booking.getBookingReference()
                + " for "
                + booking
                    .getShowSchedule()
                    .getMovie()
                    .getTitle()
                + " has been confirmed. "
                + "Ticket number: "
                + ticket.getTicketNumber()
                + ".";


        Notification notification =
                new Notification();


        notification.setUser(
            customer
        );

        notification.setTitle(
            title
        );

        notification.setMessage(
            message
        );

        notification.setType(
            NotificationType.TICKET_GENERATED
        );

        notification.setEmailStatus(
            EmailStatus.NOT_ATTEMPTED
        );


        notification =
                notificationRepository
                    .save(notification);


        boolean sent =
                emailService.sendEmail(
                    customer.getEmail(),
                    title,
                    message
                    + "\n\nLogin to CineMax Movies "
                    + "to view or download your ticket."
                );


        if (sent) {
            notification.setEmailStatus(EmailStatus.SENT);
        } else {
            notification.setEmailStatus(EmailStatus.FAILED);
        }


        return notificationRepository
                .save(notification);
    }


    // Load this customer's notifications with the newest first
    public List<Notification>
    getNotificationsForUser(
            Long userId) {

        return notificationRepository
                .findByUser_IdOrderByCreatedAtDesc(
                    userId
                );
    }


    // Check ownership before marking the notification as read
    public void markAsRead(
            Long notificationId,
            Long userId) {

        // Optional holds the notification when the database finds a match
        Optional<Notification> optionalNotification =
                notificationRepository.findById(notificationId);

        // Stop when the database search found no record
        if (optionalNotification.isEmpty()) {
            throw new IllegalArgumentException(
                    "Notification not found"
            );
        }

        Notification notification = optionalNotification.get();


        if (!notification
                .getUser()
                .getId()
                .equals(userId)) {

            throw new IllegalArgumentException(
                "Access denied"
            );
        }


        notification.setRead(true);

        notificationRepository
                .save(notification);
    }
}
