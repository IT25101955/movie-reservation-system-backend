package com.cinemax.cinemax.controller;

import com.cinemax.cinemax.model.Payment;
import com.cinemax.cinemax.model.PaymentStatus;
import com.cinemax.cinemax.repository.PaymentRepository;
import com.cinemax.cinemax.service.PaymentService;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// Handle browser requests and select HTML pages
@Controller
@RequestMapping("/admin/payments")
public class AdminPaymentController {

    private final PaymentRepository paymentRepository;
    private final PaymentService paymentService;

    // Constructor injection supplies the dependencies this class needs
    public AdminPaymentController(
            PaymentRepository paymentRepository,
            PaymentService paymentService) {
        this.paymentRepository = paymentRepository;
        this.paymentService = paymentService;
    }

    // Handle a GET request to load a page or download a file
    @GetMapping
    public String payments(
            @RequestParam(required = false) PaymentStatus status,
            Model model) {

        List<Payment> payments = status == null
                ? paymentRepository.findAllByOrderByCreatedAtDesc()
                : paymentRepository.findByStatusOrderByCreatedAtDesc(status);

        // Send these values to the HTML page
        model.addAttribute("payments", payments);
        model.addAttribute("statuses", PaymentStatus.values());
        model.addAttribute("selectedStatus", status);

        // Open the matching HTML template
        return "admin/payments/list";
    }

    // Handle a POST request that submits data or changes a record
    @PostMapping("/{id}/success")
    public String markSuccess(@PathVariable Long id) {
        try {
            paymentService.resolvePendingPayment(id, PaymentStatus.SUCCESS);
        } catch (IllegalArgumentException exception) {
            // Ask the browser to load the next page
            return "redirect:/admin/payments?resolveError";
        }
        // Ask the browser to load the next page
        return "redirect:/admin/payments?updated";
    }

    // Handle a POST request that submits data or changes a record
    @PostMapping("/{id}/failed")
    public String markFailed(@PathVariable Long id) {
        try {
            paymentService.resolvePendingPayment(id, PaymentStatus.FAILED);
        } catch (IllegalArgumentException exception) {
            // Ask the browser to load the next page
            return "redirect:/admin/payments?resolveError";
        }
        // Ask the browser to load the next page
        return "redirect:/admin/payments?updated";
    }
}
