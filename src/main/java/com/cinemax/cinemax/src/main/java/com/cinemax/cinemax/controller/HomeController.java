package com.cinemax.cinemax.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

// Handle browser requests and select HTML pages
@Controller
public class HomeController {

    // Handle a GET request to load a page or download a file
    @GetMapping("/")
    public String home() {
        // Ask the browser to load the next page
        return "redirect:/movies";
    }
}
