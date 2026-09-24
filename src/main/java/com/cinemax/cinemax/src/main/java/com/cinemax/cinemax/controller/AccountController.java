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


    // Constructor injection supplies the dependencies this class needs
    public AccountController(
            UserService userService,
            BookingRepository bookingRepository,
            PaymentRepository paymentRepository,
            TicketService ticketService,
            BookingService bookingService,
            NotificationService notificationService) {

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


        // Open the matching HTML template
        return "account/booking-details";
    }


    // Handle a customer request to cancel their own booking
    @PostMapping("/bookings/{id}/cancel")
    public String cancelBooking(
            @PathVariable Long id,
            Principal principal) {

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

        try {

            bookingService
                    .cancelBookingByCustomer(
                        booking
                    );

            ticketService
                    .deactivateTicketForBooking(
                        booking.getId()
                    );

            return "redirect:/account/bookings/"
                    + id
                    + "?cancelled";

        } catch (IllegalArgumentException e) {

            return "redirect:/account/bookings/"
                    + id
                    + "?cancelError";
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


    private User currentUser(
            // Principal identifies the currently logged-in account
            Principal principal) {

        return userService
                .getUserByEmail(
                    principal.getName()
                );
    }
}
