package com.cinemax.cinemax.controller;

import com.cinemax.cinemax.dto.ReviewRequest;

import com.cinemax.cinemax.model.Movie;
import com.cinemax.cinemax.model.User;

import com.cinemax.cinemax.service.MovieService;
import com.cinemax.cinemax.service.ReviewService;
import com.cinemax.cinemax.service.UserService;

import jakarta.validation.Valid;

import org.springframework.stereotype.Controller;

import org.springframework.ui.Model;

import org.springframework.validation.BindingResult;

import org.springframework.web.bind.annotation.*;

import java.security.Principal;

// Handle browser requests and select HTML pages
@Controller
@RequestMapping("/reviews")
public class ReviewController {

    private final ReviewService
            reviewService;

    private final MovieService
            movieService;

    private final UserService
            userService;


    // Constructor injection supplies the dependencies this class needs
    public ReviewController(
            ReviewService reviewService,
            MovieService movieService,
            UserService userService) {

        this.reviewService =
                reviewService;

        this.movieService =
                movieService;

        this.userService =
                userService;
    }


    // Handle a GET request to load a page or download a file
    @GetMapping("/movie/{movieId}/add")
    public String reviewForm(
            @PathVariable Long movieId,
            Principal principal,
            Model model) {

        // Get the currently logged-in customer
        User user =
                userService.getUserByEmail(
                    principal.getName()
                );

        // Check whether customer has booked this movie
        boolean canReview =
                reviewService.hasBookedMovie(
                user.getId(),
                movieId
            );

        // Return to the movie page when there is no confirmed booking
        if (!canReview) {
            return "redirect:/movies/"
                    + movieId
                    + "?noBooking";
        }

        // Get movie details
        Movie movie =
                movieService.getMovieById(
                    movieId
                );

        model.addAttribute(
            "movie",
            movie
        );

        model.addAttribute(
            "reviewRequest",
            new ReviewRequest()
        );

        return "reviews/form";
    }


    // Handle a POST request that submits data or changes a record
    @PostMapping("/movie/{movieId}")
    public String submitReview(
            @PathVariable Long movieId,
            @Valid
            @ModelAttribute("reviewRequest")
            ReviewRequest reviewRequest,
            BindingResult result,
            Principal principal,
            Model model) {

        // Get the currently logged-in customer
        User user =
                userService.getUserByEmail(
                    principal.getName()
                );

        // Check whether customer has booked this movie
        boolean canReview =
                reviewService.hasBookedMovie(
                    user.getId(),
                    movieId
                );

        // Return to the movie page when there is no confirmed booking
        if (!canReview) {
            return "redirect:/movies/"
                    + movieId
                    + "?noBooking";
        }

        Movie movie =
                movieService.getMovieById(
                    movieId
                );

        // Return to the form when submitted values fail validation
        if (result.hasErrors()) {
            model.addAttribute(
                "movie",
                movie
            );

            return "reviews/form";
        }

        reviewService.submitReview(
            user,
            movieId,
            reviewRequest
        );

        return "redirect:/movies/"
                + movieId
                + "?reviewSubmitted";
    }
}
