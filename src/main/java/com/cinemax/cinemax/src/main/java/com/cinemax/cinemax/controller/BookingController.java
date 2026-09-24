package com.cinemax.cinemax.controller;

import com.cinemax.cinemax.model.*;

import com.cinemax.cinemax.repository.*;

import com.cinemax.cinemax.service.*;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;

import org.springframework.web.bind.annotation.*;

import java.security.Principal;

import java.time.LocalDateTime;
import java.time.ZoneId;

import java.math.BigDecimal;

import java.util.*;

// Handle browser requests and select HTML pages
@Controller
@RequestMapping("/booking")
public class BookingController {

    private final ShowScheduleService
            showService;

    private final ScreenService
            screenService;

    private final SeatHoldService
            seatHoldService;

    private final BookingService
            bookingService;

    private final UserService
            userService;

    private final SeatHoldRepository
            seatHoldRepository;

    private final BookingSeatRepository
            bookingSeatRepository;


    // Constructor injection supplies the dependencies this class needs
    public BookingController(
            ShowScheduleService showService,
            ScreenService screenService,
            SeatHoldService seatHoldService,
            BookingService bookingService,
            UserService userService,
            SeatHoldRepository seatHoldRepository,
            BookingSeatRepository bookingSeatRepository) {

        this.showService =
                showService;

        this.screenService =
                screenService;

        this.seatHoldService =
                seatHoldService;

        this.bookingService =
                bookingService;

        this.userService =
                userService;

        this.seatHoldRepository =
                seatHoldRepository;

        this.bookingSeatRepository =
                bookingSeatRepository;
    }


    // Handle a GET request to load a page or download a file
    @GetMapping("/show/{showId}/seats")
    public String seatMap(
            // Read this ID from the URL path
            @PathVariable Long showId,
            // Principal identifies the currently logged-in account
            Principal principal,
            Model model) {

        seatHoldService
                .cleanupExpiredHolds();


        User user =
                userService
                    .getUserByEmail(
                        principal.getName()
                    );


        ShowSchedule show =
                showService
                    .getActiveShowById(showId);


        List<Seat> seats =
                screenService
                    .getSeats(
                        show.getScreen()
                            .getId()
                    );


        // Collect the IDs of seats that are already booked
        Set<Long> bookedSeatIds = new HashSet<>();

        List<BookingSeat> bookedSeats =
                bookingSeatRepository.findByShowSchedule_Id(showId);

        for (BookingSeat bookingSeat : bookedSeats) {
            bookedSeatIds.add(bookingSeat.getSeat().getId());
        }


        List<SeatHold> activeHolds =
                seatHoldRepository
                    .findByShowSchedule_IdAndHeldUntilAfter(
                        showId,
                        LocalDateTime.now()
                    );


        // Collect seats held by other customers
        Set<Long> otherHeldSeatIds = new HashSet<>();

        for (SeatHold hold : activeHolds) {
            Long holdUserId = hold.getUser().getId();

            if (!holdUserId.equals(user.getId())) {
                otherHeldSeatIds.add(hold.getSeat().getId());
            }
        }


        // Send these values to the HTML page
        model.addAttribute(
            "show",
            show
        );

        model.addAttribute(
            "seats",
            seats
        );

        model.addAttribute(
            "bookedSeatIds",
            bookedSeatIds
        );

        model.addAttribute(
            "otherHeldSeatIds",
            otherHeldSeatIds
        );


        // Open the matching HTML template
        return "booking/seats";
    }


    // Handle a POST request that submits data or changes a record
    @PostMapping("/show/{showId}/hold")
    // Validate the selection and temporarily hold available seats
    public String holdSeats(
            // Read this ID from the URL path
            @PathVariable Long showId,

            @RequestParam(
                required = false
            )
            List<Long> selectedSeatIds,

            // Principal identifies the currently logged-in account
            Principal principal) {

        User user =
                userService
                    .getUserByEmail(
                        principal.getName()
                    );


        try {

            seatHoldService.holdSeats(
                showId,
                selectedSeatIds,
                user
            );

        } catch (
                IllegalArgumentException e) {

            // Ask the browser to load the next page
            return "redirect:/booking/show/"
                    + showId
                    + "/seats?error";
        }


        // Ask the browser to load the next page
        return "redirect:/booking/confirm?showId="
                + showId;
    }


    // Handle a GET request to load a page or download a file
    @GetMapping("/confirm")
    public String confirmation(
            @RequestParam Long showId,
            // Principal identifies the currently logged-in account
            Principal principal,
            Model model) {

        User user =
                userService
                    .getUserByEmail(
                        principal.getName()
                    );


        List<SeatHold> holds =
                seatHoldService
                    .getUserActiveHolds(
                        user.getId(),
                        showId
                    );


        if (holds.isEmpty()) {

            // Ask the browser to load the next page
            return "redirect:/booking/show/"
                    + showId
                    + "/seats?expired";
        }


        ShowSchedule show =
                showService
                    .getActiveShowById(showId);


        BigDecimal total =
                show.getTicketPrice()
                    .multiply(
                        BigDecimal.valueOf(
                            holds.size()
                        )
                    );


        LocalDateTime heldUntil =
                holds.get(0)
                    .getHeldUntil();


        long expiryMillis =
                heldUntil
                    .atZone(
                        ZoneId.systemDefault()
                    )
                    .toInstant()
                    .toEpochMilli();


        // Send these values to the HTML page
        model.addAttribute(
            "show",
            show
        );

        model.addAttribute(
            "holds",
            holds
        );

        model.addAttribute(
            "total",
            total
        );

        model.addAttribute(
            "expiryMillis",
            expiryMillis
        );


        // Open the matching HTML template
        return "booking/confirm";
    }


    // Handle a POST request that submits data or changes a record
    @PostMapping("/confirm")
    // Convert held seats into a booking awaiting payment
    public String confirmBooking(
            @RequestParam Long showId,
            // Principal identifies the currently logged-in account
            Principal principal) {

        User user =
                userService
                    .getUserByEmail(
                        principal.getName()
                    );


        try {

            Booking booking =
                    bookingService
                        .confirmBooking(
                            showId,
                            user
                        );


            // Ask the browser to load the next page
            return "redirect:/booking/pending/"
                    + booking.getId();

        } catch (
                IllegalArgumentException e) {

            // Ask the browser to load the next page
            return "redirect:/booking/show/"
                    + showId
                    + "/seats?expired";
        }
    }


    // Handle a POST request that submits data or changes a record
    @PostMapping("/cancel-hold")
    public String cancelHold(
            @RequestParam Long showId,
            // Principal identifies the currently logged-in account
            Principal principal) {

        User user =
                userService
                    .getUserByEmail(
                        principal.getName()
                    );


        seatHoldService
                .cancelHolds(
                    user.getId(),
                    showId
                );


        // Ask the browser to load the next page
        return "redirect:/movies";
    }


    // Handle a GET request to load a page or download a file
    @GetMapping("/pending/{bookingId}")
    public String pendingBooking(
            // Read this ID from the URL path
            @PathVariable Long bookingId,
            // Principal identifies the currently logged-in account
            Principal principal,
            Model model) {

        User user = userService
                .getUserByEmail(principal.getName());

        Booking booking = bookingService
                .getBookingForUser(bookingId, user.getId());

        // Send these values to the HTML page
        model.addAttribute(
            "booking",
            booking
        );

        model.addAttribute(
            "bookingSeats",
            bookingService
                .getBookingSeats(
                    bookingId
                )
        );

        // Open the matching HTML template
        return "booking/pending";
    }
}
