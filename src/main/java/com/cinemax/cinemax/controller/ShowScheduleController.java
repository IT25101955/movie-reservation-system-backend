package com.cinemax.cinemax.controller;

import com.cinemax.cinemax.dto.ShowBatchRequest;
import com.cinemax.cinemax.dto.ShowUpdateRequest;

import com.cinemax.cinemax.service.MovieService;
import com.cinemax.cinemax.service.TheatreService;
import com.cinemax.cinemax.service.ShowScheduleService;

import jakarta.validation.Valid;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;

import org.springframework.validation.BindingResult;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

// Handle browser requests and select HTML pages
@Controller
@RequestMapping("/admin/shows")
public class ShowScheduleController {

    private final ShowScheduleService showService;
    private final MovieService movieService;
    private final TheatreService theatreService;

    // Constructor injection supplies the dependencies this class needs
    public ShowScheduleController(
            ShowScheduleService showService,
            MovieService movieService,
            TheatreService theatreService) {

        this.showService = showService;
        this.movieService = movieService;
        this.theatreService = theatreService;
    }

    // Handle a GET request to load a page or download a file
    @GetMapping
    public String listShows(Model model) {

        // Send these values to the HTML page
        model.addAttribute("scheduleGroups", showService.getGroupedSchedules());

        // Open the matching HTML template
        return "admin/shows/list";
    }

    @PostMapping("/movie/{movieId}/delete-all")
    public String deleteAllMovieSchedules(
            @PathVariable Long movieId,
            RedirectAttributes redirectAttributes) {
        try {
            showService.deleteAllSchedulesAndBookingsForMovie(movieId);
            redirectAttributes.addFlashAttribute(
                    "success", "All schedules for this movie were permanently deleted.");
        } catch (Exception exception) {
            redirectAttributes.addFlashAttribute(
                    "error", "Unable to delete all schedules: " + exception.getMessage());
        }
        return "redirect:/admin/shows";
    }

    // Handle a GET request to load a page or download a file
    @GetMapping("/add")
    public String addShow(Model model) {

        // Send these values to the HTML page
        model.addAttribute(
            "showBatch",
            new ShowBatchRequest()
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
            @ModelAttribute("showBatch")
            ShowBatchRequest request,

            BindingResult result,

            Model model) {

        // Return to the form when submitted values fail validation
        if (result.hasErrors()) {

            loadOptions(model);

            // Open the matching HTML template
            return "admin/shows/form";
        }

        try {
            showService.createBatch(
                    request,
                    movieService.getActiveMovieById(request.getMovieId()),
                    theatreService.getActiveTheatreById(request.getTheatreId())
            );

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

    @PostMapping("/update")
    public String updateShows(
            @Valid @ModelAttribute("showUpdate") ShowUpdateRequest request,
            BindingResult result,
            Model model) {
        if (result.hasErrors()) {
            loadOptions(model);
            return "admin/shows/edit";
        }
        try {
            showService.updateDay(request);
            return "redirect:/admin/shows?success";
        } catch (IllegalArgumentException exception) {
            model.addAttribute("scheduleError", exception.getMessage());
            loadOptions(model);
            return "admin/shows/edit";
        }
    }

    // Handle a GET request to load a page or download a file
    @GetMapping("/edit/{id}")
    public String editShow(
            // Read this ID from the URL path
            @PathVariable Long id,
            Model model) {

        // Send these values to the HTML page
        com.cinemax.cinemax.model.ShowSchedule selected = showService.getShowById(id);
        ShowUpdateRequest request = new ShowUpdateRequest();
        request.setMovieId(selected.getMovie().getId());
        request.setTheatreId(selected.getTheatre().getId());
        request.setShowDate(selected.getShowDate());
        request.setAdultPrice(selected.getAdultPrice());
        request.setChildPrice(selected.getChildPrice());
        request.setVipPrice(selected.getVipPrice());
        java.util.List<com.cinemax.cinemax.model.ShowSchedule> dayShows = showService.getShowsForEdit(id);
        request.setStartTimes(dayShows.stream().map(show -> show.getStartTime()).toList());
        request.setShowIds(dayShows.stream().map(show -> show.getId()).toList());
        model.addAttribute("showUpdate", request);
        model.addAttribute("dayShows", dayShows);

        loadOptions(model);

        // Open the matching HTML template
        return "admin/shows/edit";
    }

    // Handle a POST request that submits data or changes a record
    @PostMapping("/{id}/delete")
    public String deleteShow(
            // Read this ID from the URL path
            @PathVariable Long id,
            RedirectAttributes redirectAttributes) {

        try {
            showService.deleteShow(id);
            redirectAttributes.addFlashAttribute("success", "Show permanently deleted.");
        } catch (IllegalArgumentException | IllegalStateException exception) {
            redirectAttributes.addFlashAttribute("error", exception.getMessage());
        } catch (Exception exception) {
            redirectAttributes.addFlashAttribute(
                    "error",
                    "Show could not be deleted because it is referenced by other records.");
        }

        // Ask the browser to load the next page
        return "redirect:/admin/shows";
    }

    private void loadOptions(Model model) {

        model.addAttribute(
            "movies",
            movieService.getActiveMovies()
        );

        model.addAttribute(
            "theatres",
            theatreService.getActiveTheatres()
        );
    }
}
