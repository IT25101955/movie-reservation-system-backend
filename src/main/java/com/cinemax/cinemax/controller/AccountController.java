package com.cinemax.cinemax.controller;

import com.cinemax.cinemax.model.*;

import com.cinemax.cinemax.repository.*;

import com.cinemax.cinemax.service.*;

import org.springframework.stereotype.Controller;

import org.springframework.ui.Model;

import org.springframework.web.bind.annotation.*;

import java.security.Principal;

// Handle browser requests and select HTML pages
@Controller
@RequestMapping("/account")
public class AccountController {

    private final UserService
            userService;

    private final BookingRepository
            bookingRepository;

    private final PaymentRepository
            paymentRepository;

    private final TicketService
            ticketService;

    private final BookingService
            bookingService;

    private final NotificationService
            notificationService;

    private final BookingCancellationService
            bookingCancellationService;

    private final BookingCancellationRepository
            bookingCancellationRepository;

    private final RefundRepository
            refundRepository;

    // Constructor injection supplies the dependencies this class needs
    public AccountController(
            UserService userService,
            BookingRepository bookingRepository,
            PaymentRepository paymentRepository,
            TicketService ticketService,
            BookingService bookingService,
            NotificationService notificationService,
            BookingCancellationService bookingCancellationService,
            BookingCancellationRepository bookingCancellationRepository,
            RefundRepository refundRepository) {

        this.userService =
                userService;

        this.bookingRepository =
                bookingRepository;

        this.paymentRepository =
                paymentRepository;

        this.ticketService =
                ticketService;

        this.bookingService =
                bookingService;

        this.notificationService =
                notificationService;
                
        this.bookingCancellationService = bookingCancellationService;

        this.bookingCancellationRepository =
                bookingCancellationRepository;

        this.refundRepository =
                refundRepository;
    }


    // Handle a GET request to load a page or download a file
    @GetMapping("/bookings")
    public String bookingHistory(
            // Principal identifies the currently logged-in account
            Principal principal,
            Model model) {

        User user =
                currentUser(
                    principal
                );


        // Send these values to the HTML page
        model.addAttribute(
            "bookings",
            bookingRepository
                .findByUser_IdOrderByCreatedAtDesc(
                    user.getId()
                )
        );


        // Open the matching HTML template
        return "account/bookings";
    }


    // Handle a GET request to load a page or download a file
    @GetMapping("/bookings/{id}")
    public String bookingDetails(
            // Read this ID from the URL path
            @PathVariable Long id,
            // Principal identifies the currently logged-in account
            Principal principal,
            Model model) {

        User user =
                currentUser(
                    principal
                );


        Booking booking =
                bookingService
                    .getBookingForUser(
                        id,
                        user.getId()
                    );


        Ticket ticket =
                ticketService
                    .getTicketForBooking(
                        booking.getId()
                    );


        // Send these values to the HTML page
        model.addAttribute(
            "booking",
            booking
        );

        model.addAttribute(
            "canCancel",
            bookingService
                .canCustomerCancel(
                    booking
                )
        );

        model.addAttribute(
            "bookingSeats",
            bookingService
                .getBookingSeats(
                    booking.getId()
                )
        );

        model.addAttribute(
            "payments",
            paymentRepository
                .findByBooking_IdOrderByCreatedAtDesc(
                    booking.getId()
                )
        );

        model.addAttribute(
            "ticket",
            ticket
        );


        // Add cancellation and refund details for cancelled bookings
        bookingCancellationRepository
                .findByBookingId(booking.getId())
                .ifPresent(cancellation -> {
                    model.addAttribute(
                            "cancellation",
                            cancellation
                    );

                    refundRepository
                            .findByBooking_Id(
                                    booking.getId()
                            )
                            .ifPresent(refund ->
                                    model.addAttribute(
                                            "refund",
                                            refund
                                    )
                            );
                });


        // Open the matching HTML template
        return "account/booking-details";
    }


    // Handle a customer request to cancel their own booking
    @PostMapping("/bookings/{id}/cancel")
    public String requestCancelBooking(
            @PathVariable Long id,
            @RequestParam(value = "reason", defaultValue = "No reason provided") String reason,
            Principal principal) {

        User user = currentUser(principal);
        Booking booking = bookingService.getBookingForUser(id, user.getId());

        try {
            bookingCancellationService.requestCancellation(booking, reason);
            return "redirect:/account/bookings/" + id + "?cancelRequested";
        } catch (IllegalArgumentException e) {
            return "redirect:/account/bookings/" + id + "?cancelError=" + e.getMessage();
        }
    }


    // Handle a GET request to load a page or download a file
    @GetMapping("/notifications")
    public String notifications(
            // Principal identifies the currently logged-in account
            Principal principal,
            Model model) {

        User user =
                currentUser(
                    principal
                );


        // Send these values to the HTML page
        model.addAttribute(
            "notifications",
            notificationService
                .getNotificationsForUser(
                    user.getId()
                )
        );


        // Open the matching HTML template
        return "account/notifications";
    }


    // Handle a POST request that submits data or changes a record
    @PostMapping(
        "/notifications/{id}/read"
    )
    public String markNotificationRead(
            // Read this ID from the URL path
            @PathVariable Long id,
            // Principal identifies the currently logged-in account
            Principal principal) {

        User user =
                currentUser(
                    principal
                );


        notificationService
                .markAsRead(
                    id,
                    user.getId()
                );


        // Ask the browser to load the next page
        return "redirect:/account/notifications";
    }


    // =========================================================
    // MARK ALL NOTIFICATIONS AS READ
    // =========================================================

    @PostMapping("/notifications/mark-all-read")
    public String markAllNotificationsRead(
            Principal principal) {

        User user =
                currentUser(
                        principal
                );

        // Only this user's notification records are changed
        notificationService
                .markAllAsRead(
                        user.getId()
                );

        return "redirect:/account/notifications";
    }


    // =========================================================
    // DELETE ONE NOTIFICATION
    // =========================================================

    @PostMapping("/notifications/{id}/delete")
    public String deleteNotification(

            @PathVariable Long id,

            Principal principal) {

        User user =
                currentUser(
                        principal
                );

        /*
         NotificationService checks ownership.

         Therefore customer A cannot delete
         customer B's notification.
        */
        notificationService
                .deleteNotificationAsCustomer(

                        id,

                        user.getId()
                );

        return "redirect:/account/notifications";
    }


    // =========================================================
    // CLEAR ALL CUSTOMER NOTIFICATIONS
    // =========================================================

    @PostMapping("/notifications/clear-all")
    public String clearAllNotifications(
            Principal principal) {

        User user =
                currentUser(
                        principal
                );

        // Permanently delete this customer's
        // complete notification history.
        notificationService
                .clearAllNotificationsForCustomer(
                        user.getId()
                );

        return "redirect:/account/notifications";
    }


    private User currentUser(
            // Principal identifies the currently logged-in account
            Principal principal) {

        return userService
                .getUserByEmail(
                    principal.getName()
                );
    }
}
