package com.cinemax.cinemax.controller;

import com.cinemax.cinemax.model.Theatre;
import com.cinemax.cinemax.service.TheatreService;

import jakarta.validation.Valid;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

// Handle browser requests and select HTML pages
@Controller
@RequestMapping("/admin/theatres")
public class TheatreController {

    private final TheatreService theatreService;

    // Constructor injection supplies the dependencies this class needs
    public TheatreController(
            TheatreService theatreService) {

        this.theatreService = theatreService;
    }

    // Handle a GET request to load a page or download a file
    @GetMapping
    public String listTheatres(Model model) {

        // Send these values to the HTML page
        model.addAttribute(
            "theatres",
            theatreService.getAllTheatres()
        );

        // Open the matching HTML template
        return "admin/theatres/list";
    }

    // Handle a GET request to load a page or download a file
    @GetMapping("/add")
    public String addTheatre(Model model) {

        // Send these values to the HTML page
        model.addAttribute(
            "theatre",
            new Theatre()
        );

        // Open the matching HTML template
        return "admin/theatres/form";
    }

    // Handle a POST request that submits data or changes a record
    @PostMapping("/save")
    public String saveTheatre(
            @Valid
            @ModelAttribute("theatre")
            Theatre theatre,

            BindingResult result) {

        // Return to the form when submitted values fail validation
        if (result.hasErrors()) {
            // Open the matching HTML template
            return "admin/theatres/form";
        }

        theatreService.saveTheatre(theatre);

        // Ask the browser to load the next page
        return "redirect:/admin/theatres?success";
    }

    // Handle a GET request to load a page or download a file
    @GetMapping("/edit/{id}")
    public String editTheatre(
            // Read this ID from the URL path
            @PathVariable Long id,
            Model model) {

        // Send these values to the HTML page
        model.addAttribute(
            "theatre",
            theatreService.getTheatreById(id)
        );

        // Open the matching HTML template
        return "admin/theatres/form";
    }

    // Handle a POST request that submits data or changes a record
    @PostMapping("/{id}/delete")
    public String deleteTheatre(
            // Read this ID from the URL path
            @PathVariable Long id) {

        theatreService.deleteTheatre(id);

        // Ask the browser to load the next page
        return "redirect:/admin/theatres?deleted";
    }

    // Handle a POST request that submits data or changes a record
    @PostMapping("/{id}/restore")
    public String restoreTheatre(
            // Read this ID from the URL path
            @PathVariable Long id) {

        theatreService.restoreTheatre(id);

        // Ask the browser to load the next page
        return "redirect:/admin/theatres?restored";
    }
}
