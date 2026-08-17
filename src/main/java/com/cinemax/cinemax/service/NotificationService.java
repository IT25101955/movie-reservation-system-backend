package com.cinemax.cinemax.service;

import com.cinemax.cinemax.model.*;

import com.cinemax.cinemax.repository.BookingSeatRepository;
import com.cinemax.cinemax.repository.NotificationRepository;
import com.cinemax.cinemax.repository.UserRepository;

import jakarta.transaction.Transactional;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;


// Handles customer notifications,
// ticket notifications,
// cancellation/refund notifications,
// and Admin broadcasts.
@Service
public class NotificationService {

    private final NotificationRepository
            notificationRepository;

    private final EmailService
            emailService;

    private final UserRepository
            userRepository;

    private final BookingSeatRepository
            bookingSeatRepository;


    // Constructor injection
    public NotificationService(

            NotificationRepository
                    notificationRepository,

            EmailService
                    emailService,

            UserRepository
                    userRepository,

            BookingSeatRepository
                    bookingSeatRepository) {

        this.notificationRepository =
                notificationRepository;

        this.emailService =
                emailService;

        this.userRepository =
                userRepository;

        this.bookingSeatRepository =
                bookingSeatRepository;
    }


    // =========================================================
    // SEAT DISPLAY
    // =========================================================

    // Convert BookingSeat records into:
    //
    // A1, A2, A3
    //
    // This is used inside booking and ticket notifications.
    private String formatSeatCodes(
            Long bookingId) {

        List<BookingSeat> seats =
                bookingSeatRepository
                        .findByBooking_Id(
                                bookingId
                        );


        if (seats.isEmpty()) {

            return "N/A";
        }


        return seats.stream()

                .map(
                    bookingSeat ->
                        bookingSeat
                            .getSeat()
                            .getSeatCode()
                )

                .collect(
                    Collectors.joining(", ")
                );
    }


    // =========================================================
    // EMAIL HELPER
    // =========================================================

    // Email is optional in CineMax.
    //
    // When mail is disabled:
    //     status = NOT_ATTEMPTED
    //
    // When email sends:
    //     status = SENT
    //
    // When SMTP fails:
    //     status = FAILED
    private Notification trySendEmail(

            Notification notification,

            String email,

            String subject,

            String message) {


        // Do not call SMTP when mail is disabled
        if (!emailService.isMailEnabled()) {

            notification.setEmailStatus(
                    EmailStatus.NOT_ATTEMPTED
            );


            return notificationRepository
                    .save(notification);
        }


        boolean sent =
                emailService.sendEmail(

                        email,

                        subject,

                        message
                );


        notification.setEmailStatus(

                sent

                    ? EmailStatus.SENT

                    : EmailStatus.FAILED
        );


        return notificationRepository
                .save(notification);
    }


    // =========================================================
    // BOOKING CREATED
    // =========================================================

    // Called immediately after the customer confirms
    // their held seats and CineMax creates the booking.
    @Transactional
    public Notification createBookingNotification(
            Booking booking) {


        User customer =
                booking.getUser();


        ShowSchedule show =
                booking.getShowSchedule();


        String movieTitle =

                show != null
                && show.getMovie() != null

                ? show.getMovie().getTitle()

                : "Movie";


        /*
         IMPORTANT:

         Your CURRENT system uses:

         ShowSchedule -> Theatre

         It DOES NOT use:

         ShowSchedule -> Screen -> Theatre

         Therefore we use getTheatre().
        */
        String theatreName =

                show != null
                && show.getTheatre() != null

                ? show.getTheatre().getName()

                : "CineMax Theatre";


        String theatreLocation =

                show != null
                && show.getTheatre() != null

                ? show.getTheatre().getLocation()

                : "N/A";


        String showDate =

                show != null
                && show.getShowDate() != null

                ? show.getShowDate().toString()

                : "TBD";


        String showTime =

                show != null
                && show.getStartTime() != null

                ? show.getStartTime().toString()

                : "TBD";


        String title =

                "Booking Created: "
                + booking.getBookingReference();


        String message =

                "Hello "
                + customer.getFullName()
                + ",\n\n"

                + "Your booking has been created successfully.\n\n"

                + "Booking Details:\n"

                + "• Reference: "
                + booking.getBookingReference()
                + "\n"

                + "• Movie: "
                + movieTitle
                + "\n"

                + "• Theatre: "
                + theatreName
                + "\n"

                + "• Location: "
                + theatreLocation
                + "\n"

                + "• Date & Time: "
                + showDate
                + " at "
                + showTime
                + "\n"

                + "• Reserved Seats: "
                + formatSeatCodes(
                        booking.getId()
                )
                + "\n"

                + "• Total Amount: LKR "
                + booking.getTotalAmount()
                + "\n"

                + "• Status: Awaiting Payment\n\n"

                + "Please complete payment "
                + "to confirm your booking.";


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
                NotificationType.BOOKING_CREATED
        );

