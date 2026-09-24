package com.cinemax.cinemax.controller;

import com.cinemax.cinemax.service.MovieService;
import com.cinemax.cinemax.service.ReviewService;
import com.cinemax.cinemax.service.ShowScheduleService;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;

import org.springframework.web.bind.annotation.*;

// Handle browser requests and select HTML pages
@Controller
@RequestMapping("/movies")
public class CustomerMovieController {

    private final MovieService movieService;

    private final ShowScheduleService showScheduleService;

    private final ReviewService reviewService;

    // Constructor injection supplies the dependencies this class needs
    public CustomerMovieController(
            MovieService movieService,
            ShowScheduleService showScheduleService,
            ReviewService reviewService) {

        this.movieService = movieService;

        this.showScheduleService = showScheduleService;

        this.reviewService = reviewService;
    }


    // Handle a GET request to load a page or download a file
    @GetMapping
    public String movies(
            @RequestParam(
                    required = false
            )
            String keyword,

            Model model) {

        // Send these values to the HTML page
        model.addAttribute(
                "movies",
                movieService
                        .getPublicMovies(keyword)
        );

        model.addAttribute(
                "keyword",
                keyword
        );

        // Open the matching HTML template
        return "movies/list";
    }


    // Handle a GET request to load a page or download a file
    @GetMapping("/{id}")
    public String movieDetails(
            // Read this ID from the URL path
            @PathVariable Long id,
            Model model) {

        // Send these values to the HTML page
        model.addAttribute(
                "movie",
                movieService
                        .getActiveMovieById(id)
        );

        model.addAttribute(
                "shows",
                showScheduleService
                        .getFutureShowsForMovie(id)
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

        // Open the matching HTML template
        return "movies/details";
    }
}
