package com.cinemax.cinemax.service;

import com.cinemax.cinemax.designpattern.SeatPricingContext;
import com.cinemax.cinemax.model.*;
import com.cinemax.cinemax.repository.*;

import jakarta.transaction.Transactional;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

// Spring creates this service to handle business rules
@Service
public class BookingService {

    private final BookingRepository
            bookingRepository;

    private final BookingSeatRepository
            bookingSeatRepository;

    private final SeatHoldRepository
            seatHoldRepository;

    private final ShowScheduleService
            showScheduleService;

    // DESIGN PATTERN: Strategy context for seat pricing
    private final SeatPricingContext
            seatPricingContext;


    // Constructor injection supplies the dependencies this class needs
    public BookingService(
            BookingRepository bookingRepository,
            BookingSeatRepository bookingSeatRepository,
            SeatHoldRepository seatHoldRepository,
            ShowScheduleService showScheduleService,
            SeatPricingContext seatPricingContext) {

        this.bookingRepository =
                bookingRepository;

        this.bookingSeatRepository =
                bookingSeatRepository;

        this.seatHoldRepository =
                seatHoldRepository;

        this.showScheduleService =
                showScheduleService;

        this.seatPricingContext =
                seatPricingContext;
    }


    // Run these database steps together; runtime failures roll them back
    @Transactional
    // Convert held seats into a booking awaiting payment
    public Booking confirmBooking(
            Long showId,
            User user) {

        LocalDateTime now = LocalDateTime.now();

        List<SeatHold> holds =
                seatHoldRepository
                    .findByUser_IdAndShowSchedule_IdAndHeldUntilAfter(
                        user.getId(),
                        showId,
                        now
                    );

        System.out.println("====================================");
        System.out.println("CONFIRMING BOOKING");
        System.out.println("User ID: " + user.getId());
        System.out.println("Show ID: " + showId);
        System.out.println("Current time: " + now);
        System.out.println("Active seat holds found: " + holds.size());
        for (SeatHold hold : holds) {
            System.out.println(
                    "Seat: " + hold.getSeat().getSeatCode()
                            + " | Hold expires: " + hold.getHeldUntil()
            );
        }
        System.out.println("====================================");


        if (holds.isEmpty()) {

            throw new IllegalArgumentException(
                "Your seat hold has expired. Please select your seats again."
            );
        }


        ShowSchedule show =
                showScheduleService
                    .getShowForBooking(showId);


        BigDecimal total = BigDecimal.ZERO;
        for (SeatHold hold : holds) {
            total = total.add(priceFor(show, hold));
        }


        Booking booking =
                new Booking();


        booking.setBookingReference(
            generateBookingReference()
        );

        booking.setUser(user);

        booking.setShowSchedule(show);

        booking.setTotalAmount(total);

        booking.setStatus(
            BookingStatus.PENDING_PAYMENT
        );


        Booking savedBooking =
                bookingRepository.save(
                    booking
                );


        try {

            for (SeatHold hold : holds) {

                BookingSeat bookingSeat =
                        new BookingSeat();

                bookingSeat.setBooking(
                    savedBooking
                );

                bookingSeat.setShowSchedule(
                    show
                );

                bookingSeat.setSeat(
                    hold.getSeat()
                );

                bookingSeat.setSeatPrice(priceFor(show, hold));


                bookingSeatRepository
                        .save(bookingSeat);
            }


            // Send pending database changes now so constraint errors occur here
            bookingSeatRepository.flush();

        } catch (
                DataIntegrityViolationException e) {

            throw new IllegalArgumentException(
                "One of your selected seats is no longer available."
            );
        }


        seatHoldRepository
                .deleteByUser_IdAndShowSchedule_Id(
                    user.getId(),
                    showId
                );

        seatHoldRepository.flush();


        return savedBooking;
    }

    private BigDecimal priceFor(ShowSchedule show, SeatHold hold) {
        // Strategy Pattern: choose VIP, child or adult pricing at runtime.
        return seatPricingContext.calculatePrice(show, hold);
    }


    // Create a short random reference for the booking
    private String
    generateBookingReference() {

        return "CMX-"
                + UUID.randomUUID()
                    .toString()
                    .substring(0, 8)
                    .toUpperCase();
    }