        notification.setEmailStatus(
                EmailStatus.NOT_ATTEMPTED
        );


        notification =
                notificationRepository
                        .save(notification);


        return trySendEmail(

                notification,

                customer.getEmail(),

                title,

                message
        );
    }


    // =========================================================
    // TICKET GENERATED
    // =========================================================

    // Called after payment succeeds and the e-ticket
    // has been generated.
    @Transactional
    public Notification createTicketNotification(
            Ticket ticket) {


        Booking booking =
                ticket.getBooking();


        User customer =
                booking.getUser();


        ShowSchedule show =
                booking.getShowSchedule();


        String movieTitle =

                show != null
                && show.getMovie() != null

                ? show.getMovie().getTitle()

                : "Movie";


        String theatreName =

                show != null
                && show.getTheatre() != null

                ? show.getTheatre().getName()

                : "CineMax Theatre";


        String showDate =

                show != null
                && show.getShowDate() != null

                ? show.getShowDate().toString()

                : "TBD";


        String showTime =

                show != null
                && show.getStartTime() != null

                ? show.getStartTime().toString()

                : "TBD";


        String title =

                "Your CineMax E-Ticket is Ready - "
                + ticket.getTicketNumber();


        String message =

                "Hello "
                + customer.getFullName()
                + ",\n\n"

                + "Your payment was successful "
                + "and your e-ticket is ready.\n\n"

                + "Ticket Details:\n"

                + "• Ticket Number: "
                + ticket.getTicketNumber()
                + "\n"

                + "• Booking Reference: "
                + booking.getBookingReference()
                + "\n"

                + "• Movie: "
                + movieTitle
                + "\n"

                + "• Theatre: "
                + theatreName
                + "\n"

                + "• Date & Time: "
                + showDate
                + " at "
                + showTime
                + "\n"

                + "• Confirmed Seats: "
                + formatSeatCodes(
                        booking.getId()
                )
                + "\n"

                + "• Amount Paid: LKR "
                + booking.getTotalAmount()
                + "\n\n"

                + "Please present your ticket "
                + "at the cinema entrance.";


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


        return trySendEmail(

                notification,

                customer.getEmail(),

                title,

                message
        );
    }


    // =========================================================
    // CANCELLATION / REFUND
    // =========================================================

    /*
     IMPORTANT:

     Do NOT replace your current cancellation workflow.

     BookingCancellationService already calculates:

       Total Paid
       Refund Amount
       Amount Retained

     and sends those values here.

     Therefore this method keeps that feature working.
    */
    @Transactional
    public Notification createCancellationNotification(

            User user,

            String title,

            String message) {


        Notification notification =
                new Notification();


        notification.setUser(
                user
        );


        notification.setTitle(
                title
        );


        notification.setMessage(
                message
        );


        // Use different types for approval and rejection
        if (title != null
                && title
                    .toLowerCase()
                    .contains("reject")) {


            notification.setType(
                    NotificationType
                            .CANCELLATION_REJECTED
            );


        } else if (
                title != null
                && title
                    .toLowerCase()
                    .contains("approve")) {


            notification.setType(
                    NotificationType
                            .CANCELLATION_APPROVED
            );


        } else {


            notification.setType(
                    NotificationType
                            .BOOKING_CANCELLED
            );
        }


        notification.setEmailStatus(
                EmailStatus.NOT_ATTEMPTED
        );


        notification =
                notificationRepository
                        .save(notification);


        return trySendEmail(

                notification,

                user.getEmail(),

                title,

                message
        );
    }


    // =========================================================
    // GENERIC NOTIFICATION
    // =========================================================

    @Transactional
    public Notification createNotification(

            User user,

            NotificationType type,

            String title,

            String message) {


        Notification notification =
                new Notification();


        notification.setUser(
                user
        );

        notification.setType(
                type
        );

        notification.setTitle(
                title
        );

        notification.setMessage(
                message
        );

        notification.setEmailStatus(
                EmailStatus.NOT_ATTEMPTED
        );


        notification =
                notificationRepository
                        .save(notification);


        return trySendEmail(

                notification,

                user.getEmail(),

                title,

                message
        );
    }


    // =========================================================
    // ADMIN BROADCAST
    // =========================================================

    @Transactional
    public int sendAdminBroadcast(

            String title,

            String message,

            NotificationType type,

            String targetAudience,

            String specificUserEmail,

            String promoCode,

            boolean sendEmail) {


        List<User> recipients =
                new ArrayList<>();


        // -----------------------------------------
        // Send to one specific registered user
        // -----------------------------------------
        if ("SPECIFIC_USER"
                .equalsIgnoreCase(
                        targetAudience
                )) {


            if (specificUserEmail == null
                    || specificUserEmail
                        .isBlank()) {


                throw new IllegalArgumentException(
                        "Recipient email is required."
                );
            }


            User user =
                    userRepository

                        .findByEmailIgnoreCase(
                            specificUserEmail.trim()
                        )

                        .orElseThrow(
                            () ->
                                new IllegalArgumentException(
                                    "No CineMax user found with email: "
                                    + specificUserEmail
                                )
                        );


            recipients.add(
                    user
            );
        }


        // -----------------------------------------
        // Send to every enabled CineMax account
        // -----------------------------------------
        else if (
                "ALL_USERS"
                .equalsIgnoreCase(
                        targetAudience
                )) {


            recipients =

                    userRepository
                        .findAll()
                        .stream()

                        // Ignore disabled accounts
                        .filter(
                            User::isEnabled
                        )

                        .toList();
        }


        // -----------------------------------------
        // Default: send only to customers
        // -----------------------------------------
        else {


            recipients =

                    userRepository
                        .findByRoleAndEnabledTrue(
                            Role.CUSTOMER
                        );
        }


        if (recipients.isEmpty()) {

            return 0;
        }


        String fullMessage =
                message.trim();


        // Add promotional code when Admin supplied one
        if (promoCode != null
                && !promoCode.isBlank()) {


            fullMessage +=

                    "\n\nPromo / Discount Code: "
                    + promoCode
                        .trim()
                        .toUpperCase();
        }


        /*
         Create one unique broadcast ID.

         Every recipient receives a separate
         Notification database row but all rows
         share this same broadcastId.
        */
        String broadcastId =
                UUID.randomUUID()
                        .toString();


        int sentCount = 0;


        for (User recipient : recipients) {


            Notification notification =
                    new Notification();


            notification.setUser(
                    recipient
            );

            notification.setTitle(
                    title.trim()
            );

            notification.setMessage(
                    fullMessage
            );

            notification.setType(

                    type != null

                    ? type

                    : NotificationType.PROMOTION
            );


            notification.setBroadcastId(
                    broadcastId
            );


            notification.setEmailStatus(
                    EmailStatus.NOT_ATTEMPTED
            );


            notification =
                    notificationRepository
                            .save(notification);


            /*
             Email is OPTIONAL.

             In-app notification is created even
             when email is disabled.
            */
            if (sendEmail
                    && emailService
                        .isMailEnabled()) {


                boolean sent =
                        emailService.sendEmail(

                            recipient.getEmail(),

                            title.trim(),

                            "Hello "
                            + recipient.getFullName()
                            + ",\n\n"

                            + fullMessage

                            + "\n\n"
                            + "CineMax Movies Team"
                        );


                notification.setEmailStatus(

                        sent

                        ? EmailStatus.SENT

                        : EmailStatus.FAILED
                );


                notificationRepository
                        .save(notification);
            }


            sentCount++;
        }


        return sentCount;
    }


    // =========================================================
    // CUSTOMER NOTIFICATION OPERATIONS
    // =========================================================

    // Get all notifications belonging to this customer
    public List<Notification>
    getNotificationsForUser(
            Long userId) {


        return notificationRepository
                .findByUser_IdOrderByCreatedAtDesc(
                        userId
                );
    }


    // Customer marks one notification as read
    @Transactional
    public void markAsRead(

            Long notificationId,

            Long userId) {


        Notification notification =

                notificationRepository

                    .findById(
                        notificationId
                    )

                    .orElseThrow(
                        () ->
                            new IllegalArgumentException(
                                "Notification not found"
                            )
                    );


        // Security check:
        // Customer can only modify their own notification.
        if (!notification
                .getUser()
                .getId()
                .equals(userId)) {


            throw new IllegalArgumentException(
                    "Access denied"
            );
        }


        notification.setRead(
                true
        );


        notificationRepository
                .save(notification);
    }


    // Mark every notification belonging to
    // this customer as read.
    @Transactional
    public void markAllAsRead(
            Long userId) {


        List<Notification> unread =

                notificationRepository
                    .findByUser_IdAndReadFalse(
                        userId
                    );


        for (Notification notification : unread) {

            notification.setRead(
                    true
            );
        }


        notificationRepository
                .saveAll(unread);
    }


    // Customer permanently deletes one notification
    @Transactional
    public void deleteNotificationAsCustomer(

            Long notificationId,

            Long userId) {


        Notification notification =

                notificationRepository

                    .findById(
                        notificationId
                    )

                    .orElseThrow(
                        () ->
                            new IllegalArgumentException(
                                "Notification not found"
                            )
                    );


        // Prevent one customer deleting
        // another customer's notification.
        if (!notification
                .getUser()
                .getId()
                .equals(userId)) {


            throw new IllegalArgumentException(
                    "Access denied"
            );
        }


        notificationRepository
                .delete(notification);
    }


    // Customer clears their complete notification inbox
    @Transactional
    public int clearAllNotificationsForCustomer(
            Long userId) {


        return notificationRepository
                .deleteByUser_Id(
                    userId
                );
    }


    // =========================================================
    // ADMIN NOTIFICATION OPERATIONS
    // =========================================================

    public List<Notification>
    getRecentNotificationsForAdmin() {


        return notificationRepository
                .findTop50ByOrderByCreatedAtDesc();
    }


    public long
    getTotalNotificationsCount() {


        return notificationRepository
                .count();
    }


    public long
    getUnreadNotificationsCount() {


        return notificationRepository
                .countByReadFalse();
    }


    public boolean isMailEnabled() {

        return emailService
                .isMailEnabled();
    }


    // Admin can delete:
    //
    // deleteEntireBroadcast = false
    //      -> delete one notification
    //
    // deleteEntireBroadcast = true
    //      -> remove the broadcast from all customers
    @Transactional
    public int deleteNotificationAsAdmin(

            Long notificationId,

            boolean deleteEntireBroadcast) {


        Notification notification =

                notificationRepository

                    .findById(
                        notificationId
                    )

                    .orElseThrow(
                        () ->
                            new IllegalArgumentException(
                                "Notification not found"
                            )
                    );


        // Delete every copy of the same broadcast
        if (deleteEntireBroadcast) {


            if (notification.getBroadcastId()
                    != null

                    && !notification
                        .getBroadcastId()
                        .isBlank()) {


                return notificationRepository
                        .deleteByBroadcastId(
                            notification
                                .getBroadcastId()
                        );
            }


            /*
             Fallback for old promotion notifications
             which were created before broadcastId
             existed.
            */
            if (

                notification.getType()
                    == NotificationType.PROMOTION

                ||

                notification.getType()
                    == NotificationType.DISCOUNT

                ||

                notification.getType()
                    == NotificationType.ANNOUNCEMENT

                ||

                notification.getType()
                    == NotificationType.GENERAL

            ) {


                int deleted =

                        notificationRepository
                            .deleteByTitleAndMessageAndType(

                                notification.getTitle(),

                                notification.getMessage(),

                                notification.getType()
                            );


                return Math.max(
                        deleted,
                        1
                );
            }
        }


        // Otherwise delete only this one record
        notificationRepository
                .delete(notification);


        return 1;
    }


    // Admin permanently clears every notification
    // record from the system.
    @Transactional
    public long clearAllNotificationsAsAdmin() {


        long total =
                notificationRepository
                    .count();


        notificationRepository
                .deleteAll();


        return total;
    }
}
