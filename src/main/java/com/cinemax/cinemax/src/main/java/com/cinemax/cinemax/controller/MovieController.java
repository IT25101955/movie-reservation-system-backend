package com.cinemax.cinemax.controller;

import com.cinemax.cinemax.model.Movie;
import com.cinemax.cinemax.model.MovieStatus;
import com.cinemax.cinemax.service.MovieService;

import jakarta.validation.Valid;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;

import org.springframework.web.bind.annotation.*;

// Handle browser requests and select HTML pages
@Controller
@RequestMapping("/admin/movies")
public class MovieController {

    private final MovieService movieService;

    // Constructor injection supplies the dependencies this class needs
    public MovieController(MovieService movieService) {
        this.movieService = movieService;
    }


    // READ + SEARCH
    // Handle a GET request to load a page or download a file
    @GetMapping
    public String movieList(
            @RequestParam(required = false) String keyword,
            Model model) {

        // Send these values to the HTML page
        model.addAttribute(
                "movies",
                movieService.searchMovies(keyword)
        );

        model.addAttribute("keyword", keyword);

        // Open the matching HTML template
        return "admin/movies/list";
    }


    // SHOW CREATE FORM
    // Handle a GET request to load a page or download a file
    @GetMapping("/add")
    public String showAddMovieForm(Model model) {

        // Send these values to the HTML page
        model.addAttribute("movie", new Movie());

        model.addAttribute(
                "statuses",
                MovieStatus.values()
        );

        // Open the matching HTML template
        return "admin/movies/form";
    }


    // CREATE
    // Handle a POST request that submits data or changes a record
    @PostMapping("/save")
    public String saveMovie(
            @Valid @ModelAttribute("movie") Movie movie,
            BindingResult result,
            Model model) {

        // Return to the form when submitted values fail validation
        if (result.hasErrors()) {

            // Send these values to the HTML page
            model.addAttribute(
                    "statuses",
                    MovieStatus.values()
            );

            // Open the matching HTML template
            return "admin/movies/form";
        }

        movieService.saveMovie(movie);

        // Ask the browser to load the next page
        return "redirect:/admin/movies?success";
    }


    // SHOW UPDATE FORM
    // Handle a GET request to load a page or download a file
    @GetMapping("/edit/{id}")
    public String showEditMovieForm(
            // Read this ID from the URL path
            @PathVariable Long id,
            Model model) {

        // Send these values to the HTML page
        model.addAttribute(
                "movie",
                movieService.getMovieById(id)
        );

        model.addAttribute(
                "statuses",
                MovieStatus.values()
        );

        // Open the matching HTML template
        return "admin/movies/form";
    }


    // DELETE
    // Handle a POST request that submits data or changes a record
    @PostMapping("/{id}/delete")
    public String deleteMovie(@PathVariable Long id) {

        movieService.deleteMovie(id);

        // Ask the browser to load the next page
        return "redirect:/admin/movies?deleted";
    }


    // Handle a POST request that submits data or changes a record
    @PostMapping("/{id}/restore")
    public String restoreMovie(@PathVariable Long id) {

        movieService.restoreMovie(id);

        // Ask the browser to load the next page
        return "redirect:/admin/movies?restored";
    }


    // VIEW
    // Handle a GET request to load a page or download a file
    @GetMapping("/view/{id}")
    public String viewMovie(
            // Read this ID from the URL path
            @PathVariable Long id,
            Model model) {

        // Send these values to the HTML page
        model.addAttribute(
                "movie",
                movieService.getMovieById(id)
        );

        // Open the matching HTML template
        return "admin/movies/view";
    }
}
