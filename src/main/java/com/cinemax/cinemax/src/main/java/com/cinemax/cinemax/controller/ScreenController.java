package com.cinemax.cinemax.controller;

import com.cinemax.cinemax.model.Screen;
import com.cinemax.cinemax.model.Theatre;

import com.cinemax.cinemax.service.ScreenService;
import com.cinemax.cinemax.service.TheatreService;

import jakarta.validation.Valid;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;

import org.springframework.web.bind.annotation.*;

// Handle browser requests and select HTML pages
@Controller
@RequestMapping("/admin/screens")
public class ScreenController {

    private final ScreenService screenService;
    private final TheatreService theatreService;


    // Constructor injection supplies the dependencies this class needs
    public ScreenController(
            ScreenService screenService,
            TheatreService theatreService) {

        this.screenService = screenService;
        this.theatreService = theatreService;
    }


    // Handle a GET request to load a page or download a file
    @GetMapping
    public String listScreens(Model model) {

        // Send these values to the HTML page
        model.addAttribute(
                "screens",
                screenService.getAllScreens()
        );

        // Open the matching HTML template
        return "admin/screens/list";
    }


    // Handle a GET request to load a page or download a file
    @GetMapping("/add")
    public String addScreen(Model model) {

        // Send these values to the HTML page
        model.addAttribute(
                "screen",
                new Screen()
        );

        model.addAttribute(
                "theatres",
                theatreService.getActiveTheatres()
        );

        // Open the matching HTML template
        return "admin/screens/form";
    }


    // Handle a POST request that submits data or changes a record
    @PostMapping("/save")
    // Save the screen and rebuild seats only when its layout changes
    public String saveScreen(

            @Valid
            @ModelAttribute("screen")
            Screen screen,

            BindingResult result,

            @RequestParam Long theatreId,

            Model model) {

        // Return to the form when submitted values fail validation
        if (result.hasErrors()) {

            // Send these values to the HTML page
            model.addAttribute(
                    "theatres",
                    theatreService.getActiveTheatres()
            );

            // Open the matching HTML template
            return "admin/screens/form";
        }


        Theatre theatre =
                theatreService
                        .getActiveTheatreById(theatreId);

        screen.setTheatre(theatre);

        try {
            screenService.saveScreen(screen);

        } catch (IllegalArgumentException exception) {
            model.addAttribute("screenError", exception.getMessage());
            model.addAttribute(
                    "theatres",
                    theatreService.getActiveTheatres()
            );
            // Open the matching HTML template
            return "admin/screens/form";
        }

        // Ask the browser to load the next page
        return "redirect:/admin/screens?success";
    }


    // Handle a GET request to load a page or download a file
    @GetMapping("/edit/{id}")
    public String editScreen(
            // Read this ID from the URL path
            @PathVariable Long id,
            Model model) {

        Screen screen =
                screenService.getScreenById(id);

        // Send these values to the HTML page
        model.addAttribute(
                "screen",
                screen
        );

        model.addAttribute(
                "theatres",
                theatreService.getActiveTheatres()
        );

        // Open the matching HTML template
        return "admin/screens/form";
    }


    // Handle a GET request to load a page or download a file
    @GetMapping("/seats/{id}")
    public String viewSeats(
            // Read this ID from the URL path
            @PathVariable Long id,
            Model model) {

        // Send these values to the HTML page
        model.addAttribute(
                "screen",
                screenService.getScreenById(id)
        );

        model.addAttribute(
                "seats",
                screenService.getSeats(id)
        );

        // Open the matching HTML template
        return "admin/screens/seats";
    }


    // Handle a POST request that submits data or changes a record
    @PostMapping("/{id}/delete")
    public String deleteScreen(@PathVariable Long id) {

        screenService.deleteScreen(id);

        // Ask the browser to load the next page
        return "redirect:/admin/screens?deleted";
    }


    // Handle a POST request that submits data or changes a record
    @PostMapping("/{id}/restore")
    public String restoreScreen(@PathVariable Long id) {

        screenService.restoreScreen(id);

        // Ask the browser to load the next page
        return "redirect:/admin/screens?restored";
    }
}
