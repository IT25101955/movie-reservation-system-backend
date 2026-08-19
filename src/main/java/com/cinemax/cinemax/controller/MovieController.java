package com.cinemax.cinemax.controller;

import com.cinemax.cinemax.model.Movie;
import com.cinemax.cinemax.model.MovieStatus;
import com.cinemax.cinemax.service.MovieService;

import jakarta.validation.Valid;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

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
    public String showAddMovieForm(
            Model model) {

        Movie movie = new Movie();

        movie.setStatus(
                MovieStatus.NOW_SHOWING
        );

        model.addAttribute(
                "movie",
                movie
        );

        model.addAttribute(
                "statuses",
                MovieStatus.values()
        );

        return "admin/movies/form";
    }


    // CREATE / UPDATE
    // Handle a POST request that submits data or changes a record
    @PostMapping("/save")
    public String saveMovie(
            @Valid
            @ModelAttribute("movie")
            Movie movie,

            BindingResult result,

            Model model) {


        /*
         * Validation errors such as
         * blank title or invalid duration.
         */
        if (result.hasErrors()) {

            model.addAttribute(
                    "statuses",
                    MovieStatus.values()
            );

            return "admin/movies/form";
        }


        try {

            movieService.saveMovie(
                    movie
            );


        } catch (
            IllegalArgumentException e
        ) {

            /*
             * Put duplicate error underneath
             * Movie Title.
             */
            result.rejectValue(
                    "title",
                    "duplicate",
                    e.getMessage()
            );


            model.addAttribute(
                    "statuses",
                    MovieStatus.values()
            );


            return "admin/movies/form";
        }


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
    public String deleteMovie(
            @PathVariable Long id,
            RedirectAttributes redirectAttributes) {

        try {
            movieService.deleteMovie(id);
            redirectAttributes.addFlashAttribute("success", "Movie permanently deleted.");
        } catch (IllegalArgumentException | IllegalStateException exception) {
            redirectAttributes.addFlashAttribute("error", exception.getMessage());
        } catch (Exception exception) {
            // Print the REAL database error in Terminal.
            // This helps identify any table we may have missed.
            exception.printStackTrace();

            redirectAttributes.addFlashAttribute(
                    "error",
                    "Movie deletion failed. Check the Terminal for the exact database error.");
        }

        // Ask the browser to load the next page
        return "redirect:/admin/movies";
    }


    @PostMapping("/{id}/delete-permanently")
    public String permanentlyDeleteMovie(
            @PathVariable Long id,
            RedirectAttributes redirectAttributes) {

        try {

            movieService.permanentlyDeleteMovie(id);

            redirectAttributes.addFlashAttribute(
                    "success",
                    "Movie permanently deleted from the database."
            );

        } catch (IllegalStateException e) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    e.getMessage()
            );

        } catch (IllegalArgumentException e) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    e.getMessage()
            );

        } catch (Exception e) {

            // Print the REAL database error in Terminal.
            // This helps identify any table we may have missed.
            e.printStackTrace();

            redirectAttributes.addFlashAttribute(
                    "error",
                    "Movie deletion failed. Check the Terminal for the exact database error."
            );

        }

        return "redirect:/admin/movies";
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
