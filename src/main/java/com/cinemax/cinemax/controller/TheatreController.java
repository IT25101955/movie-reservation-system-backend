package com.cinemax.cinemax.controller;

import com.cinemax.cinemax.model.Theatre;
import com.cinemax.cinemax.service.TheatreService;
import com.cinemax.cinemax.repository.FacilityRepository;

import jakarta.validation.Valid;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

// Handle browser requests and select HTML pages
@Controller
@RequestMapping("/admin/theatres")
public class TheatreController {

    private final TheatreService theatreService;
    private final FacilityRepository facilityRepository;

    // Constructor injection supplies the dependencies this class needs
    public TheatreController(
            TheatreService theatreService,
            FacilityRepository facilityRepository) {

        this.theatreService = theatreService;
        this.facilityRepository = facilityRepository;
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
        model.addAttribute("facilities", facilityRepository.findAll());
        model.addAttribute("selectedFacilityIds", Set.of());

        // Open the matching HTML template
        return "admin/theatres/form";
    }

    // Handle a POST request that submits data or changes a record
    @PostMapping("/save")
    public String saveTheatre(
            @Valid
            @ModelAttribute("theatre")
            Theatre theatre,

            BindingResult result,

            @RequestParam(name = "facilityIds", required = false) List<Long> facilityIds,

        Model model) {

        // Return to the form when submitted values fail validation
        if (result.hasErrors()) {
            model.addAttribute("facilities", facilityRepository.findAll());
            // Open the matching HTML template
            return "admin/theatres/form";
        }

        theatreService.saveTheatre(theatre, facilityIds);

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
        model.addAttribute("facilities", facilityRepository.findAll());
        model.addAttribute(
            "selectedFacilityIds",
            theatreService.getTheatreById(id).getFacilities().stream()
                .map(facility -> facility.getId())
                .collect(Collectors.toSet())
        );

        // Open the matching HTML template
        return "admin/theatres/form";
    }

    @GetMapping("/{id}/seats")
    public String manageSeats(@PathVariable Long id, Model model) {
        List<com.cinemax.cinemax.model.Seat> seats = theatreService.getSeats(id);
        model.addAttribute("theatre", theatreService.getTheatreById(id));
        model.addAttribute("seats", seats);
        model.addAttribute("rows", seats.stream()
                .map(seat -> seat.getRowLabel())
                .distinct()
                .sorted()
                .toList());
        model.addAttribute("vipRows", theatreService.getVipRows(id));
        return "admin/theatres/seats";
    }

    @PostMapping("/{theatreId}/seats/vip-rows")
    public String updateVipRows(
            @PathVariable Long theatreId,
            @RequestParam(name = "vipRows", required = false) List<String> vipRows) {
        theatreService.updateVipRows(theatreId, vipRows);
        return "redirect:/admin/theatres/" + theatreId + "/seats";
    }

    // Handle a POST request that submits data or changes a record
    @PostMapping("/{id}/delete")
    public String deleteTheatre(
            // Read this ID from the URL path
            @PathVariable Long id,
            RedirectAttributes redirectAttributes) {

        try {
            theatreService.deleteTheatre(id);
            redirectAttributes.addFlashAttribute("success", "Theatre permanently deleted.");
        } catch (IllegalArgumentException | IllegalStateException exception) {
            redirectAttributes.addFlashAttribute("error", exception.getMessage());
        } catch (Exception exception) {
            redirectAttributes.addFlashAttribute(
                    "error",
                    "Theatre could not be deleted because it is referenced by other records.");
        }

        // Ask the browser to load the next page
        return "redirect:/admin/theatres";
    }

}
