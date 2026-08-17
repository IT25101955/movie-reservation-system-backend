package com.cinemax.cinemax.service;

import com.cinemax.cinemax.dto.PaymentRequest;
import com.cinemax.cinemax.designpattern.TicketSubject;
import com.cinemax.cinemax.designpattern.PaymentGatewayContext;

import com.cinemax.cinemax.model.*;

import com.cinemax.cinemax.repository.PaymentRepository;

import jakarta.transaction.Transactional;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

import java.util.Optional;
import java.util.UUID;


// Spring creates this service to handle business rules
@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;

    private final BookingService bookingService;

    private final TicketService ticketService;

    // DESIGN PATTERN: Strategy context for payment processing
    private final PaymentGatewayContext paymentGatewayContext;

    // DESIGN PATTERN: Observer subject for ticket-generation events
    private final TicketSubject ticketSubject;

    // Constructor injection supplies the dependencies this class needs
    public PaymentService(
            PaymentRepository paymentRepository,
            BookingService bookingService,
            TicketService ticketService,
            PaymentGatewayContext paymentGatewayContext,
            TicketSubject ticketSubject) {

        this.paymentRepository =
                paymentRepository;

        this.bookingService = bookingService;

        this.ticketService = ticketService;

        this.paymentGatewayContext = paymentGatewayContext;

        this.ticketSubject = ticketSubject;
    }


    // Run these database steps together; runtime failures roll them back
    @Transactional
    // Simulate payment and update the booking, ticket and notification
    public Payment processPayment(

            Long bookingId,

            Long userId,

            PaymentRequest request) {


        // --------------------------------------------------------
        // LOCK BOOKING
        // --------------------------------------------------------

        Booking booking =
                bookingService
                        .getBookingForPaymentUpdate(
                                bookingId,
                                userId
                        );


        // --------------------------------------------------------
        // MAKE SURE PAYMENT HAS NOT ALREADY BEEN PROCESSED
        // --------------------------------------------------------

        if (booking.getStatus()
                != BookingStatus.PENDING_PAYMENT) {

            throw new IllegalArgumentException(
                    "This booking is no longer awaiting payment."
            );
        }


        Payment payment =
                new Payment();


        payment.setBooking(
            booking
        );

        payment.setAmount(
            booking.getTotalAmount()
        );

        payment.setPaymentMethod(
            PaymentMethod.CARD
        );

        payment.setTransactionReference(
            generateTransactionReference()
        );


        String cardNumber =
                request.getCardNumber();


        payment.setCardLastFour(
            cardNumber.substring(
                cardNumber.length() - 4
            )
        );


        // Strategy Pattern: delegate the sandbox gateway rule to a strategy
        PaymentStatus result =
                paymentGatewayContext.process(
                    cardNumber
                );


        payment.setStatus(result);

        if (result == PaymentStatus.SUCCESS) {

            payment.setPaidAt(
                LocalDateTime.now()
            );


            Booking confirmedBooking =
                    bookingService
                        .markBookingConfirmed(
                            booking
                        );


            Ticket ticket =
                    ticketService
                        .generateTicket(
                            confirmedBooking
                        );


            // Observer Pattern: notify every registered ticket observer
            ticketSubject
                    .notifyObservers(
                        ticket
                    );

        } else if (
            result == PaymentStatus.FAILED) {

            bookingService
                .cancelBooking(
                    booking
                );
        }


        return paymentRepository
                .save(payment);
    }


    // Payment gateway rules are now separated into Strategy classes.
    // PaymentService only asks the Strategy Context to process the card.

    // Create a short random payment reference
    private String
    generateTransactionReference() {

        return "PAY-"
            + UUID.randomUUID()
                .toString()
                .substring(0, 10)
                .toUpperCase();
    }


    // Search the database and report when the record is missing
    public Payment getPayment(
            Long id) {

        // Optional holds the payment when the database finds a match
        Optional<Payment> payment = paymentRepository.findById(id);

        // Check whether the database search found a record
        if (payment.isPresent()) {
            return payment.get();
        }

        // Stop if the requested record does not exist or is inaccessible
        throw new IllegalArgumentException("Payment not found");
    }

    // Load a payment only when its booking belongs to this customer
    public Payment getPaymentForUser(
            Long paymentId,
            Long userId) {

        // Optional holds the payment when the database finds a match
        Optional<Payment> payment =
                paymentRepository
                        .findByIdAndBooking_User_Id(
                                paymentId,
                                userId
                        );

        // Check whether the database search found a record
        if (payment.isPresent()) {
            return payment.get();
        }

        // Stop if the requested record does not exist or is inaccessible
        throw new IllegalArgumentException(
                "Payment not found or access denied."
        );
    }

    // Run these database steps together; runtime failures roll them back
    @Transactional
    // Resolve a pending payment as successful or failed
    public Payment resolvePendingPayment(
            Long paymentId,
            PaymentStatus newStatus) {

        Payment payment = getPayment(paymentId);

        if (payment.getStatus() != PaymentStatus.PENDING) {
            throw new IllegalArgumentException(
                    "Only pending payments can be resolved."
            );
        }

        if (newStatus != PaymentStatus.SUCCESS &&
            newStatus != PaymentStatus.FAILED) {
            throw new IllegalArgumentException(
                    "A pending payment can only be marked successful or failed."
            );
        }

        Booking booking = payment.getBooking();

        if (newStatus == PaymentStatus.SUCCESS) {

            payment.setStatus(PaymentStatus.SUCCESS);
            payment.setPaidAt(LocalDateTime.now());

            Booking confirmed =
                    bookingService.markBookingConfirmed(booking);

            Ticket ticket =
                    ticketService.generateTicket(confirmed);

            ticketSubject.notifyObservers(ticket);

        } else {

            payment.setStatus(PaymentStatus.FAILED);
            bookingService.cancelBooking(booking);
        }

        return paymentRepository.save(payment);
    }
}
