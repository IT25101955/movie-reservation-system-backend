package com.cinemax.cinemax.controller;

import com.cinemax.cinemax.model.BookingStatus;
import com.cinemax.cinemax.model.User;
import com.cinemax.cinemax.repository.BookingRepository;
import com.cinemax.cinemax.repository.NotificationRepository;
import com.cinemax.cinemax.repository.TicketRepository;
import com.cinemax.cinemax.service.UserService;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.security.Principal;

// Handle browser requests and select HTML pages
@Controller
@RequestMapping("/customer")
public class CustomerDashboardController {

    private final UserService userService;
    private final BookingRepository bookingRepository;
    private final NotificationRepository notificationRepository;
    private final TicketRepository ticketRepository;

    // Constructor injection supplies the dependencies this class needs
    public CustomerDashboardController(
            UserService userService,
            BookingRepository bookingRepository,
            NotificationRepository notificationRepository,
            TicketRepository ticketRepository) {

        this.userService = userService;
        this.bookingRepository = bookingRepository;
        this.notificationRepository = notificationRepository;
        this.ticketRepository = ticketRepository;
    }

    // Handle a GET request to load a page or download a file
    @GetMapping("/dashboard")
    public String dashboard(
            // Principal identifies the currently logged-in account
            Principal principal,
            Model model) {

        User user = userService.getUserByEmail(
                principal.getName()
        );

        // Send these values to the HTML page
        model.addAttribute("user", user);
        model.addAttribute(
                "totalBookings",
                bookingRepository.countByUser_Id(user.getId())
        );
        model.addAttribute(
                "confirmedBookings",
                bookingRepository.countByUser_IdAndStatus(
                        user.getId(),
                        BookingStatus.CONFIRMED
                )
        );
        model.addAttribute(
                "activeTickets",
                ticketRepository.countByBooking_User_IdAndActiveTrue(
                        user.getId()
                )
        );
        model.addAttribute(
                "unreadNotifications",
                notificationRepository.countByUser_IdAndReadFalse(
                        user.getId()
                )
        );

        // Open the matching HTML template
        return "customer/dashboard";
    }
}
