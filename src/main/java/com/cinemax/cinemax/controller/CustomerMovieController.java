package com.cinemax.cinemax.controller;

import com.cinemax.cinemax.model.MovieStatus;
import com.cinemax.cinemax.service.MovieService;
import com.cinemax.cinemax.service.ReviewService;
import com.cinemax.cinemax.service.UserService;

import java.security.Principal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;

import org.springframework.web.bind.annotation.*;

// Handle browser requests and select HTML pages
@Controller
@RequestMapping("/movies")
public class CustomerMovieController {

    private final MovieService movieService;

    private final ReviewService reviewService;
        private final UserService userService;

    // Constructor injection supplies the dependencies this class needs
    public CustomerMovieController(
            MovieService movieService,
            ReviewService reviewService,
            UserService userService) {

        this.movieService = movieService;

        this.reviewService = reviewService;
        this.userService = userService;
    }


    // Handle a GET request to load a page or download a file
    @GetMapping
    public String movies(
            @RequestParam(
                required = false
            )
            String keyword,

            @RequestParam(required = false)
            MovieStatus status,

            Model model) {

        // Send these values to the HTML page
        model.addAttribute(
                "movies",
                movieService
                    .getPublicMovies(keyword, status)
        );

        model.addAttribute(
                "keyword",
                keyword
        );

        model.addAttribute(
                "selectedStatus",
                status
        );

        // Open the matching HTML template
        return "movies/list";
    }


    // Handle a GET request to load a page or download a file
    @GetMapping("/{id}")
    public String movieDetails(
            // Read this ID from the URL path
            @PathVariable Long id,
            Model model,
            Principal principal) {

        // Send these values to the HTML page
        model.addAttribute(
                "movie",
                movieService
                    .getActiveMovieById(id)
        );

        model.addAttribute(
                "movies",
                movieService.getPublicMovies(null)
        );

        model.addAttribute(
        "reviews",
        reviewService
                .getApprovedReviews(id)
        );


        model.addAttribute(
        "averageRating",
        reviewService
                .getAverageRating(id)
        );


        model.addAttribute(
        "reviewCount",
        reviewService
                .getApprovedReviewCount(id)
        );

        if (principal != null) {
            model.addAttribute(
                    "currentUser",
                    userService.getUserByEmail(principal.getName())
            );
        }

        // Open the matching HTML template
        return "movies/details";
    }
}
