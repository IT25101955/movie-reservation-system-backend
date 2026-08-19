package com.cinemax.cinemax.controller;

import com.cinemax.cinemax.model.*;

import com.cinemax.cinemax.service.*;

import org.springframework.http.*;

import org.springframework.stereotype.Controller;

import org.springframework.ui.Model;

import org.springframework.web.bind.annotation.*;

import java.security.Principal;

// Handle browser requests and select HTML pages
@Controller
@RequestMapping("/tickets/{ticketId}")
public class TicketController {

    private final TicketService
            ticketService;

    private final TicketPdfService
            ticketPdfService;

    private final UserService
            userService;

    private final BookingService
            bookingService;


    // Constructor injection supplies the dependencies this class needs
    public TicketController(
            TicketService ticketService,
            TicketPdfService ticketPdfService,
            UserService userService,
            BookingService bookingService) {

        this.ticketService =
                ticketService;

        this.ticketPdfService =
                ticketPdfService;

        this.userService =
                userService;

        this.bookingService =
                bookingService;
    }


    // Handle a GET request to load a page or download a file
    @GetMapping
    public String viewTicket(
            // Read this ID from the URL path
            @PathVariable Long ticketId,
            // Principal identifies the currently logged-in account
            Principal principal,
            Model model) {

        User user =
                userService
                    .getUserByEmail(
                        principal.getName()
                    );


        Ticket ticket =
                ticketService
                    .getTicketForUser(
                        ticketId,
                        user.getId()
                    );


        // Send these values to the HTML page
        model.addAttribute(
            "ticket",
            ticket
        );

        model.addAttribute(
            "bookingSeats",
            bookingService
                .getBookingSeats(
                    ticket
                        .getBooking()
                        .getId()
                )
        );


        // Open the matching HTML template
        return "tickets/view";
    }


    // Handle a GET request to load a page or download a file
    @GetMapping("/download")
    public ResponseEntity<byte[]>
    downloadTicket(
            // Read this ID from the URL path
            @PathVariable Long ticketId,
            // Principal identifies the currently logged-in account
            Principal principal) {

        User user =
                userService
                    .getUserByEmail(
                        principal.getName()
                    );


        Ticket ticket =
                ticketService
                    .getTicketForUser(
                        ticketId,
                        user.getId()
                    );


        byte[] pdf =
                ticketPdfService
                    .generateTicketPdf(
                        ticket
                    );


        return ResponseEntity
                .ok()
                .header(
                    HttpHeaders
                        .CONTENT_DISPOSITION,

                    "attachment; filename=\""
                    + ticket.getTicketNumber()
                    + ".pdf\""
                )
                .contentType(
                    MediaType.APPLICATION_PDF
                )
                .body(pdf);
    }
}
