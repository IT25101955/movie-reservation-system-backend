package com.cinemax.cinemax.controller;

import com.cinemax.cinemax.service.SalesReportService;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;

// Handle browser requests and select HTML pages
@Controller
@RequestMapping("/admin/reports")
public class AdminReportController {

    private final SalesReportService reportService;

    // Constructor injection supplies the dependencies this class needs
    public AdminReportController(SalesReportService reportService) {
        this.reportService = reportService;
    }

    // Handle a GET request to load a page or download a file
    @GetMapping("/sales")
    public String salesReport(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate startDate,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate endDate,
            Model model) {

        try {
            Map<String, Object> report =
                    reportService.generateReport(startDate, endDate);

            addReportToModel(report, model);

        } catch (IllegalArgumentException exception) {
            // Send these values to the HTML page
            model.addAttribute("reportError", exception.getMessage());
            Map<String, Object> fullReport =
                    reportService.generateReport(null, null);

            addReportToModel(fullReport, model);
        }

        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);

        // Open the matching HTML template
        return "admin/reports/sales";
    }

    private void addReportToModel(
            Map<String, Object> report,
            Model model) {

        // Send every report value to the HTML page
        for (Map.Entry<String, Object> item : report.entrySet()) {
            model.addAttribute(item.getKey(), item.getValue());
        }
    }
}
