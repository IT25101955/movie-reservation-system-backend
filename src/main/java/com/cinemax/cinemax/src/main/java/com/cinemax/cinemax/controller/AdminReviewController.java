package com.cinemax.cinemax.controller;

import com.cinemax.cinemax.service.ReviewService;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

// Handle browser requests and select HTML pages
@Controller
@RequestMapping("/admin/reviews")
public class AdminReviewController {

    private final ReviewService reviewService;

    // Constructor injection supplies the dependencies this class needs
    public AdminReviewController(
            ReviewService reviewService) {

        this.reviewService = reviewService;
    }


    // Handle a GET request to load a page or download a file
    @GetMapping
    public String reviews(Model model) {

        // Send these values to the HTML page
        model.addAttribute(
                "reviews",
                reviewService.getAllReviews()
        );

        // Open the matching HTML template
        return "admin/reviews/list";
    }


    // Handle a POST request that submits data or changes a record
    @PostMapping("/{id}/approve")
    // Approve the review and record the moderation time
    public String approveReview(
            // Read this ID from the URL path
            @PathVariable Long id) {

        reviewService.approveReview(id);

        // Ask the browser to load the next page
        return "redirect:/admin/reviews?approved";
    }


    // Handle a POST request that submits data or changes a record
    @PostMapping("/{id}/reject")
    // Reject the review and record the moderation time
    public String rejectReview(
            // Read this ID from the URL path
            @PathVariable Long id) {

        reviewService.rejectReview(id);

        // Ask the browser to load the next page
        return "redirect:/admin/reviews?rejected";
    }
}
