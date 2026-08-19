package com.cinemax.cinemax.controller;

import com.cinemax.cinemax.model.*;
import com.cinemax.cinemax.dto.SeatSelectionDto;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.cinemax.cinemax.repository.*;

import com.cinemax.cinemax.service.*;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;

import java.time.LocalDateTime;
import java.time.ZoneId;

import java.math.BigDecimal;

import java.util.*;
import java.util.stream.Collectors;

// Handle browser requests and select HTML pages
@Controller
@RequestMapping("/booking")
public class BookingController {

    private final ShowScheduleService
            showService;

    private final TheatreService theatreService;

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

    private final ShowScheduleRepository
            showScheduleRepository;

    private final SeatRepository
            seatRepository;

    // Handles booking-created notifications
    private final NotificationService
            notificationService;

    private final ObjectMapper objectMapper = new ObjectMapper();


    // Constructor injection supplies the dependencies this class needs
    public BookingController(
            ShowScheduleService showService,
            TheatreService theatreService,
            SeatHoldService seatHoldService,
            BookingService bookingService,
            UserService userService,
            SeatHoldRepository seatHoldRepository,
            BookingSeatRepository bookingSeatRepository,
            ShowScheduleRepository showScheduleRepository,
            SeatRepository seatRepository,
            NotificationService notificationService) {

        this.showService =
                showService;

        this.theatreService =
            theatreService;

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

        this.showScheduleRepository =
                showScheduleRepository;

        this.seatRepository =
                seatRepository;

        // Save the notification service dependency
        this.notificationService =
                notificationService;

    }


    @GetMapping("/seats")
    public String showSeatSelection(
            @RequestParam("showId") Long showId,
            Principal principal,
            Model model) {

        ShowSchedule show =
                showScheduleRepository
                        .findById(showId)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Show not found"
                                )
                        );

        List<Seat> seats =
                seatRepository
                        .findByTheatreIdAndActiveTrueOrderByRowLabelAscSeatNumberAsc(
                                show.getTheatre().getId()
                        );


        Map<String, List<Seat>> seatsByRow =
                seats.stream()
                        .collect(
                                Collectors.groupingBy(
                                        Seat::getRowLabel,
                                        LinkedHashMap::new,
                                        Collectors.toList()
                                )
                        );

        seatHoldService.cleanupExpiredHolds();

        User user = null;
        if (principal != null) {
            user = userService.getUserByEmail(principal.getName());
        }

        Set<Long> bookedSeatIds = new HashSet<>();
        List<BookingSeat> bookedSeats = bookingSeatRepository.findByShowSchedule_Id(showId);
        for (BookingSeat bookingSeat : bookedSeats) {
            bookedSeatIds.add(bookingSeat.getSeat().getId());
        }

        Set<Long> otherHeldSeatIds = new HashSet<>();
        List<SeatHold> activeHolds = seatHoldRepository.findByShowSchedule_IdAndHeldUntilAfter(showId, LocalDateTime.now());
        for (SeatHold hold : activeHolds) {
            if (user == null || !hold.getUser().getId().equals(user.getId())) {
                otherHeldSeatIds.add(hold.getSeat().getId());
            }
        }

        model.addAttribute(
                "show",
                show
        );

        model.addAttribute(
                "seats",
                seats
        );

        model.addAttribute(
                "seatsByRow",
                seatsByRow
        );

        model.addAttribute(
                "bookedSeatIds",
                bookedSeatIds
        );

        model.addAttribute(
                "otherHeldSeatIds",
                otherHeldSeatIds
        );

        model.addAttribute(
                "adultPrice",
                show.getAdultPrice()
        );

        model.addAttribute(
                "childPrice",
                show.getChildPrice()
        );

        model.addAttribute(
                "vipPrice",
                show.getVipPrice()
        );


        return "booking/seats";
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
                theatreService
                    .getSeats(
                        show.getTheatre()
                            .getId()
                    );

                    Map<String, List<Seat>> seatsByRow = seats.stream()
                        .collect(Collectors.groupingBy(
                            seat -> seat.getRowLabel(),
                            LinkedHashMap::new,
                            Collectors.toList()
                        ));


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

        model.addAttribute("seats", seats);
        model.addAttribute("seatsByRow", seatsByRow);

        model.addAttribute(
            "bookedSeatIds",
            bookedSeatIds
        );

        model.addAttribute(
            "otherHeldSeatIds",
            otherHeldSeatIds
        );

        model.addAttribute("adultPrice", show.getAdultPrice());
        model.addAttribute("childPrice", show.getChildPrice());
        model.addAttribute("vipPrice", show.getVipPrice());


        // Open the matching HTML template
        return "booking/seats";
    }


    // Handle a POST request that submits data or changes a record
    @PostMapping("/show/{showId}/hold")
    // Validate the selection and temporarily hold available seats
    public String holdSeats(
            // Read this ID from the URL path
            @PathVariable Long showId,

            @RequestParam(required = false) String seatSelectionsJson,

            // Principal identifies the currently logged-in account
            Principal principal) {

        User user =
                userService
                    .getUserByEmail(
                        principal.getName()
                    );


        try {

            List<SeatSelectionDto> selections = objectMapper.readValue(
                    seatSelectionsJson,
                    new TypeReference<>() { });
            seatHoldService.holdSeats(showId, selections, user);

        } catch (
                Exception e) {

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


        BigDecimal total = BigDecimal.ZERO;
        for (SeatHold hold : holds) {
            if (hold.getSeat().getSeatCategory() == SeatCategory.VIP) {
                total = total.add(show.getVipPrice());
            } else if (hold.getTicketType() == TicketType.CHILD) {
                total = total.add(show.getChildPrice());
            } else {
                total = total.add(show.getAdultPrice());
            }
        }


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
            Principal principal,
            RedirectAttributes redirectAttributes) {

        User user =
                userService
                    .getUserByEmail(
                        principal.getName()
                    );


        try {

            // Create the customer's booking
            Booking booking =
                    bookingService
                        .confirmBooking(
                            showId,
                            user
                        );


            // Try to create the notification.
            //
            // A notification problem should NOT stop
            // the customer from proceeding to payment.
            try {

                notificationService
                        .createBookingNotification(
                            booking
                        );

            } catch (Exception notificationError) {

                System.err.println(
                        "Booking "
                        + booking.getBookingReference()
                        + " was created, but notification failed: "
                        + notificationError.getMessage()
                );
            }


            // Continue to the payment page
            return "redirect:/booking/pending/"
                    + booking.getId();

        } catch (
                IllegalArgumentException e) {

            System.err.println("BOOKING CONFIRMATION FAILED");
            System.err.println("User ID: " + user.getId());
            System.err.println("Show ID: " + showId);
            System.err.println("Reason: " + e.getMessage());

            redirectAttributes.addFlashAttribute(
                    "bookingError",
                    e.getMessage()
            );

            return "redirect:/booking/show/"
                    + showId
                    + "/seats";
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
