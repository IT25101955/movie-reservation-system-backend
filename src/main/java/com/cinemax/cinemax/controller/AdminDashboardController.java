package com.cinemax.cinemax.controller;

import com.cinemax.cinemax.service.AdminDashboardService;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

// Handle browser requests and select HTML pages
@Controller
@RequestMapping("/admin")
public class AdminDashboardController {

    private final AdminDashboardService dashboardService;

    // Constructor injection supplies the dependencies this class needs
    public AdminDashboardController(
            AdminDashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    // Handle a GET request to load a page or download a file
    @GetMapping({"", "/dashboard"})
    public String dashboard(Model model) {

        // Send these values to the HTML page
        model.addAttribute(
                "stats",
                dashboardService.getStats()
        );

        // Open the matching HTML template
        return "admin/dashboard";
    }
}