    // Search the database and report when the record is missing
    public Booking getBooking(Long id) {

        // Search for the booking using its ID
        Optional<Booking> booking = bookingRepository.findById(id);

        // Check whether the database search found a record
        if (booking.isPresent()) {
            return booking.get();
        }

        // Stop if the requested record does not exist or is inaccessible
        throw new IllegalArgumentException("Booking not found");
    }


    // Get all seats for a booking
    public List<BookingSeat>
    getBookingSeats(Long bookingId) {

        return bookingSeatRepository
                .findByBooking_Id(
                    bookingId
                );
    }

    // Load a booking only when it belongs to this customer
    public Booking getBookingForUser(
            Long bookingId,
            Long userId) {

        // Search using both IDs so customers only see their own booking
        Optional<Booking> booking =
                bookingRepository.findByIdAndUser_Id(
                        bookingId,
                        userId
                );

        // Check whether the database search found a record
        if (booking.isPresent()) {
            return booking.get();
        }

        // Stop if the requested record does not exist or is inaccessible
        throw new IllegalArgumentException(
                "Booking not found or access denied"
        );
    }

    // ============================================================
    // GET BOOKING AND LOCK IT FOR PAYMENT
    // ============================================================
    //
    // This method must be called inside the payment transaction.
    //
    // The database lock remains active until PaymentService
    // finishes the complete payment transaction.
    //
    @Transactional
    public Booking getBookingForPaymentUpdate(

            Long bookingId,

            Long userId) {


        return bookingRepository
                .findForPaymentUpdate(
                        bookingId,
                        userId
                )
                .orElseThrow(
                        () ->
                                new IllegalArgumentException(
                                        "Booking not found or access denied."
                                )
                );
    }

        // Check whether a customer can cancel this booking
        public boolean canCustomerCancel(
            Booking booking) {

        if (booking.getStatus()
            == BookingStatus.CANCELLED) {

            return false;
        }

        ShowSchedule show =
            booking.getShowSchedule();

        if (show == null) {
            return false;
        }

        LocalDateTime showDateTime =
            LocalDateTime.of(
                show.getShowDate(),
                show.getStartTime()
            );

        return showDateTime.isAfter(
            LocalDateTime.now()
        );
        }

        // Cancel a booking only when its show has not started
        @Transactional
        public void cancelBookingByCustomer(
            Booking booking) {

        if (booking.getStatus()
            == BookingStatus.CANCELLED) {

            throw new IllegalArgumentException(
                "This booking is already cancelled."
            );
        }

        ShowSchedule show =
            booking.getShowSchedule();

        if (show == null) {
            throw new IllegalArgumentException(
                "This booking cannot be cancelled because its show is unavailable."
            );
        }

        LocalDateTime showDateTime =
            LocalDateTime.of(
                show.getShowDate(),
                show.getStartTime()
            );

        if (!showDateTime.isAfter(
            LocalDateTime.now())) {

            throw new IllegalArgumentException(
                "This booking cannot be cancelled because the show has already started."
            );
        }

        booking.setStatus(
            BookingStatus.CANCELLED
        );

        bookingRepository.save(
            booking
        );

        List<BookingSeat> bookingSeats =
            bookingSeatRepository
                .findByBooking_Id(
                    booking.getId()
                );

        bookingSeatRepository.deleteAll(
            bookingSeats
        );
        }

    // Cancel the booking and free its seats
    // Run these database steps together; runtime failures roll them back
    @Transactional
    // Cancel the booking and release its reserved seats
    public void cancelBooking(
            Booking booking) {

        booking.setStatus(
            BookingStatus.CANCELLED
        );

        bookingRepository.save(booking);

        List<BookingSeat> seats =
                bookingSeatRepository
                    .findByBooking_Id(
                        booking.getId()
                    );

        bookingSeatRepository
                .deleteAll(seats);
    }

    // Run these database steps together; runtime failures roll them back
    @Transactional
    // Mark the booking confirmed after successful payment
    public Booking markBookingConfirmed(
            Booking booking) {

        booking.setStatus(
            BookingStatus.CONFIRMED
        );

        return bookingRepository.save(
                booking
        );
    }

    // Load all bookings with the newest first
    public List<Booking>
    getAllBookings() {

        return bookingRepository
                .findAllByOrderByCreatedAtDesc();
    }

    // Load bookings matching the selected status
    public List<Booking>
    getBookingsByStatus(
            BookingStatus status) {

        return bookingRepository
                .findByStatusOrderByCreatedAtDesc(
                        status
                );
    }
}
