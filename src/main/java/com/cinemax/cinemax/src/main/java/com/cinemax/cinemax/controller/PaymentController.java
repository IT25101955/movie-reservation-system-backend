package com.cinemax.cinemax.controller;

import com.cinemax.cinemax.dto.PaymentRequest;

import com.cinemax.cinemax.model.*;

import com.cinemax.cinemax.service.*;

import jakarta.validation.Valid;

import org.springframework.stereotype.Controller;

import org.springframework.ui.Model;

import org.springframework.validation.BindingResult;

import org.springframework.web.bind.annotation.*;

import java.security.Principal;

// Handle browser requests and select HTML pages
@Controller
@RequestMapping("/payment")
public class PaymentController {

    private final PaymentService
            paymentService;

    private final BookingService
            bookingService;

    private final UserService
            userService;


    // Constructor injection supplies the dependencies this class needs
    public PaymentController(
            PaymentService paymentService,
            BookingService bookingService,
            UserService userService) {

        this.paymentService =
                paymentService;

        this.bookingService =
                bookingService;

        this.userService =
                userService;
    }


    // Handle a GET request to load a page or download a file
    @GetMapping("/{bookingId}")
    public String paymentPage(
            // Read this ID from the URL path
            @PathVariable Long bookingId,
            // Principal identifies the currently logged-in account
            Principal principal,
            Model model) {

        User user =
                userService
                    .getUserByEmail(
                        principal.getName()
                    );


        Booking booking =
                bookingService
                    .getBookingForUser(
                        bookingId,
                        user.getId()
                    );


        if (booking.getStatus()
                != BookingStatus.PENDING_PAYMENT) {

            // Ask the browser to load the next page
            return "redirect:/movies";
        }


        // Send these values to the HTML page
        model.addAttribute(
            "booking",
            booking
        );

        model.addAttribute(
            "paymentRequest",
            new PaymentRequest()
        );


        // Open the matching HTML template
        return "payment/form";
    }


    // Handle a POST request that submits data or changes a record
    @PostMapping("/process")
    // Simulate payment and update the booking, ticket and notification
    public String processPayment(

            @RequestParam Long bookingId,

            @Valid
            @ModelAttribute("paymentRequest")
            PaymentRequest paymentRequest,

            BindingResult result,

            // Principal identifies the currently logged-in account
            Principal principal,

            Model model) {


        User user =
                userService
                    .getUserByEmail(
                        principal.getName()
                    );


        Booking booking =
                bookingService
                    .getBookingForUser(
                        bookingId,
                        user.getId()
                    );


        // Return to the form when submitted values fail validation
        if (result.hasErrors()) {

            // Send these values to the HTML page
            model.addAttribute(
                "booking",
                booking
            );

            // Open the matching HTML template
            return "payment/form";
        }


        try {

            Payment payment =
                    paymentService
                        .processPayment(
                            booking,
                            paymentRequest
                        );


            if (payment.getStatus()
                    == PaymentStatus.SUCCESS) {

                // Ask the browser to load the next page
                return "redirect:/payment/success/"
                        + payment.getId();
            }


            if (payment.getStatus()
                    == PaymentStatus.FAILED) {

                // Ask the browser to load the next page
                return "redirect:/payment/failed/"
                        + payment.getId();
            }


            // Ask the browser to load the next page
            return "redirect:/payment/pending/"
                    + payment.getId();


        } catch (
                IllegalArgumentException e) {

            model.addAttribute(
                "booking",
                booking
            );

            model.addAttribute(
                "paymentError",
                e.getMessage()
            );

            // Open the matching HTML template
            return "payment/form";
        }
    }


    // Handle a GET request to load a page or download a file
    @GetMapping("/success/{paymentId}")
    public String success(
            // Read this ID from the URL path
            @PathVariable Long paymentId,
            // Principal identifies the currently logged-in account
            Principal principal,
            Model model) {

        User user = userService.getUserByEmail(principal.getName());

        // Send these values to the HTML page
        model.addAttribute(
            "payment",
            paymentService
                .getPaymentForUser(paymentId, user.getId())
        );

        // Open the matching HTML template
        return "payment/success";
    }


    // Handle a GET request to load a page or download a file
    @GetMapping("/failed/{paymentId}")
    public String failed(
            // Read this ID from the URL path
            @PathVariable Long paymentId,
            // Principal identifies the currently logged-in account
            Principal principal,
            Model model) {

        User user = userService.getUserByEmail(principal.getName());

        // Send these values to the HTML page
        model.addAttribute(
            "payment",
            paymentService
                .getPaymentForUser(paymentId, user.getId())
        );

        // Open the matching HTML template
        return "payment/failed";
    }


    // Handle a GET request to load a page or download a file
    @GetMapping("/pending/{paymentId}")
    public String pending(
            // Read this ID from the URL path
            @PathVariable Long paymentId,
            // Principal identifies the currently logged-in account
            Principal principal,
            Model model) {

        User user = userService.getUserByEmail(principal.getName());

        // Send these values to the HTML page
        model.addAttribute(
            "payment",
            paymentService
                .getPaymentForUser(paymentId, user.getId())
        );

        // Open the matching HTML template
        return "payment/pending";
    }
}
