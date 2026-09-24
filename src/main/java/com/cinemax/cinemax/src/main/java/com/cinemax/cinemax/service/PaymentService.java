package com.cinemax.cinemax.service;

import com.cinemax.cinemax.dto.PaymentRequest;

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

    private final NotificationService notificationService;

    // Constructor injection supplies the dependencies this class needs
    public PaymentService(
            PaymentRepository paymentRepository,
            BookingService bookingService,
            TicketService ticketService,
            NotificationService notificationService) {

        this.paymentRepository =
                paymentRepository;

        this.bookingService = bookingService;

        this.ticketService = ticketService;

        this.notificationService = notificationService;
    }


    // Run these database steps together; runtime failures roll them back
    @Transactional
    // Simulate payment and update the booking, ticket and notification
    public Payment processPayment(
            Booking booking,
            PaymentRequest request) {

        if (booking.getStatus()
                != BookingStatus.PENDING_PAYMENT) {

            throw new IllegalArgumentException(
                "This booking is not awaiting payment."
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


        PaymentStatus result =
                simulateGateway(
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


            notificationService
                    .createTicketNotification(
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


    // Return a demonstration payment result using the test card number
    private PaymentStatus simulateGateway(
            String cardNumber) {

        /*
         CineMax sandbox cards:
          4242424242424242  SUCCESS
          4000000000000002  FAILED
          4000000000000077  PENDING
         */

        if (cardNumber.equals(
                "4242424242424242")) {

            return PaymentStatus.SUCCESS;
        }



        if (cardNumber.equals(
                "4000000000000000")) {

            return PaymentStatus.PENDING;
        }


        return PaymentStatus.FAILED;
    }


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

            notificationService.createTicketNotification(ticket);

        } else {

            payment.setStatus(PaymentStatus.FAILED);
            bookingService.cancelBooking(booking);
        }

        return paymentRepository.save(payment);
    }
}
