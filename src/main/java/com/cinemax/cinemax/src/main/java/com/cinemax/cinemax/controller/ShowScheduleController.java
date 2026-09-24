package com.cinemax.cinemax.controller;

import com.cinemax.cinemax.model.ShowSchedule;

import com.cinemax.cinemax.service.MovieService;
import com.cinemax.cinemax.service.ScreenService;
import com.cinemax.cinemax.service.ShowScheduleService;

import jakarta.validation.Valid;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;

import org.springframework.validation.BindingResult;

import org.springframework.web.bind.annotation.*;

// Handle browser requests and select HTML pages
@Controller
@RequestMapping("/admin/shows")
public class ShowScheduleController {

    private final ShowScheduleService showService;
    private final MovieService movieService;
    private final ScreenService screenService;

    // Constructor injection supplies the dependencies this class needs
    public ShowScheduleController(
            ShowScheduleService showService,
            MovieService movieService,
            ScreenService screenService) {

        this.showService = showService;
        this.movieService = movieService;
        this.screenService = screenService;
    }

    // Handle a GET request to load a page or download a file
    @GetMapping
    public String listShows(Model model) {

        // Send these values to the HTML page
        model.addAttribute(
            "shows",
            showService.getAllShows()
        );

        // Open the matching HTML template
        return "admin/shows/list";
    }

    // Handle a GET request to load a page or download a file
    @GetMapping("/add")
    public String addShow(Model model) {

        // Send these values to the HTML page
        model.addAttribute(
            "showSchedule",
            new ShowSchedule()
        );

        loadOptions(model);

        // Open the matching HTML template
        return "admin/shows/form";
    }

    // Handle a POST request that submits data or changes a record
    @PostMapping("/save")
    // Validate the schedule and protect details of existing bookings
    public String saveShow(

            @Valid
            @ModelAttribute("showSchedule")
            ShowSchedule show,

            BindingResult result,

            @RequestParam Long movieId,
            @RequestParam Long screenId,

            Model model) {

        // Return to the form when submitted values fail validation
        if (result.hasErrors()) {

            loadOptions(model);

            // Open the matching HTML template
            return "admin/shows/form";
        }

        show.setMovie(
            movieService.getActiveMovieById(movieId)
        );

        show.setScreen(
            screenService.getActiveScreenById(screenId)
        );

        try {

            showService.saveShow(show);

        } catch (IllegalArgumentException e) {

            // Send these values to the HTML page
            model.addAttribute(
                "scheduleError",
                e.getMessage()
            );

            loadOptions(model);

            // Open the matching HTML template
            return "admin/shows/form";
        }

        // Ask the browser to load the next page
        return "redirect:/admin/shows?success";
}

    // Handle a GET request to load a page or download a file
    @GetMapping("/edit/{id}")
    public String editShow(
            // Read this ID from the URL path
            @PathVariable Long id,
            Model model) {

        // Send these values to the HTML page
        model.addAttribute(
            "showSchedule",
            showService.getShowById(id)
        );

        loadOptions(model);

        // Open the matching HTML template
        return "admin/shows/form";
    }

    // Handle a POST request that submits data or changes a record
    @PostMapping("/{id}/delete")
    public String deleteShow(
            // Read this ID from the URL path
            @PathVariable Long id) {

        showService.deleteShow(id);

        // Ask the browser to load the next page
        return "redirect:/admin/shows?deleted";
    }

    // Handle a POST request that submits data or changes a record
    @PostMapping("/{id}/restore")
    public String restoreShow(
            // Read this ID from the URL path
            @PathVariable Long id) {

        try {
            showService.restoreShow(id);
            // Ask the browser to load the next page
            return "redirect:/admin/shows?restored";

        } catch (IllegalArgumentException exception) {
            // Ask the browser to load the next page
            return "redirect:/admin/shows?restoreError";
        }
    }

    private void loadOptions(Model model) {

        model.addAttribute(
            "movies",
            movieService.getActiveMovies()
        );

        model.addAttribute(
            "screens",
            screenService.getActiveScreens()
        );
    }
}
