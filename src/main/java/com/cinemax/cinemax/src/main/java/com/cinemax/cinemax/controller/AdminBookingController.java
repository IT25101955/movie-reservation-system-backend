package com.cinemax.cinemax.controller;

import com.cinemax.cinemax.model.Booking;
import com.cinemax.cinemax.model.BookingStatus;
import com.cinemax.cinemax.repository.PaymentRepository;
import com.cinemax.cinemax.service.BookingService;
import com.cinemax.cinemax.service.TicketService;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// Handle browser requests and select HTML pages
@Controller
@RequestMapping("/admin/bookings")
public class AdminBookingController {

    private final BookingService bookingService;
    private final PaymentRepository paymentRepository;
    private final TicketService ticketService;

    // Constructor injection supplies the dependencies this class needs
    public AdminBookingController(
            BookingService bookingService,
            PaymentRepository paymentRepository,
            TicketService ticketService) {

        this.bookingService = bookingService;
        this.paymentRepository = paymentRepository;
        this.ticketService = ticketService;
    }

    // Handle a GET request to load a page or download a file
    @GetMapping
    public String bookings(
            @RequestParam(required = false) BookingStatus status,
            Model model) {

        List<Booking> bookings = status == null
                ? bookingService.getAllBookings()
                : bookingService.getBookingsByStatus(status);

        // Send these values to the HTML page
        model.addAttribute("bookings", bookings);
        model.addAttribute("statuses", BookingStatus.values());
        model.addAttribute("selectedStatus", status);

        // Open the matching HTML template
        return "admin/bookings/list";
    }

    // Handle a GET request to load a page or download a file
    @GetMapping("/{id}")
    public String details(
            // Read this ID from the URL path
            @PathVariable Long id,
            Model model) {

        Booking booking = bookingService.getBooking(id);

        // Send these values to the HTML page
        model.addAttribute("booking", booking);
        model.addAttribute("bookingSeats", bookingService.getBookingSeats(id));
        model.addAttribute(
                "payments",
                paymentRepository.findByBooking_IdOrderByCreatedAtDesc(id)
        );
        model.addAttribute("ticket", ticketService.getTicketForBooking(id));

        // Open the matching HTML template
        return "admin/bookings/details";
    }

    // Handle a POST request that submits data or changes a record
    @PostMapping("/{id}/cancel")
    // Cancel the booking and release its reserved seats
    public String cancelBooking(@PathVariable Long id) {

        Booking booking = bookingService.getBooking(id);

        bookingService.cancelBooking(booking);
        ticketService.deactivateTicketForBooking(id);

        // Ask the browser to load the next page
        return "redirect:/admin/bookings/" + id + "?cancelled";
    }
}
